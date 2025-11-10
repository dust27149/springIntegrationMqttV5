package com.example.dust.mqttv5demo.component.storage.model;

import lombok.Data;

/**
 * 临时安全令牌信息
 */
@Data
public class StsInfo {
    /**
     * 存储桶名称
     */
    private String bucket;
    /**
     * 临时凭证
     */
    private Credentials credentials;
    /**
     * 访问端点
     */
    private String endpoint;
    /**
     * 对象存储路径前缀
     */
    private String objectKeyPrefix;
    /**
     * 提供商
     */
    private String provider;
    /**
     * 地域
     */
    private String region;

    @Data
    public static class Credentials {
        /**
         * 访问密钥ID
         */
        private String accessKeyId;
        /**
         * 访问密钥Secret
         */
        private String accessKeySecret;
        /**
         * 过期时间，单位：秒
         */
        private Integer expire;
        /**
         * 安全令牌
         */
        private String securityToken;

    }
}
