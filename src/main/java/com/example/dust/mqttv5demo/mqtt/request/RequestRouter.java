package com.example.dust.mqttv5demo.mqtt.request;

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
 * 请求路由器，处理请求相关的消息
 */
@Slf4j
@Configuration
public class RequestRouter {

    @Resource
    ObjectMapper objectMapper;
    @Resource
    MqttPublisher mqttPublisher;

    @ServiceActivator(inputChannel = MqttChannels.INBOUND_REQUESTS, outputChannel = MqttChannels.OUTBOUND_REQUESTS_REPLY)
    public Message<?> inboundRequests(Message<?> message) {
        log.info("inboundRequests: {}", message);
        return message;
    }

    @ServiceActivator(inputChannel = MqttChannels.OUTBOUND_REQUESTS_REPLY)
    public void outboundRequestsReply(Message<?> message) {
        log.info("outboundRequestsReply: {}", message);
        mqttPublisher.publish(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC) + "_reply", "outboundRequestsReply");
    }
}
