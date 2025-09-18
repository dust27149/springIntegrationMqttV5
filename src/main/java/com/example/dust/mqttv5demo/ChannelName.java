package com.example.dust.mqttv5demo;

/**
 * 通道名称
 */
public class ChannelName {

    // 入站通道，接收MQTT消息
    public static final String INBOUND = "mqttInboundChannel";
    // 中间通道，做业务处理。中间通道的数量可根据业务需求调整
    public static final String BOUND = "mqttBoundChannel";
    // 出站通道，发送MQTT消息
    public static final String OUTBOUND = "mqttOutboundChannel";

}
