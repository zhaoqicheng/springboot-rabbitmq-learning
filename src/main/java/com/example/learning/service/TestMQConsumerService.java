package com.example.learning.service;

import com.example.learning.pojo.User;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Created by zhaoqicheng on 2017/12/8.
 * <p>
 * 消费者监听rabbit MQ 的消息，从而达到消费消息的目的
 */

@Component
public class TestMQConsumerService {

    @RabbitListener(queues = "user-simple")
    @RabbitHandler
    public void processSimple_1(User message) {
        System.out.println("消费者一消费消息，接收到的消息为：" + message.toString());

    }

    @RabbitListener(queues = "user-simple")
    @RabbitHandler
    public void processSimple_2(User message) {
        System.out.println("消费者二消费消息，接收到的消息为：" + message.toString());

    }

    @RabbitListener(queues = "topic.message")
    @RabbitHandler
    public void processSimple_3(String msg) {
        System.out.println("topicMessagesReceiver  : " +msg);
    }

    @RabbitListener(queues = "topic.messages")
    @RabbitHandler
    public void processSimple_4(String msg) {
        System.out.println("topicMessagesReceiver  : " +msg);
    }

    @RabbitListener(queues = "fanout.A")
    @RabbitHandler
    public void processSimple_5(String msg) {
        System.out.println("topicMessagesReceiver  : " +msg);
    }

    @RabbitListener(queues = "fanout.B")
    @RabbitHandler
    public void processSimple_6(String msg) {
        System.out.println("topicMessagesReceiver  : " +msg);
    }

    @RabbitListener(queues = "fanout.C")
    @RabbitHandler
    public void processSimple_7(String msg) {
        System.out.println("topicMessagesReceiver  : " +msg);
    }

}
