package com.example.learning.service;

import com.example.learning.pojo.User;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Created by zhaoqicheng on 2017/12/8.
 * <p>
 * 消费者监听rabbit MQ 的消息，从而达到消费消息的目的
 */

@Component
public class TestMQCompleConsumerService {

}
