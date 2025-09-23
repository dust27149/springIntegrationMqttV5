package com.example.dust.mqttv5demo.component.mqtt.router.requests;

import com.example.dust.mqttv5demo.component.mqtt.router.defaults.CommonTopicRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * requests主题请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
public class RequestsTopicRequest<T> extends CommonTopicRequest<T> {

    private String method;

}
