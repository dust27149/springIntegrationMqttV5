package com.example.dust.mqttv5demo.component.mqtt.router.drc;

import com.example.dust.mqttv5demo.component.mqtt.MqttChannels;
import com.example.dust.mqttv5demo.component.mqtt.MqttPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

/**
 * DRC路由器，处理DRC相关的消息
 */
@Slf4j
@Configuration
public class DrcRouter {

    @Resource
    ObjectMapper objectMapper;
    @Resource
    MqttPublisher mqttPublisher;

    @ServiceActivator(inputChannel = MqttChannels.INBOUND_DRC_UP)
    public void inboundDrcUp(Message<?> message) {
        log.info("inboundDrcUp: {}", message);
    }

    @ServiceActivator(inputChannel = MqttChannels.OUTBOUND_DRC_DOWN)
    public void outboundDrcDown(Message<?> message) {
        log.info("outboundDrcDown: {}", message);
        mqttPublisher.publish("outboundDrcDown");
    }
}
