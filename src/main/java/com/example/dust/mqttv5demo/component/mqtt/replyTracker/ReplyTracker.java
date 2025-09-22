package com.example.dust.mqttv5demo.component.mqtt.replyTracker;

import com.example.dust.mqttv5demo.component.mqtt.router.defaults.CommonTopicResponse;

import java.util.concurrent.CompletableFuture;

/**
 * 跟踪待处理的回复（tid -> future），并允许完成或移除它们。
 */
public interface ReplyTracker {
    /**
     * 注册一个新的相关ID，并返回一个future以等待其响应。
     *
     * @param tid           事务ID
     * @param timeoutMillis 超时时间（毫秒），如果小于等于0则表示不设置超时
     * @return 等待回复的future
     */
    CompletableFuture<CommonTopicResponse<?>> register(String tid, long timeoutMillis);

    /**
     * 完成给定相关ID的future（如果存在的话）。
     *
     * @param tid      事务ID
     * @param response 回复内容
     */
    void complete(String tid, CommonTopicResponse<?> response);

    /**
     * 如果需要，移除相关性（例如，在手动取消或超时清理之后）。
     *
     * @param tid 事务ID
     */
    void remove(String tid);
}
