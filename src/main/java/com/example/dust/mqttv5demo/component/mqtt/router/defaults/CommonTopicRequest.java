package com.example.dust.mqttv5demo.component.mqtt.router.defaults;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用主题请求
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonTopicRequest<T> {

    protected String tid;

    protected String bid;

    protected T data;

    protected Long timestamp;
}
