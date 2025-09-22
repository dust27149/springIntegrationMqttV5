package com.example.dust.mqttv5demo.component.mqtt.router.osd;

import com.example.dust.mqttv5demo.component.mqtt.MqttChannels;
import com.example.dust.mqttv5demo.component.mqtt.MqttPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    @Resource
    RabbitTemplate rabbitTemplate;

    String exchange = "exchange.osd";
    String routingKey = "router.osd";

    @ServiceActivator(inputChannel = MqttChannels.INBOUND_OSD)
    public void inboundOSD(Message<?> message) {
        log.info("inboundOSD: {}", message);
        try {
            Object payload = message.getPayload();
            // 保持字符串或JSON字符串，其他对象转成JSON
            String body;
            if (payload instanceof String s) {
                body = s;
            } else {
                body = objectMapper.writeValueAsString(payload);
            }
            rabbitTemplate.convertAndSend(exchange, routingKey, body);
            log.debug("OSD消息转发成功, exchange={} routingKey={} size={}", exchange, routingKey, body.length());
        } catch (Exception e) {
            log.error("OSD消息转发失败", e);
        }
    }
}
