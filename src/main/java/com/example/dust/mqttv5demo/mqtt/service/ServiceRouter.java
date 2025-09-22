package com.example.dust.mqttv5demo.mqtt.service;

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
 * 服务路由器，处理服务相关的消息
 */
@Slf4j
@Configuration
public class ServiceRouter {
    @Resource
    ObjectMapper objectMapper;
    @Resource
    MqttPublisher mqttPublisher;
    @Resource
    CorrelationManager correlationManager;

    @ServiceActivator(inputChannel = MqttChannels.OUTBOUND_SERVICES)
    public void outboundServices(Message<?> message) {
        log.info("outboundServices: {}", message);
        mqttPublisher.publish("outboundServices");
    }

    @ServiceActivator(inputChannel = MqttChannels.INBOUND_SERVICES_REPLY)
    public void inboundServicesReply(Message<?> message) {
        log.info("inboundServicesReply: {}", message);
        try {
            CommonTopicResponse<?> response = objectMapper.readValue((byte[]) message.getPayload(),
                    CommonTopicResponse.class);
            correlationManager.complete(response.getTid(), response);
        } catch (Exception e) {
            log.error("inboundPropertySetReply: {}", e.getMessage());
        }
    }
}
