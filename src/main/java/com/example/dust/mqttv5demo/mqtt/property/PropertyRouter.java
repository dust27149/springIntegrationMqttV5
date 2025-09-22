package com.example.dust.mqttv5demo.mqtt.property;

import com.example.dust.mqttv5demo.mqtt.MqttChannels;
import com.example.dust.mqttv5demo.mqtt.MqttPublisher;
import com.example.dust.mqttv5demo.mqtt.correlation.CorrelationManager;
import com.example.dust.mqttv5demo.mqtt.defaults.CommonTopicResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

/**
 * 属性路由器，处理属性相关的消息
 */
@Slf4j
@Configuration
public class PropertyRouter {

    @Resource
    ObjectMapper objectMapper;
    @Resource
    MqttPublisher mqttPublisher;
    @Resource
    CorrelationManager correlationManager;

    @ServiceActivator(inputChannel = MqttChannels.OUTBOUND_PROPERTY_SET)
    public void inboundProperty(Message<?> message) {
        log.info("inboundProperty: {}", message);
        mqttPublisher.publish("inboundProperty");
    }

    @ServiceActivator(inputChannel = MqttChannels.INBOUND_PROPERTY_SET_REPLY)
    public void inboundPropertySetReply(Message<?> message) {
        log.info("inboundPropertySetReply: {}", message);
        try {
            CommonTopicResponse<?> response = objectMapper.readValue((byte[]) message.getPayload(),
                    CommonTopicResponse.class);
            correlationManager.complete(response.getTid(), response);
        } catch (Exception e) {
            log.error("inboundPropertySetReply: {}", e.getMessage());
        }
    }

}
