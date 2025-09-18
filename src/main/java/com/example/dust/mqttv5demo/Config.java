package com.example.dust.mqttv5demo;

import java.util.Random;

/**
 * 配置类，实际项目建议写在nacos中
 */
public class Config {
    // MQTT相关配置
    // MQTT连接地址
    public static final String BROKER = "tcp://localhost:1883";
    // MQTT客户端ID，需唯一
    public static final String CLIENT_ID = "mqtt";
    // 订阅者ID，需唯一
    public static final String SUB = CLIENT_ID + "_sub" + new Random().nextInt(100);
    // 发布者ID，需唯一
    public static final String PUB = CLIENT_ID + "_pub" + new Random().nextInt(100);
    // MQTT用户名密码
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    // 订阅主题。支持通配符。
    public static final String[] TOPIC_TO_SUB = { "topic/to/subcribe", "topic/to/subcribe2" };
    // 发布主题
    public static final String TOPIC_TO_PUB = "topic/to/publish";
    // 共享订阅主题，格式"$share/{groupID}/topic"。groupID可自定义,同一个groupID下的客户端会均摊消息
    public static final String SHARED_GROUP = "$share/groupId/";

    // 线程池配置
    // 核心线程数
    public static final int CORE_POOL_SIZE = 20;
    // 最大线程数
    public static final int MAX_POOL_SIZE = 200;
    // 线程存活时间
    public static final int KEEP_ALIVE = 120;
    // 任务队列容量
    public static final int QUEUE_CAPACITY = 2000;
}
