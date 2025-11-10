package com.example.dust.mqttv5demo.component.storage.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.example.dust.mqttv5demo.common.excetpions.CustomException;
import com.example.dust.mqttv5demo.common.utils.FileTools;
import com.example.dust.mqttv5demo.component.storage.StsService;
import com.example.dust.mqttv5demo.component.storage.model.StsInfo;
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
@ConditionalOnProperty(prefix = "storage.provider", name = "s3", havingValue = "true")
public class S3StsService implements StsService {

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
            AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                    .withRegion(region).build();

            AssumeRoleRequest request = new AssumeRoleRequest()
                    .withRoleArn(roleArn)
                    .withRoleSessionName(roleSessionName)
                    .withDurationSeconds(Math.toIntExact(expire));
            AssumeRoleResult result = stsClient.assumeRole(request);
            log.info("获取S3临时凭证成功");
            return getStsInfo(result.getCredentials());
        } catch (Exception e) {
            throw new CustomException("获取S3临时凭证失败");
        }
    }

    @Override
    public void uploadFile(FileInputStream stream, String filePath) {
        AmazonS3 s3 = null;
        try {
            // 构建 S3 客户端(兼容 MinIO: 自定义 endpoint + PathStyle)
            s3 = AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                    .withPathStyleAccessEnabled(true)
                    .withCredentials(getAwsStaticCredentialsProvider())
                    .build();

            // 桶存在性检查
            if (!s3.doesBucketExistV2(bucket)) s3.createBucket(bucket);

            ObjectMetadata meta = new ObjectMetadata();
            try {
                int available = stream.available();
                if (available > 0) meta.setContentLength(available);
                meta.setContentType(FileTools.guessContentType(filePath));
            } catch (Exception ignore) {
            }
            s3.putObject(new PutObjectRequest(bucket, filePath, stream, meta));
            log.info("S3 上传成功 bucket={} key={}", bucket, filePath);
        } catch (Exception e) {
            throw new CustomException("S3上传文件失败");
        } finally {
            try {
                stream.close();
            } catch (Exception ignore) {
            }
            if (s3 != null) {
                s3.shutdown();
            }
        }
    }

    private @NotNull AWSStaticCredentialsProvider getAwsStaticCredentialsProvider() {
        StsInfo.Credentials c = getStsInfo().getCredentials();
        if (c.getSecurityToken() != null && !c.getSecurityToken().isEmpty()) {
            BasicSessionCredentials sessionCred = new BasicSessionCredentials(c.getAccessKeyId(), c.getAccessKeySecret(), c.getSecurityToken());
            return new AWSStaticCredentialsProvider(sessionCred);
        } else {
            return new AWSStaticCredentialsProvider(new BasicAWSCredentials(c.getAccessKeyId(), c.getAccessKeySecret()));
        }
    }

    private @NotNull StsInfo getStsInfo(Credentials credentials) {
        StsInfo stsInfo = new StsInfo();
        StsInfo.Credentials cred = new StsInfo.Credentials();
        cred.setAccessKeyId(credentials.getAccessKeyId());
        cred.setAccessKeySecret(credentials.getSecretAccessKey());
        cred.setSecurityToken(credentials.getSessionToken());
        cred.setExpire(expire);
        stsInfo.setCredentials(cred);
        stsInfo.setEndpoint(endpoint);
        stsInfo.setProvider("aws");
        stsInfo.setRegion(region);
        return stsInfo;
    }
}
