// package com.example.dust.mqttv5demo.mqtt;

// import lombok.extern.slf4j.Slf4j;

// import java.util.Arrays;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.integration.dsl.IntegrationFlow;
// import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
// import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;

// /**
//  * MQTT V3配置
//  */
// @Slf4j
// @Configuration
// public class MqttV3Subscriber {

//     @Value("${mqtt.broker}")
//     String broker;
//     @Value("${mqtt.username}")
//     String username;
//     @Value("${mqtt.password}")
//     String password;
//     @Value("${mqtt.client-id-sub}")
//     String clientIdSub;
//     @Value("${mqtt.client-id-pub}")
//     String clientIdPub;
//     @Value("${mqtt.group-id}")
//     String groupId;
//     @Value("${mqtt.default-topic-to-sub}")
//     String[] defaultTopicToSub;
//     @Value("${mqtt.default-topic-to-pub}")
//     String defaultTopicToPub;

//     /**
//      * MQTT V3消息接收配置
//      * 
//      * @return 集成流
//      */
//     @Bean
//     public IntegrationFlow mqttV3InFlow() {
//         MqttPahoMessageDrivenChannelAdapter messageProducer = new MqttPahoMessageDrivenChannelAdapter(broker,
//                 clientIdSub,
//                 Arrays.stream(defaultTopicToSub).map(s -> "$shared/" + groupId + "/" + s).toArray(String[]::new));
//         return IntegrationFlow.from(messageProducer).channel(MqttChannels.INBOUND).get();
//     }

//     /**
//      * MQTT V3消息发送配置
//      * 
//      * @return 集成流
//      */
//     @Bean
//     public IntegrationFlow mqttV3OutFlow() {
//         MqttPahoMessageHandler handler = new MqttPahoMessageHandler(broker, clientIdPub);
//         handler.setAsync(true);
//         handler.setDefaultTopic(defaultTopicToPub);
//         // MqttHeaderMapper mqttHeaderMapper = new MqttHeaderMapper();
//         // mqttHeaderMapper.setOutboundHeaderNames("some_user_header",
//         // MessageHeaders.CONTENT_TYPE);
//         // handler.setHeaderMapper(mqttHeaderMapper);
//         return IntegrationFlow.from(MqttChannels.OUTBOUND).handle(handler).get();
//     }
// }
