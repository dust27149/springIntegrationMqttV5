package com.example.dust.mqttv5demo.component.mqtt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * mqtt通道配置
 */
@Configuration
public class MqttChannels {

    @Value("${mqtt.inbound-thread.core-pool-size:10}")
    private int corePoolSize;
    @Value("${mqtt.inbound-thread.max-pool-size:20}")
    private int maxPoolSize;
    @Value("${mqtt.inbound-thread.keep-alive:60}")
    private int keepAlive;
    @Value("${mqtt.inbound-thread.queue-capacity:1000}")
    private int queueCapacity;

    // 入站通道，接收MQTT消息
    public static final String INBOUND = "inbound";
    public static final String INBOUND_DEFAULT = "inbound_default";

    // 出站通道，发送MQTT消息
    public static final String OUTBOUND = "outbound";
    public static final String OUTBOUND_DEFAULT = "outbound_default";

    // 业务通道，做业务处理
    // OSD通道，接收设备上报的在线状态数据
    public static final String INBOUND_OSD = "inbound_osd";
    // state通道，接收设备上报的属性状态数据并回复
    public static final String INBOUND_STATE = "inbound_state";
    public static final String OUTBOUND_STATE_REPLY = "outbound_state_reply";
    // services通道，调用设备能力，接收设备回复
    public static final String OUTBOUND_SERVICES = "outbound_services";
    public static final String INBOUND_SERVICES_REPLY = "inbound_services_reply";
    // events通道，接收设备上报的事件数据并回复
    public static final String INBOUND_EVENTS = "inbound_events";
    public static final String INBOUND_EVENTS_DEFAULT = "inbound_events_default";
    public static final String INBOUND_EVENTS_AIRSENESE_WARNING = "inbound_events_airsenese_warning";
    public static final String OUTBOUND_EVENTS_REPLY = "outbound_events_reply";
    // requests通道，接收设备上报的请求数据并回复
    public static final String INBOUND_REQUESTS = "inbound_requests";
    public static final String INBOUND_REQUESTS_DEFAULT = "inbound_requests_default";
    public static final String INBOUND_REQUESTS_AIRPORT_BIND_STATUS = "inbound_requests_airport_bind_status";
    public static final String INBOUND_REQUESTS_AIRPORT_ORGANIZATION_GET = "inbound_requests_airport_organization_get";
    public static final String INBOUND_REQUESTS_AIRPORT_ORGANIZATION_BIND = "inbound_requests_airport_organization_bind";
    public static final String INBOUND_REQUESTS_CONFIG = "inbound_requests_config";
    public static final String INBOUND_REQUESTS_STORAGE_CONFIG_GET = "inbound_requests_storage_config_get";
    public static final String OUTBOUND_REQUESTS_REPLY = "outbound_requests_reply";
    // status通道，接收设备上报的系统状态数据并回复
    public static final String INBOUND_STATUS = "inbound_status";
    public static final String OUTBOUND_STATUS_REPLY = "outbound_status_reply";
    // property通道，设置设备属性，接收设备回复
    public static final String OUTBOUND_PROPERTY_SET = "outbound_property_set";
    public static final String INBOUND_PROPERTY_SET_REPLY = "inbound_property_set_reply";
    // DRC通道，接收设备上报的DRC数据和发送DRC控制命令
    public static final String INBOUND_DRC_UP = "inbound_drc_up";
    public static final String OUTBOUND_DRC_DOWN = "outbound_drc_down";

    @Bean(name = MqttChannels.INBOUND)
    public MessageChannel inboundChannel() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                keepAlive, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity),
                new CustomizableThreadFactory("inboundThreadPool-"), new ThreadPoolExecutor.AbortPolicy());
        return new ExecutorChannel(executor);
    }

    @Bean(name = MqttChannels.INBOUND_OSD)
    public MessageChannel inboundOSDChannel() {
        return new DirectChannel();
    }

    @Bean(name = MqttChannels.INBOUND_STATE)
    public MessageChannel inboundStateChannel() {
        return new DirectChannel();
    }

    @Bean(name = MqttChannels.INBOUND_SERVICES_REPLY)
    public MessageChannel inboundServicesReplyChannel() {
        return new DirectChannel();
    }

    @Bean(name = MqttChannels.INBOUND_EVENTS)
    public MessageChannel inboundEventsChannel() {
        return new DirectChannel();
    }

    @Bean(name = MqttChannels.INBOUND_REQUESTS)
    public MessageChannel inboundRequestsChannel() {
        return new DirectChannel();
    }

    @Bean(name = MqttChannels.INBOUND_STATUS)
    public MessageChannel inboundStatusChannel() {
        return new DirectChannel();
    }

    @Bean(name = MqttChannels.INBOUND_PROPERTY_SET_REPLY)
    public MessageChannel inboundPropertySetReplyChannel() {
        return new DirectChannel();
    }

    @Bean(name = MqttChannels.INBOUND_DRC_UP)
    public MessageChannel inboundDrcUpChannel() {
        return new DirectChannel();
    }

    @Bean(name = MqttChannels.INBOUND_DEFAULT)
    public MessageChannel inboundDefaultChannel() {
        return new DirectChannel();
    }
}
