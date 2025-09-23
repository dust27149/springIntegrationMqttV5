package com.example.dust.mqttv5demo.component.mqtt.router.requests;

import com.example.dust.mqttv5demo.component.mqtt.MqttChannels;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedHashMap;

@Getter
public enum RequestsMethodEnums {

    UNKNOWN("unknown", MqttChannels.INBOUND_REQUESTS_DEFAULT, LinkedHashMap.class);

    final String method;
    final String channel;
    final Class<?> clazz;

    RequestsMethodEnums(String method, String channel, Class<?> clazz) {
        this.method = method;
        this.channel = channel;
        this.clazz = clazz;
    }

    public static RequestsMethodEnums find(String method) {
        return Arrays.stream(RequestsMethodEnums.values())
                .filter(e -> e.getMethod().equals(method))
                .findFirst()
                .orElse(UNKNOWN);
    }

}
