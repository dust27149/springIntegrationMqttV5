package com.example.dust.mqttv5demo.mqtt.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用主题响应
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonTopicResponse<T> {

    protected String tid;

    protected String bid;

    protected T data;

    protected Long timestamp;
}
