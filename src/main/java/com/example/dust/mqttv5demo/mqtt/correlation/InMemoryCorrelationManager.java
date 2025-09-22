package com.example.dust.mqttv5demo.mqtt.correlation;

import com.example.dust.mqttv5demo.mqtt.defaults.CommonTopicResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 本地 JVM 级别的相关性管理实现：
 * - 仅支持当前实例（不跨节点）
 * - 使用 ConcurrentHashMap 存储 tid -> CompletableFuture
 * - 注册时可设置超时，超时后 future 以 CorrelationTimeoutException 异常完成并移除
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "correlation.redis", name = "enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryCorrelationManager implements CorrelationManager {

    private final Map<String, CompletableFuture<CommonTopicResponse<?>>> futures = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "corr-inmem-timeout");
        t.setDaemon(true);
        return t;
    });

    @Override
    public CompletableFuture<CommonTopicResponse<?>> register(String tid, long timeoutMillis) {
        if (tid == null)
            throw new IllegalArgumentException("tid不能为null");
        CompletableFuture<CommonTopicResponse<?>> future = futures.computeIfAbsent(tid, k -> new CompletableFuture<>());
        if (timeoutMillis > 0)
            scheduler.schedule(() -> timeoutFuture(tid, future), timeoutMillis, TimeUnit.MILLISECONDS);
        return future;
    }

    private void timeoutFuture(String tid, CompletableFuture<CommonTopicResponse<?>> f) {
        if (!f.isDone()) {
            futures.remove(tid, f);
            f.completeExceptionally(new CorrelationTimeoutException(tid));
            log.debug("等待(InMemory)回复超时 tid={}", tid);
        }
    }

    @Override
    public void complete(String tid, CommonTopicResponse<?> response) {
        if (tid == null)
            return;
        CompletableFuture<CommonTopicResponse<?>> f = futures.remove(tid);
        if (f != null && !f.isDone()) {
            f.complete(response);
        } else {
            log.trace("没有等待的future, tid={} (可能已超时)", tid);
        }
    }

    @Override
    public void remove(String tid) {
        futures.remove(tid);
    }
}
