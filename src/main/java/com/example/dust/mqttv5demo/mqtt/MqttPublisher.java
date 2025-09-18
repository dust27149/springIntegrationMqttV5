package com.example.dust.mqttv5demo.mqtt;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.example.dust.mqttv5demo.mqtt.defaults.CommonTopicRequest;
import com.example.dust.mqttv5demo.mqtt.defaults.CommonTopicResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;

@Configuration
public class MqttPublisher {

    @Resource
    IMqttMessageGateway messageGateway;
    @Resource
    ObjectMapper objectMapper;

    public void publish(String payload) {
        messageGateway.publish(payload);
    }

    public void publish(String topic, String payload) {
        messageGateway.publish(topic, payload);
    }

    public void publish(String topic, Object payload) {
        try {
            messageGateway.publish(topic, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("payload类型异常", e);
        }
    }

    /**
     * 发送消息并等待回复
     * 
     * @param <T>        消息内容类型
     * @param clazz      消息内容类型Class
     * @param topic      主题
     * @param request    请求消息
     * @param retryCount 重试次数
     * @param timeout    超时时间，单位：毫秒
     * @return 如果在超时时间内收到了回复消息，则返回消息内容，否则会重试，重试失败后返回null
     */
    public <T> CommonTopicResponse<T> publishWithReply(Class<T> clazz, String topic, CommonTopicRequest<T> request,
            int retryCount, long timeout) {
        AtomicInteger time = new AtomicInteger(0);
        boolean hasBid = StringUtils.hasText(request.getBid());
        while (time.getAndIncrement() <= retryCount) {
            // 如果用户未指定bid，则需要生成一个唯一的bid
            request.setBid(hasBid ? request.getBid() : UUID.randomUUID().toString());
            this.publish(topic, request);
            // 此处会阻塞等待消息，直到超时或者收到消息
            CommonTopicResponse<?> receiver = Chan.getData(request.getTid(), timeout);
            // 如果超时前收到了消息，则需要匹配 tid 和 bid。
            if (Objects.nonNull(receiver)
                    && receiver.getTid().equals(request.getTid())
                    && receiver.getBid().equals(request.getBid())) {
                return objectMapper.convertValue(receiver,
                        objectMapper.getTypeFactory().constructParametricType(CommonTopicResponse.class, clazz));
            }
            // 如果超时仍未收到消息，则会重试。每次重试时，tid都需要重新生成。
            request.setTid(UUID.randomUUID().toString());
        }
        return null;
    }
}
