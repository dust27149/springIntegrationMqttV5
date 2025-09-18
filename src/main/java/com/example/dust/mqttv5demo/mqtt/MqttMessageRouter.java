package com.example.dust.mqttv5demo.mqtt;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;

/**
 * MQTT消息路由器，根据主题将消息路由到不同的业务通道
 */
@Slf4j
@Configuration
public class MqttMessageRouter extends AbstractMessageRouter {

    @Resource
    ApplicationContext applicationContext;

    @Override
    @Router(inputChannel = MqttChannels.INBOUND)
    protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
        String topic = (String) message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        Object payload = message.getPayload();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String time = sdf.format(message.getHeaders().getTimestamp());
        log.info("{} topic<{}> Received MQTT message: {}", time, topic,
                new String((byte[]) payload, StandardCharsets.UTF_8));

        MqttMessageEnums topicEnum = MqttMessageEnums.find(topic);
        MessageChannel bean = (MessageChannel) applicationContext.getBean(topicEnum.getBeanName());
        return Collections.singleton(bean);
    }

}
