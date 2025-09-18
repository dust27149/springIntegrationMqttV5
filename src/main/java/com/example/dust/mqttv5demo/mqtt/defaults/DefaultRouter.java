package com.example.dust.mqttv5demo.mqtt.defaults;

import com.example.dust.mqttv5demo.mqtt.MqttChannels;
import com.example.dust.mqttv5demo.mqtt.MqttPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

/**
 * 默认路由器，处理未匹配到具体路由的消息
 */
@Slf4j
@Configuration
public class DefaultRouter {

    @Resource
    ObjectMapper objectMapper;
    @Resource
    MqttPublisher mqttPublisher;

    @ServiceActivator(inputChannel = MqttChannels.INBOUND_DEFAULT)
    public void inboundDefault(Message<?> message) {
        log.error("inboundDefault: {}", message);
    }

    @ServiceActivator(inputChannel = MqttChannels.OUTBOUND_DEFAULT)
    public void outboundDefault(Message<?> message) {
        log.error("outboundDefault: {}", message);
        mqttPublisher.publish("outboundDefault");
    }
}
