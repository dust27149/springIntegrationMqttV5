package com.example.dust.mqttv5demo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Provides a RedisMessageListenerContainer bean only when distributed correlation (redis) is enabled.
 */
@Configuration
@ConditionalOnProperty(prefix = "correlation.redis", name = "enabled", havingValue = "true")
public class RedisListenerContainerConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // Additional tuning (thread pool etc.) can be added here if needed.
        return container;
    }
}
