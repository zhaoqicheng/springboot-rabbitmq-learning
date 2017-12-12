package com.example.learning.service;

import com.example.learning.common.Constants;
import com.example.learning.encapsulation.MQAccessBuilder;
import com.example.learning.encapsulation.MessageProcess;
import com.example.learning.encapsulation.ThreadPoolConsumer;
import com.example.learning.pojo.User;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by zhaoqicheng on 2017/12/8.
 * <p>
 * 消费者监听rabbit MQ 的消息，从而达到消费消息的目的
 */

@Component
public class TestMQComplexConsumerService {


}
