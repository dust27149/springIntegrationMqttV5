package com.example.dust.mqttv5demo.mqtt.correlation;

import com.example.dust.mqttv5demo.mqtt.defaults.CommonTopicResponse;

import java.util.concurrent.CompletableFuture;

/**
 * 抽象类，用于在集群中注册和完成基于相关性的（tid）回复。
 */
public interface CorrelationManager {

    /**
     * 注册一个新的相关ID，并返回一个future以等待其响应。
     * 如果已经存在，则返回现有的future（幂等注册）。
     */
    CompletableFuture<CommonTopicResponse<?>> register(String tid, long timeoutMillis);

    /**
     * 完成给定相关ID的future（如果存在的话）。
     */
    void complete(String tid, CommonTopicResponse<?> response);

    /**
     * 如果需要，移除相关性（例如，在手动取消或超时清理之后）。
     */
    void remove(String tid);
}
