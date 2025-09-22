package com.example.dust.mqttv5demo.component.mqtt.router.state;

import com.example.dust.mqttv5demo.component.mqtt.MqttChannels;
import com.example.dust.mqttv5demo.component.mqtt.MqttPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;

/**
 * 状态路由器，处理状态相关的消息
 */
@Slf4j
@Configuration
public class StateRouter {

    @Resource
    ObjectMapper objectMapper;
    @Resource
    MqttPublisher mqttPublisher;

    @ServiceActivator(inputChannel = MqttChannels.INBOUND_STATE, outputChannel = MqttChannels.OUTBOUND_STATE_REPLY)
    public Message<?> inboundState(Message<?> message) {
        log.info("inboundState: {}", message);
        return message;
    }

    @ServiceActivator(inputChannel = MqttChannels.OUTBOUND_STATE_REPLY)
    public void outboundStateReply(Message<?> message) {
        log.info("outboundStateReply: {}", message);
        mqttPublisher.publish(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC) + "_reply", "outboundStateReply");
    }
}
