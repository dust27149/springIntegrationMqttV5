package com.example.dust.mqttv5demo.component.mqtt.router.requests;

import com.example.dust.mqttv5demo.component.mqtt.router.defaults.CommonTopicResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * requests主题响应
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
public class RequestsTopicResponse<T> extends CommonTopicResponse<T> {

    private String method;

}
