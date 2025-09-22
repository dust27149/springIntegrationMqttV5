package com.example.dust.mqttv5demo.component.mqtt;

import lombok.Getter;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * DJI CloudAPI主题枚举，配合{@link MqttMessageRouter}匹配不同的主题并路由到相应的处理器
 */
@Getter
public enum MqttMessageEnums {

    OSD(Pattern.compile("thing/product/[A-Za-z0-9]+/osd"), MqttChannels.INBOUND_OSD),
    STATE(Pattern.compile("thing/product/[A-Za-z0-9]+/state"), MqttChannels.INBOUND_STATE),
    SERVICES_REPLY(Pattern.compile("thing/product/[A-Za-z0-9]+/services_reply"), MqttChannels.INBOUND_SERVICES_REPLY),
    EVENTS(Pattern.compile("thing/product/[A-Za-z0-9]+/events"), MqttChannels.INBOUND_EVENTS),
    REQUESTS(Pattern.compile("thing/product/[A-Za-z0-9]+/requests"), MqttChannels.INBOUND_REQUESTS),
    STATUS(Pattern.compile("sys/product/[A-Za-z0-9]+/status"), MqttChannels.INBOUND_STATUS),
    PROPERTY_SET_REPLY(Pattern.compile("thing/product/[A-Za-z0-9]+/property/set_reply"), MqttChannels.INBOUND_PROPERTY_SET_REPLY),
    DRC_UP(Pattern.compile("thing/product/[A-Za-z0-9]+/drc/up"), MqttChannels.INBOUND_DRC_UP),
    UNKNOWN(Pattern.compile("^.*$"), MqttChannels.INBOUND_DEFAULT);

    final Pattern pattern;
    final String beanName;

    MqttMessageEnums(Pattern pattern, String beanName) {
        this.pattern = pattern;
        this.beanName = beanName;
    }

    /**
     * 根据主题查找枚举
     *
     * @param topic 主题
     * @return 枚举
     */
    public static MqttMessageEnums find(String topic) {
        return Arrays.stream(values())
                .filter(e -> e.pattern.matcher(topic).matches())
                .findFirst()
                .orElse(UNKNOWN);
    }

}
