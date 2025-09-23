package com.example.dust.mqttv5demo.component.mqtt.router.events;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.dust.mqttv5demo.common.model.AirsenseWarning;
import com.example.dust.mqttv5demo.component.mqtt.MqttChannels;
import com.example.dust.mqttv5demo.component.mqtt.MqttPublisher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageHeaders;

import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * 事件路由器，处理事件相关的消息
 */
@Slf4j
@Configuration
public class EventsRouter {

    @Resource
    ObjectMapper objectMapper;
    @Resource
    MqttPublisher mqttPublisher;

    @Bean
    public IntegrationFlow eventRouterFlow() {
        return IntegrationFlow.from(MqttChannels.INBOUND_EVENTS)
                .<byte[], EventsTopicRequest<?>>transform(bytes -> {
                    try {
                        log.debug("eventRouterFlows收到消息: {}", new String(bytes));
                        EventsTopicRequest<Object> req = objectMapper.readValue(bytes,
                                new TypeReference<>() {

                                });
                        req.setData(objectMapper.convertValue(req.getData(),
                                EventsMethodEnums.find(req.getMethod()).getClazz()));
                        log.debug("eventRouter转换消息成功: {}", req);
                        return req;
                    } catch (Exception e) {
                        log.error("eventRouter转换消息失败: {}", e.getMessage());
                        return null;
                    }
                })
                .<EventsTopicRequest<?>, EventsMethodEnums>route(
                        receiver -> EventsMethodEnums.find(receiver.getMethod()),
                        mapping -> Arrays.stream(EventsMethodEnums.values())
                                .forEach(e -> mapping.channelMapping(e, MqttChannels.OUTBOUND_EVENTS_REPLY)))
                .get();
    }

    @ServiceActivator(inputChannel = MqttChannels.INBOUND_EVENTS_DEFAULT, outputChannel = MqttChannels.OUTBOUND_EVENTS_REPLY)
    public EventsTopicRequest<LinkedHashMap<String, Object>> inboundEventsDefault(
            EventsTopicRequest<LinkedHashMap<String, Object>> message) {
        log.error("[events] 未适配的消息: {}", message);
        return message;
    }

    @ServiceActivator(inputChannel = MqttChannels.INBOUND_EVENTS_AIRSENESE_WARNING, outputChannel = MqttChannels.OUTBOUND_EVENTS_REPLY)
    public EventsTopicRequest<AirsenseWarning[]> inboundEventsAirsenseWarning(
            EventsTopicRequest<AirsenseWarning[]> message) {
        log.debug("[events] AirsenseWarning收到消息: {}", message);
        return message;
    }

    @ServiceActivator(inputChannel = MqttChannels.OUTBOUND_EVENTS_REPLY)
    public void outboundEventsReply(EventsTopicRequest<?> message, MessageHeaders header) {
        log.debug("[events] Reply收到消息: {}", message);
        if (message.getNeedReply() == null || message.getNeedReply() == 0) {
            log.debug("不需要回复消息");
            return;
        }

        JSONObject data = new JSONObject();
        data.put("result", 0);

        EventsTopicResponse<JSONObject> response = new EventsTopicResponse<>();
        response.setTid(message.getTid());
        response.setBid(message.getBid());
        response.setTimestamp(message.getTimestamp());
        response.setMethod(message.getMethod());
        response.setData(data);
        mqttPublisher.publish(header.get(MqttHeaders.RECEIVED_TOPIC) + "_reply", JSON.toJSONString(response));
    }
}
