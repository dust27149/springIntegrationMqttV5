// package com.example.dust.mqttv5demo;

// import lombok.extern.slf4j.Slf4j;

// import java.util.Arrays;
// import java.util.concurrent.LinkedBlockingQueue;
// import java.util.concurrent.ThreadPoolExecutor;
// import java.util.concurrent.TimeUnit;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.integration.channel.ExecutorChannel;
// import org.springframework.integration.dsl.IntegrationFlow;
// import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
// import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
// import org.springframework.messaging.MessageChannel;
// import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

// /**
//  * MQTT V3配置
//  */
// @Slf4j
// @Configuration
// public class MqttV3Config {

//     /**
//      * inbound通道，接收MQTT消息
//      * 
//      * @return 消息通道
//      */
//     @Bean(name = ChannelName.INBOUND)
//     public MessageChannel inboundChannel() {
//         ThreadPoolExecutor executor = new ThreadPoolExecutor(Config.CORE_POOL_SIZE, Config.MAX_POOL_SIZE,
//                 Config.KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<>(Config.QUEUE_CAPACITY),
//                 new CustomizableThreadFactory("inboundThreadPool-"), new ThreadPoolExecutor.AbortPolicy());
//         return new ExecutorChannel(executor);
//     }

//     /**
//      * MQTT V3消息接收配置
//      * 
//      * @return 集成流
//      */
//     @Bean
//     public IntegrationFlow mqttV3InFlow() {
//         MqttPahoMessageDrivenChannelAdapter messageProducer = new MqttPahoMessageDrivenChannelAdapter(Config.BROKER,
//                 Config.SUB,
//                 Arrays.stream(Config.TOPIC_TO_SUB).map(s -> Config.SHARED_GROUP + s).toArray(String[]::new));
//         return IntegrationFlow.from(messageProducer).channel(ChannelName.INBOUND).get();
//     }

//     /**
//      * MQTT V3消息发送配置
//      * 
//      * @return 集成流
//      */
//     @Bean
//     public IntegrationFlow mqttV3OutFlow() {
//         MqttPahoMessageHandler handler = new MqttPahoMessageHandler(Config.BROKER, Config.PUB);
//         handler.setAsync(true);
//         handler.setDefaultTopic(Config.TOPIC_TO_PUB);
//         // MqttHeaderMapper mqttHeaderMapper = new MqttHeaderMapper();
//         // mqttHeaderMapper.setOutboundHeaderNames("some_user_header",
//         // MessageHeaders.CONTENT_TYPE);
//         // handler.setHeaderMapper(mqttHeaderMapper);
//         return IntegrationFlow.from(ChannelName.OUTBOUND).handle(handler).get();
//     }
// }
