package com.example.learning.encapsulation;

import com.example.learning.common.Constants;
import com.example.learning.common.DetailRes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by littlersmall on 16/9/5.
 *
 * 发送消息给rabbitMQ 我们需要知道有没有发送成功
 * 对于发送成功或者不成功的解决方式：
 *
 * 1 在本地缓存已发送的message
 * 2 通过confirmCallback或者被确认的ack，将被确认的message从本地删除
 * 3 定时扫描本地的message，如果大于一定时间未被(发送)确认，则重发
 *
 * 重发时间设定：
 *      该重发设定为 3min 如果 3min 后消发送后的消息没有被消费，则重新调用线程重发
 */
@Slf4j
public class RetryCache {
    private MessageSender sender;
    private boolean stop = false;
    private Map<String, MessageWithTime> map = new ConcurrentHashMap<>();

    /**
     * 线程安全的基本数据类型（线程中的计数器）
     */
    private AtomicLong id = new AtomicLong();

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    private static class MessageWithTime {
        long time;
        Object message;
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
        startRetry();
    }

    public String generateId() {
        return "" + id.incrementAndGet();
    }

    public void add(String id, Object message) {
        map.put(id, new MessageWithTime(System.currentTimeMillis(), message));
    }

    public void del(String id) {
        map.remove(id);
    }

    private void startRetry() {
        new Thread(() ->{
            while (!stop) {
                try {
                    Thread.sleep(Constants.RETRY_TIME_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long now = System.currentTimeMillis();

                for (Map.Entry<String, MessageWithTime> entry : map.entrySet()) {
                    MessageWithTime messageWithTime = entry.getValue();

                    if (null != messageWithTime) {
                        if (messageWithTime.getTime() + 3 * Constants.VALID_TIME < now) {
                            log.info("send message failed after 3 min " + messageWithTime);
                            del(entry.getKey());
                        } else if (messageWithTime.getTime() + Constants.VALID_TIME < now) {
                            DetailRes detailRes = sender.send(messageWithTime.getMessage());

                            if (detailRes.isSuccess()) {
                                del(entry.getKey());
                            }
                        }
                    }
                }
            }
        }).start();
    }
}
