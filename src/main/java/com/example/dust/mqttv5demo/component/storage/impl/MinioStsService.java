package com.example.dust.mqttv5demo.component.storage.impl;

import com.example.dust.mqttv5demo.common.excetpions.CustomException;
import com.example.dust.mqttv5demo.common.utils.FileTools;
import com.example.dust.mqttv5demo.component.storage.StsService;
import com.example.dust.mqttv5demo.component.storage.model.StsInfo;
import io.minio.*;
import io.minio.credentials.AssumeRoleProvider;
import io.minio.credentials.Credentials;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;

@Service
@Slf4j
@Component
@ConditionalOnProperty(prefix = "storage.provider", name = "minio", havingValue = "true")
public class MinioStsService implements StsService {

    @Value("${storage.endpoint}")
    String endpoint;
    @Value("${storage.accessKey}")
    String accessKey;
    @Value("${storage.secretKey}")
    String secretKey;
    @Value("${storage.region}")
    String region;
    @Value("${storage.bucket}")
    String bucket;
    @Value("${storage.roleArn}")
    String roleArn;
    @Value("${storage.roleSessionName}")
    String roleSessionName;
    @Value("${storage.expire:3600}")
    int expire = 3600;

    @Override
    public StsInfo getStsInfo() {
        try {
            AssumeRoleProvider provider = new AssumeRoleProvider(endpoint, accessKey, secretKey, expire, null, region, null, null, null, null);
            log.info("获取minio临时凭证成功");
            return getStsInfo(provider.fetch());
        } catch (Exception e) {
            throw new CustomException("获取minio临时凭证失败");
        }
    }

    @Override
    public void uploadFile(FileInputStream stream, String filePath) {
        MinioClient client;
        try {
            // 构造客户端
            if (roleArn != null && !roleArn.isEmpty()) {
                AssumeRoleProvider provider = new AssumeRoleProvider(endpoint, accessKey, secretKey, expire, null, region, roleArn, roleSessionName, null, null);
                client = MinioClient.builder()
                        .endpoint(endpoint)
                        .region(region)
                        .credentialsProvider(provider)
                        .build();
            } else {
                client = MinioClient.builder()
                        .endpoint(endpoint)
                        .region(region)
                        .credentials(accessKey, secretKey)
                        .build();
            }

            // 桶检测与创建
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());

            PutObjectArgs.Builder putBuilder = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .contentType(FileTools.guessContentType(filePath));

            try {
                long available = stream.available();
                if (available > 0)
                    putBuilder.stream(stream, available, -1);
                else
                    putBuilder.stream(stream, -1, 10 * 1024 * 1024);// 未知长度 -> -1 + 指定分片大小
            } catch (Exception ignore) {
            }
            ObjectWriteResponse resp = client.putObject(putBuilder.build());
            log.info("MinIO 上传成功 bucket={} key={}", resp.bucket(), resp.object());
        } catch (Exception e) {
            throw new CustomException("MinIO 上传失败");
        } finally {
            try {
                stream.close();
            } catch (Exception ignore) {
            }
        }
    }

    private @NotNull StsInfo getStsInfo(Credentials credentials) {
        StsInfo stsInfo = new StsInfo();
        StsInfo.Credentials cred = new StsInfo.Credentials();
        cred.setAccessKeyId(credentials.accessKey());
        cred.setAccessKeySecret(credentials.secretKey());
        cred.setSecurityToken(credentials.sessionToken());
        cred.setExpire(expire);
        stsInfo.setCredentials(cred);
        stsInfo.setEndpoint(endpoint);
        stsInfo.setProvider("minio");
        stsInfo.setRegion(region);
        return stsInfo;
    }
}
