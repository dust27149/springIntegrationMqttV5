package com.example.dust.mqttv5demo.mqtt.correlation;

import lombok.Getter;

/**
 * 表示等待设备响应超时的异常，用于区分设备真实返回 null 与超时未返回的情况。
 */
@Getter
public class CorrelationTimeoutException extends RuntimeException {
    private String tid;

    public CorrelationTimeoutException(String tid) {
        super("Correlation timeout tid=" + tid);
        this.tid = tid;
    }

}
