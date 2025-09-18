package com.example.dust.mqttv5demo.mqtt.osd;

import com.example.dust.mqttv5demo.mqtt.MqttChannels;
import com.example.dust.mqttv5demo.mqtt.MqttPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

/**
 * OSD路由器，处理OSD相关的消息
 */
@Slf4j
@Configuration
public class OsdRouter {

    @Resource
    ObjectMapper objectMapper;
    @Resource
    MqttPublisher mqttPublisher;

    @ServiceActivator(inputChannel = MqttChannels.INBOUND_OSD)
    public void inboundOSD(Message<?> message) {
        log.info("inboundOSD: {}", message);
    }
}
