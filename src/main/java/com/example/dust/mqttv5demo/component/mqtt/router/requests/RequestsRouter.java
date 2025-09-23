package com.example.dust.mqttv5demo.component.mqtt.router.requests;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
 * 请求路由器，处理请求相关的消息
 */
@Slf4j
@Configuration
public class RequestsRouter {

    @Resource
    ObjectMapper objectMapper;
    @Resource
    MqttPublisher mqttPublisher;

    @Bean
    public IntegrationFlow eventRouterFlow() {
        return IntegrationFlow.from(MqttChannels.INBOUND_EVENTS)
                .<byte[], RequestsTopicRequest<?>>transform(bytes -> {
                    try {
                        log.debug("requestRouterFlows收到消息: {}", new String(bytes));
                        RequestsTopicRequest<Object> req = objectMapper.readValue(bytes,
                                new TypeReference<>() {

                                });
                        req.setData(objectMapper.convertValue(req.getData(),
                                RequestsMethodEnums.find(req.getMethod()).getClazz()));
                        log.debug("requestRouter转换消息成功: {}", req);
                        return req;
                    } catch (Exception e) {
                        log.error("requestRouter转换消息失败: {}", e.getMessage());
                        return null;
                    }
                })
                .<RequestsTopicRequest<?>, RequestsMethodEnums>route(
                        receiver -> RequestsMethodEnums.find(receiver.getMethod()),
                        mapping -> Arrays.stream(RequestsMethodEnums.values())
                                .forEach(e -> mapping.channelMapping(e, e.getChannel())))
                .get();
    }

    @ServiceActivator(inputChannel = MqttChannels.INBOUND_REQUESTS_DEFAULT, outputChannel = MqttChannels.OUTBOUND_REQUESTS_REPLY)
    public RequestsTopicRequest<LinkedHashMap<String, Object>> inboundRequestsDefault(
            RequestsTopicRequest<LinkedHashMap<String, Object>> message) {
        log.error("[requests] 未适配的消息: {}", message);
        return message;
    }

    @ServiceActivator(inputChannel = MqttChannels.OUTBOUND_REQUESTS_REPLY)
    public void outboundRequestsReply(RequestsTopicRequest<?> message, MessageHeaders header) {
        log.debug("[requests] Reply消息: {}", message);
        
        JSONObject data = new JSONObject();
        data.put("result", "put data here");

        RequestsTopicResponse<JSONObject> response = new RequestsTopicResponse<>();
        response.setTid(message.getTid());
        response.setBid(message.getBid());
        response.setTimestamp(message.getTimestamp());
        response.setMethod(message.getMethod());
        response.setData(data);
        mqttPublisher.publish(header.get(MqttHeaders.RECEIVED_TOPIC) + "_reply", JSON.toJSONString(response));
    }

}
