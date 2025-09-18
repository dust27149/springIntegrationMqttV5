package com.example.dust.mqttv5demo.mqtt.event;

import com.example.dust.mqttv5demo.mqtt.MqttChannels;
import com.example.dust.mqttv5demo.mqtt.MqttPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;

/**
 * 事件路由器，处理事件相关的消息
 */
@Slf4j
@Configuration
public class EventRouter {

    @Resource
    ObjectMapper objectMapper;
    @Resource
    MqttPublisher mqttPublisher;

    @ServiceActivator(inputChannel = MqttChannels.INBOUND_EVENTS, outputChannel = MqttChannels.OUTBOUND_EVENTS_REPLY)
    public Message<?> inboundEvents(Message<?> message) {
        log.info("inboundEvents: {}", message);
        return message;
    }

    @ServiceActivator(inputChannel = MqttChannels.OUTBOUND_EVENTS_REPLY)
    public void outboundEventsReply(Message<?> message) {
        log.info("outboundEventsReply: {}", message);
        mqttPublisher.publish(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC) + "_reply", "outboundEventsReply");
    }
}
