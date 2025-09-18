package com.example.dust.mqttv5demo;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

/**
 * MQTT消息订阅处理
 */
@Slf4j
@Configuration
public class MqttSubscriber {

    @Resource
    ObjectMapper objectMapper;
    @Resource
    MqttPublisher mqttPublisher;

    /**
     * 处理接收到的MQTT消息
     * 此处由inbound通道统一接收消息，并将处理后的消息发送到bound通道做业务处理
     * 
     * @param message 接收到的消息
     * @return 处理后的消息
     */
    @ServiceActivator(inputChannel = ChannelName.INBOUND, outputChannel = ChannelName.BOUND)
    public Message<?> handleMessage(Message<?> message) {
        Object payload = message.getPayload();
        String text;
        if (payload instanceof byte[] bytes) {
            try {
                text = objectMapper.readValue(bytes, JSONObject.class).toJSONString();
            } catch (Exception e) {
                text = new String(bytes, StandardCharsets.UTF_8);
            }
        } else {
            text = String.valueOf(payload);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String time = sdf.format(message.getHeaders().getTimestamp());
        log.info("{} Received MQTT message: {}", time, text);
        return message;
    }

    /**
     * 处理bound通道的消息，做业务处理
     * 
     * @param message 接收到的消息
     */
    @ServiceActivator(inputChannel = ChannelName.BOUND)
    public void handleOutMessage(Message<?> message) {
        log.info("handleOutMessage___________________");
        mqttPublisher.sendToTopic(message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC) + "_reply",
                "mqttBoundChannel___________________");
    }

}
