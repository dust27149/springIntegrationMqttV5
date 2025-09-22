package com.example.dust.mqttv5demo.component.mqtt;

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

        String text;
        if (payload instanceof byte[] bytes) {
            text = new String(bytes, StandardCharsets.UTF_8);
        } else if (payload instanceof String s) {
            text = s;
        } else {
            text = String.valueOf(payload);
        }
        log.info("{} topic<{}> Received MQTT message (payloadType={} size={}): {}", time, topic,
                payload == null ? "null" : payload.getClass().getSimpleName(),
                payload instanceof byte[] b ? b.length : text.length(), text);

        MqttMessageEnums topicEnum = MqttMessageEnums.find(topic);
        MessageChannel bean = (MessageChannel) applicationContext.getBean(topicEnum.getBeanName());
        return Collections.singleton(bean);
    }

}
