package com.example.dust.mqttv5demo.mqtt;

import lombok.extern.slf4j.Slf4j;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.inbound.Mqttv5PahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.outbound.Mqttv5PahoMessageHandler;

import java.util.Arrays;

/**
 * MQTT 订阅配置
 */
@Slf4j
@Configuration
public class MqttSubscriber {

        @Value("${mqtt.useMqttv3:false}")
        boolean useMqttv3;
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
        @Value("${mqtt.keep-alive-interval:60}")
        int keepAliveInterval;
        @Value("${mqtt.connection-timeout:30}")
        int connectionTimeout;
        @Value("${mqtt.receive-max:100}")
        int receiveMax;
        @Value("${mqtt.automatic-reconnect:true}")
        boolean automaticReconnect;
        @Value("${mqtt.clean-start:true}")
        boolean cleanStart;
        @Value("${mqtt.qos:2}")
        int qos;
        @Value("${mqtt.group-id}")
        String groupId;
        @Value("${mqtt.default-topic-to-sub}")
        String[] defaultTopicToSub;
        @Value("${mqtt.default-topic-to-pub}")
        String defaultTopicToPub;

        /**
         * MQTT 消息接收配置
         *
         * @return 集成流
         */
        @Bean
        public IntegrationFlow mqttInFlow() {
                return IntegrationFlow.from(useMqttv3 ? getV3MessageProducer() : getV5MessageProducer())
                                .channel(MqttChannels.INBOUND)
                                .get();
        }

        /**
         * MQTT 消息发送配置
         *
         * @return 集成流
         */
        @Bean
        public IntegrationFlow mqttOutFlow() {
                return IntegrationFlow.from(MqttChannels.OUTBOUND)
                                .handle(useMqttv3 ? getV3MessageHandler() : getV5MessageHandler())
                                .get();
        }

        /**
         * 获取共享订阅主题
         * 
         * @return 主题列表
         */
        private String[] getSharedTopic() {
                String[] topics = Arrays.stream(defaultTopicToSub)
                                .map(s -> "$share/" + groupId + "/" + s)
                                .toArray(String[]::new);
                log.info("{} 订阅topic: {}", useMqttv3 ? "mqttV3" : "mqttV5", Arrays.toString(topics));
                return topics;
        }

        private Mqttv5PahoMessageDrivenChannelAdapter getV5MessageProducer() {
                MqttConnectionOptions options = new MqttConnectionOptions();
                options.setServerURIs(new String[] { broker });
                options.setUserName(username);
                options.setPassword(password.getBytes());
                options.setAutomaticReconnect(automaticReconnect);
                options.setCleanStart(cleanStart);
                options.setKeepAliveInterval(keepAliveInterval);
                options.setConnectionTimeout(connectionTimeout);
                options.setReceiveMaximum(receiveMax);
                Mqttv5PahoMessageDrivenChannelAdapter adapter = new Mqttv5PahoMessageDrivenChannelAdapter(options,
                                clientIdSub, getSharedTopic());
                adapter.setQos(qos);
                return adapter;
        }

        private Mqttv5PahoMessageHandler getV5MessageHandler() {
                Mqttv5PahoMessageHandler handler = new Mqttv5PahoMessageHandler(broker, clientIdPub);
                handler.setAsync(true);
                handler.setDefaultTopic(defaultTopicToPub);
                handler.setDefaultQos(qos);
                return handler;
        }

        private MqttPahoMessageDrivenChannelAdapter getV3MessageProducer() {
                MqttConnectOptions options = new MqttConnectOptions();
                options.setServerURIs(new String[] { broker });
                options.setUserName(username);
                options.setPassword(password.toCharArray());
                options.setAutomaticReconnect(automaticReconnect);
                options.setCleanSession(cleanStart);
                options.setKeepAliveInterval(keepAliveInterval);
                options.setConnectionTimeout(connectionTimeout);
                options.setMaxInflight(receiveMax);
                DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
                factory.setConnectionOptions(options);
                MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                                clientIdSub, factory, getSharedTopic());
                adapter.setQos(qos);
                return adapter;
        }

        private MqttPahoMessageHandler getV3MessageHandler() {
                MqttPahoMessageHandler handler = new MqttPahoMessageHandler(broker, clientIdPub);
                handler.setAsync(true);
                handler.setDefaultTopic(defaultTopicToPub);
                handler.setDefaultQos(qos);
                return handler;
        }

}