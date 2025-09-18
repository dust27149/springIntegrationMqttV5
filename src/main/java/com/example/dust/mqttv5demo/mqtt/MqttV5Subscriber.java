package com.example.dust.mqttv5demo.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.Mqttv5PahoMessageHandler;

import java.util.Arrays;

/**
 * MQTT V5配置
 */
@Slf4j
@Configuration
public class MqttV5Subscriber {

    @Value("${mqtt.broker}")
    String broker;
    @Value("${mqtt.username}")
    String username;
    @Value("${mqtt.password}")
    String password;
    @Value("${mqtt.client-id-sub}")
    String clientIdSub;
    @Value("${mqtt.client-id-pub}")
    String clientIdPub;
    @Value("${mqtt.keep-alive-interval}")
    int keepAliveInterval;
    @Value("${mqtt.connection-timeout}")
    int connectionTimeout;
    @Value("${mqtt.receive-max}")
    int receiveMax;
    @Value("${mqtt.automatic-reconnect}")
    boolean automaticReconnect;
    @Value("${mqtt.clean-start}")
    boolean cleanStart;
    @Value("${mqtt.qos}")
    int qos;
    @Value("${mqtt.group-id}")
    String groupId;
    @Value("${mqtt.default-topic-to-sub}")
    String[] defaultTopicToSub;
    @Value("${mqtt.default-topic-to-pub}")
    String defaultTopicToPub;

    /**
     * MQTT V5消息接收配置
     *
     * @return 集成流
     */
    @Bean
    public IntegrationFlow mqttV5InFlow() {
        MqttConnectionOptions options = new MqttConnectionOptions();
        options.setServerURIs(new String[]{broker});
        options.setUserName(username);
        options.setPassword(password.getBytes());
        options.setAutomaticReconnect(automaticReconnect);
        options.setCleanStart(cleanStart);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setConnectionTimeout(connectionTimeout);
        options.setReceiveMaximum(receiveMax);
        String[] sharedTopic = Arrays.stream(defaultTopicToSub).map(s -> "$share/" + groupId + "/" + s)
                .toArray(String[]::new);
        Mqttv5PahoMessageDrivenChannelAdapter messageProducer = new Mqttv5PahoMessageDrivenChannelAdapter(broker,
                clientIdSub, sharedTopic);
        return IntegrationFlow.from(messageProducer)
                .channel(MqttChannels.INBOUND)
                .get();
    }

    /**
     * MQTT V5消息发送配置
     *
     * @return 集成流
     */
    @Bean
    public IntegrationFlow mqttV5OutFlow() {
        Mqttv5PahoMessageHandler handler = new Mqttv5PahoMessageHandler(broker, clientIdPub);
        handler.setAsync(true);
        handler.setDefaultTopic(defaultTopicToPub);
        // MqttHeaderMapper mqttHeaderMapper = new MqttHeaderMapper();
        // mqttHeaderMapper.setOutboundHeaderNames("some_user_header",
        // MessageHeaders.CONTENT_TYPE);
        // handler.setHeaderMapper(mqttHeaderMapper);
        return IntegrationFlow.from(MqttChannels.OUTBOUND)
                .handle(handler)
                .get();
    }

}