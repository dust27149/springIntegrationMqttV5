package com.example.dust.mqttv5demo.component.mqtt.router.property;

import com.example.dust.mqttv5demo.component.mqtt.MqttChannels;
import com.example.dust.mqttv5demo.component.mqtt.MqttPublisher;
import com.example.dust.mqttv5demo.component.mqtt.replyTracker.ReplyTracker;
import com.example.dust.mqttv5demo.component.mqtt.router.defaults.CommonTopicResponse;
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
    ReplyTracker replyTracker;

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
            replyTracker.complete(response.getTid(), response);
        } catch (Exception e) {
            log.error("inboundPropertySetReply: {}", e.getMessage());
        }
    }

}
