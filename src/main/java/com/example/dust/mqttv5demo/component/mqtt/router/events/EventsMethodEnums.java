package com.example.dust.mqttv5demo.component.mqtt.router.events;

import com.example.dust.mqttv5demo.common.model.AirsenseWarning;
import com.example.dust.mqttv5demo.component.mqtt.MqttChannels;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedHashMap;

@Getter
public enum EventsMethodEnums {

    AIRSENSE_WARNING("airsense_warning", MqttChannels.INBOUND_EVENTS_AIRSENESE_WARNING, AirsenseWarning[].class),
    UNKNOWN("unknown", MqttChannels.INBOUND_EVENTS_DEFAULT, LinkedHashMap.class);

    final String method;
    final String channel;
    final Class<?> clazz;

    EventsMethodEnums(String method, String channel, Class<?> clazz) {
        this.method = method;
        this.channel = channel;
        this.clazz = clazz;
    }

    public static EventsMethodEnums find(String method) {
        return Arrays.stream(EventsMethodEnums.values())
                .filter(e -> e.getMethod().equals(method))
                .findFirst()
                .orElse(UNKNOWN);
    }

}
