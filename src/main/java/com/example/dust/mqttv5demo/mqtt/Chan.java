package com.example.dust.mqttv5demo.mqtt;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.LockSupport;

import com.example.dust.mqttv5demo.mqtt.defaults.CommonTopicResponse;

/**
 * 用于同步等待消息
 */
public class Chan {

    private static final ConcurrentHashMap<String, Chan> CHANNEL = new ConcurrentHashMap<>();
    private static final int UNIT = 1000_000;
    private volatile CommonTopicResponse<?> data;
    private volatile Thread t;

    /**
     * 获取消息，如果超时则返回null，否则返回消息内容
     * 调用时机：平台向设备发送消息（如property/set、service等），此时需要阻塞线程，直到其他方法需调用{@link #putData(String, CommonTopicResponse)}方法提供返回值并唤醒线程，或者超时。
     * 
     * @param tid             事务ID
     * @param timeoutInMillis 超时时间，单位：毫秒
     * @return
     */
    public static CommonTopicResponse<?> getData(String tid, long timeoutInMillis) {
        Chan chan = new Chan();
        chan.t = Thread.currentThread();
        CHANNEL.put(tid, chan);
        LockSupport.parkNanos(chan.t, timeoutInMillis * UNIT);
        chan.t = null;
        CHANNEL.remove(tid);
        return chan.data;
    }

    /**
     * 放入消息，如果消息等待线程存在，则唤醒
     * 调用时机：平台向接收到设备响应的消息（如property/set_reply、service_reply等），此时需要唤醒线程。唤醒后{@link #getData(String, long)}方法会返回消息内容。
     * 
     * @param tid      事务ID
     * @param response 消息内容
     */
    public static void putData(String tid, CommonTopicResponse<?> response) {
        Chan chan = CHANNEL.get(tid);
        if (Objects.isNull(chan))
            return;
        chan.data = response;
        if (chan.t == null)
            return;
        LockSupport.unpark(chan.t);
    }

}
