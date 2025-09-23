package com.example.dust.mqttv5demo.component.mqtt.router.events;

import com.example.dust.mqttv5demo.component.mqtt.router.defaults.CommonTopicRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
public class EventsTopicRequest<T> extends CommonTopicRequest<T> {

    private String method;

    private Integer needReply;

}
