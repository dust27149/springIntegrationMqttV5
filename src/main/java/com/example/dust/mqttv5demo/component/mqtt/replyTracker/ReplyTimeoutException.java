package com.example.dust.mqttv5demo.component.mqtt.replyTracker;

import lombok.Getter;

/**
 * 在规定时间内未收到回复时抛出此异常。
 */
@Getter
public class ReplyTimeoutException extends RuntimeException {

    private final String tid;

    public ReplyTimeoutException(String tid) {
        super("Reply timeout tid=" + tid);
        this.tid = tid;
    }
}
