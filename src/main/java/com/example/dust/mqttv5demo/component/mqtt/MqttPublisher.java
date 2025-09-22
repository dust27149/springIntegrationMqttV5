package com.example.dust.mqttv5demo.component.mqtt;

import com.alibaba.fastjson.JSON;
import com.example.dust.mqttv5demo.component.mqtt.replyTracker.ReplyTimeoutException;
import com.example.dust.mqttv5demo.component.mqtt.replyTracker.ReplyTracker;
import com.example.dust.mqttv5demo.component.mqtt.router.defaults.CommonTopicRequest;
import com.example.dust.mqttv5demo.component.mqtt.router.defaults.CommonTopicResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Component
public class MqttPublisher {

    @Resource
    IMqttMessageGateway messageGateway;
    @Resource
    ObjectMapper objectMapper;
    @Resource
    ReplyTracker replyTracker;

    public void publish(String payload) {
        messageGateway.publish(payload);
    }

    public void publish(String topic, String payload) {
        messageGateway.publish(topic, payload);
    }

    /**
     * 发送并等待回复（阻塞），内部委托给异步方法后 join。
     * 
     * @param topic      主题
     * @param request    请求数据
     * @param clazz      响应数据类型
     * @param retryCount 重试次数（额外发送次数 = retryCount，尝试总次数 = retryCount+1）
     * @param timeout    超时时间（毫秒）
     */
    private <T> CommonTopicResponse<T> publishWithReply(String topic, CommonTopicRequest<T> request, Class<T> clazz,
            int retryCount, long timeout) {
        try {
            return publishWithReplyAsync(topic, request, clazz, retryCount, timeout).join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime; // 保持原始运行时异常（如 ReplyTimeoutException）
            }
            throw new RuntimeException(cause);
        }
    }

    /**
     * 异步发送并等待回复（返回 CompletableFuture, 不阻塞调用线程）。
     * 
     * @param topic      主题
     * @param request    请求数据
     * @param clazz      响应数据类型
     * @param retryCount 重试次数（额外发送次数 = retryCount，尝试总次数 = retryCount+1）
     * @param timeout    超时时间（毫秒）
     * 
     */
    private <T> CompletableFuture<CommonTopicResponse<T>> publishWithReplyAsync(String topic,
            CommonTopicRequest<T> request, Class<T> clazz, int retryCount, long timeout) {
        final boolean userProvidedBid = StringUtils.hasText(request.getBid());
        if (!userProvidedBid) {
            request.setBid(UUID.randomUUID().toString());
        }
        int attempts = retryCount + 1;
        CompletableFuture<CommonTopicResponse<T>> overall = new CompletableFuture<>();

        attemptSend(clazz, topic, request, userProvidedBid, attempts, 1, timeout, overall);
        return overall;
    }

    private <T> void attemptSend(Class<T> clazz, String topic, CommonTopicRequest<T> request, boolean userProvidedBid,
            int totalAttempts, int currentAttempt, long timeout, CompletableFuture<CommonTopicResponse<T>> overall) {
        if (overall.isDone()) {
            return; // 已完成则不再继续
        }
        request.setTid(UUID.randomUUID().toString());
        if (!userProvidedBid && currentAttempt > 1) {
            request.setBid(UUID.randomUUID().toString());
        }
        CompletableFuture<CommonTopicResponse<?>> future = replyTracker.register(request.getTid(), timeout);
        // 发送
        this.publish(topic, JSON.toJSONString(request));
        // 链接 future
        future.whenComplete((resp, ex) -> {
            if (overall.isDone())
                return; // 已完成则不再继续
            if (ex != null) {
                Throwable cause = (ex instanceof CompletionException) ? ex.getCause() : ex;
                // 超时：若还有剩余尝试则重试，否则抛出
                if (cause instanceof ReplyTimeoutException) {
                    if (currentAttempt < totalAttempts) {
                        attemptSend(clazz, topic, request, userProvidedBid, totalAttempts, currentAttempt + 1, timeout,
                                overall);
                    } else {
                        overall.completeExceptionally(cause);
                    }
                } else {
                    // 其他异常直接结束
                    overall.completeExceptionally(cause);
                }
                return;
            }

            // ex == null
            if (resp == null) {
                // 未得到响应（可能被取消等），按失败逻辑处理
                if (currentAttempt < totalAttempts) {
                    attemptSend(clazz, topic, request, userProvidedBid, totalAttempts, currentAttempt + 1, timeout,
                            overall);
                } else {
                    overall.completeExceptionally(
                            new IllegalStateException("No response after attempts: " + totalAttempts));
                }
                return;
            }

            // 校验 tid/bid
            if (!request.getTid().equals(resp.getTid()) || !request.getBid().equals(resp.getBid())) {
                if (currentAttempt < totalAttempts) {
                    attemptSend(clazz, topic, request, userProvidedBid, totalAttempts, currentAttempt + 1, timeout,
                            overall);
                } else {
                    overall.completeExceptionally(
                            new IllegalStateException("Mismatched replyTracker data after attempts: " + totalAttempts));
                }
                return;
            }

            // 成功
            CommonTopicResponse<T> casted = objectMapper.convertValue(resp, objectMapper.getTypeFactory()
                    .constructParametricType(CommonTopicResponse.class, clazz));
            overall.complete(casted);
        });
    }
}
