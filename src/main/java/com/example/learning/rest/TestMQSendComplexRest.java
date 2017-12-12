package com.example.learning.rest;

import com.example.learning.common.Constants;
import com.example.learning.encapsulation.*;
import com.example.learning.pojo.User;
import com.example.learning.service.UserProcess;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by zhaoqicheng on 2017/12/9.
 */
@RestController
@RequestMapping(value = "testMQRestComplex")
public class TestMQSendComplexRest {

    @Autowired
    ConnectionFactory connectionFactory;

//    @Autowired
//    private UserProcess userProcess;

    private MessageSender messageSender;

    private ThreadPoolConsumer<User> threadPoolConsumer;

    /**
     * EXCHANGE: 指定交换机，由交换机将消息分发至各个通道
     * ROUTING: routing key，来指定这个消息的路由规则，routing key需要与Exchange Type及binding key联合使用才能最终生效。
     * QUEUE: 指定通道
     * <p>
     * 补充：之前整理这块代码的时候，由于没有完全看明白routing Key和Binding key，无法理解到内部机制。再次提醒自己，看代码之前弄懂他们的含义
     */
    private static final String EXCHANGE = "complex.exchange";
    private static final String ROUTING = "user-complex";
    private static final String QUEUE = "user-complex";

    /**
     * 生产者测试
     * <p>
     * <p>
     * 会产生一个名字为example.exchange 的 Exchanges 并产生一个名为 user-example 的通道
     */
    @PostMapping(value = "createMQByComplex")
    public void senderExample() {

        User user = new User();
        user.setId(2);
        user.setName("王明-2");
        user.setTelephone("2222222");
        MQAccessBuilder mqAccessBuilder = new MQAccessBuilder(connectionFactory);
        try {
            //"direct"
            messageSender = mqAccessBuilder.buildMessageSender(EXCHANGE, ROUTING, QUEUE);
            System.out.print("向通道中发送完成的结果为" + messageSender.send(user));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 消费者  使用线程消费通道中的消息
     * 被@PreConstruct修饰的方法会在服务器卸载Servlet的时候运行，
     * 并且只会被服务器调用一次，类似于Servlet的destroy()方法。
     * 被@PreConstruct修饰的方法会在destroy()方法之后运行，在Servlet被彻底卸载之前。
     */
    @PostConstruct
    public void init() {
        try {
            System.out.println("我被实例化啦！！！！");
            MQAccessBuilder mqAccessBuilder = new MQAccessBuilder(connectionFactory);

            /**
             * 这里不能new  要采用注入的方式
             */
            MessageProcess<User> reportLogProcess = new UserProcess();
            threadPoolConsumer = new ThreadPoolConsumer.ThreadPoolConsumerBuilder<User>()
                    .setThreadCount(Constants.THREAD_COUNT).setIntervalMils(Constants.INTERVAL_MILS)
                    .setExchange(EXCHANGE).setRoutingKey(ROUTING).setQueue(QUEUE)
                    .setMQAccessBuilder(mqAccessBuilder).setMessageProcess(reportLogProcess)
                    .build();
            threadPoolConsumer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
