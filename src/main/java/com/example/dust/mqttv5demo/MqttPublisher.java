package com.example.dust.mqttv5demo;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

/**
 * MQTT消息发布接口
 */
@MessagingGateway
public interface MqttPublisher {

        /**
         * 发送消息到默认主题，对应的工作流会使用默认主题发送MQTT消息
         * 
         * @param payload 消息内容
         */
        @Gateway(requestChannel = ChannelName.OUTBOUND)
        void send(String payload);

        /**
         * 发送消息到指定主题，对应的工作流会使用指定主题发送MQTT消息
         * 
         * @param topic   主题
         * @param payload 消息内容
         */
        @Gateway(requestChannel = ChannelName.OUTBOUND)
        void sendToTopic(@Header(MqttHeaders.TOPIC) String topic,
                        String payload);
}
