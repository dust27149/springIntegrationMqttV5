package com.example.dust.mqttv5demo.mqtt.defaults;

import lombok.Data;

/**
 * 通用主题请求
 */
@Data
public class CommonTopicRequest<T> {

    protected String tid;

    protected String bid;

    protected T data;

    protected Long timestamp;
}
