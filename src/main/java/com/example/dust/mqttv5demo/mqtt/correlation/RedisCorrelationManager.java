package com.example.dust.mqttv5demo.mqtt.correlation;

import com.example.dust.mqttv5demo.mqtt.defaults.CommonTopicResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 基于Redis的相关性管理实现：
 * - 支持集群跨节点
 * - 使用Redis的pub/sub机制进行回复通知。
 * - 使用带有TTL的Redis字符串键来跟踪待处理的请求。
 * - 支持请求超时处理。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "correlation.redis", name = "enabled", havingValue = "true")
public class RedisCorrelationManager implements CorrelationManager {

    @Value("${correlation.redis.channel}")
    String channel;
    @Value("${correlation.redis.key-prefix}")
    String keyPrefix;
    @Value("${correlation.redis.default-ttl-millis:30000}")
    long defaultTtlMillis;

    @Resource
    StringRedisTemplate redis;
    @Resource
    ObjectMapper objectMapper;
    @Resource
    @Lazy
    RedisMessageListenerContainer listenerContainer;

    private final Map<String, CompletableFuture<CommonTopicResponse<?>>> futures = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "corr-redis-timeout");
        t.setDaemon(true);
        return t;
    });
    private final MessageListener messageListener = new MessageListener() {
        @Override
        public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
            String body = new String(message.getBody(), StandardCharsets.UTF_8);
            try {
                Msg msg = objectMapper.readValue(body, Msg.class);
                if (msg != null && msg.tid != null) {
                    CompletableFuture<CommonTopicResponse<?>> f = futures.remove(msg.tid);
                    if (f != null && !f.isDone()) {
                        log.debug("处理Redis pub/sub消息成功,tid={},msg={}", msg.tid, msg.response);
                        f.complete(msg.response);
                    }
                }
            } catch (Exception e) {
                log.warn("处理Redis pub/sub消息失败={}", body, e);
            }
        }
    };

    @PostConstruct
    public void init() {
        try {
            listenerContainer.addMessageListener(messageListener, new ChannelTopic(channel));
            log.info("正在监听Redis pub/sub消息, channel={} keyPrefix={} ttl={}ms", channel, keyPrefix,
                    defaultTtlMillis);
        } catch (Exception e) {
            log.error("注册Redis pub/sub消息监听失败, channel={}", channel, e);
        }
    }

    @Override
    public CompletableFuture<CommonTopicResponse<?>> register(String tid, long timeoutMillis) {
        if (tid == null)
            throw new IllegalArgumentException("tid不能为null");
        long ttl = timeoutMillis > 0 ? timeoutMillis : defaultTtlMillis;
        return futures.computeIfAbsent(tid, k -> {
            CompletableFuture<CommonTopicResponse<?>> nf = new CompletableFuture<>();
            try {
                redis.opsForValue().set(keyPrefix + tid, "1", Duration.ofMillis(ttl));
            } catch (DataAccessException e) {
                log.warn("设置Redis键失败, tid={}", tid, e);
            }
            if (ttl > 0)
                scheduler.schedule(() -> timeoutFuture(tid, nf), ttl, TimeUnit.MILLISECONDS);
            return nf;
        });
    }

    private void timeoutFuture(String tid, CompletableFuture<CommonTopicResponse<?>> f) {
        if (!f.isDone()) {
            futures.remove(tid, f);
            f.completeExceptionally(new CorrelationTimeoutException(tid));
            log.debug("等待(redis)回复超时 tid={}", tid);
        }
    }

    @Override
    public void complete(String tid, CommonTopicResponse<?> response) {
        if (tid == null)
            return;
        CompletableFuture<CommonTopicResponse<?>> f = futures.remove(tid);
        if (f != null && !f.isDone()) {
            f.complete(response);
        }
        try {
            String json = objectMapper.writeValueAsString(new Msg(tid, response));
            redis.convertAndSend(channel, json);
        } catch (JsonProcessingException e) {
            log.error("序列化响应消息失败,id={}", tid, e);
        } catch (Exception ex) {
            log.error("Redis发布响应消息失败,tid={} channel={}", tid, channel, ex);
        }
    }

    @Override
    public void remove(String tid) {
        futures.remove(tid);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Msg {
        private String tid;
        private CommonTopicResponse<?> response;
    }

}
