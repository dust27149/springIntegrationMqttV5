package com.example.dust.mqttv5demo.component.storage.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.profile.DefaultProfile;
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
@ConditionalOnProperty(prefix = "storage.provider", name = "oss", havingValue = "true")
public class OssStsService implements StsService {

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
            DefaultProfile profile = DefaultProfile.getProfile(region, accessKey, secretKey);
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setDurationSeconds((long) expire);
            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            AssumeRoleResponse response = new DefaultAcsClient(profile).getAcsResponse(request);
            log.info("获取OSS临时凭证成功");
            return getStsInfo(response.getCredentials());
        } catch (Exception e) {
            throw new CustomException("获取OSS临时凭证失败");
        }
    }

    @Override
    public void uploadFile(FileInputStream stream, String filePath) {
        // OSS上传逻辑
        OSS oss = null;
        try {
            StsInfo sts = getStsInfo();
            StsInfo.Credentials c = getStsInfo().getCredentials();
            // 使用 STS 三元组构建临时会话客户端
            oss = new OSSClientBuilder().build(
                    sts.getEndpoint(),
                    c.getAccessKeyId(),
                    c.getAccessKeySecret(),
                    c.getSecurityToken()
            );
            // 若桶不存在则创建(无服务端策略限制时)
            if (!oss.doesBucketExist(bucket)) oss.createBucket(bucket);
            ObjectMetadata meta = new ObjectMetadata();
            // 可选: 设置长度(不准确可忽略)
            try {
                int available = stream.available();
                if (available > 0) meta.setContentLength(available);
                meta.setContentType(FileTools.guessContentType(filePath));
            } catch (Exception ignore) {
            }
            oss.putObject(new PutObjectRequest(bucket, filePath, stream, meta));
            log.info("OSS 文件上传成功 bucket={} key={}", bucket, filePath);
        } catch (Exception e) {
            throw new CustomException("OSS文件上传失败");
        } finally {
            try {
                stream.close();
            } catch (Exception ignore) {
            }
            if (oss != null) {
                oss.shutdown();
            }
        }
    }

    private @NotNull StsInfo getStsInfo(AssumeRoleResponse.Credentials credentials) {
        StsInfo stsInfo = new StsInfo();
        StsInfo.Credentials cred = new StsInfo.Credentials();
        cred.setAccessKeyId(credentials.getAccessKeyId());
        cred.setAccessKeySecret(credentials.getAccessKeySecret());
        cred.setSecurityToken(credentials.getSecurityToken());
        cred.setExpire(expire);
        stsInfo.setCredentials(cred);
        stsInfo.setEndpoint(endpoint);
        stsInfo.setProvider("aliyun");
        stsInfo.setRegion(region);
        return stsInfo;
    }
}
