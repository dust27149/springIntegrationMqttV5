package com.example.dust.mqttv5demo.mqtt.defaults;

import lombok.Data;

/**
 * 通用主题响应
 */
@Data
public class CommonTopicResponse<T> {

    protected String tid;

    protected String bid;

    protected T data;

    protected Long timestamp;
}
