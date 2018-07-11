package com.example.learning;

import com.example.learning.common.Constants;
import com.example.learning.common.DetailRes;
import com.example.learning.encapsulation.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.io.IOException;


/**
 * 概念以及要义
 * https://www.cnblogs.com/diegodu/p/4971586.html
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@SuppressWarnings(value = "all")
public class SpringbootRabbitmqLearningApplicationTests {

    @Test
    public void contextLoads() {
    }

    /**
     * EXCHANGE: 指定交换机，由交换机将消息分发至各个通道
     * ROUTING: routing key，来指定这个消息的路由规则，routing key需要与Exchange Type及binding key联合使用才能最终生效。
     * QUEUE: 指定通道  (通道中：生产者负责向通道中添加数据、消费者负责从通道中消费数据，通道是生产者负责声明的)
     */
    private static final String EXCHANGE = "example";
    private static final String ROUTING = "user-example";
    private static final String QUEUE = "user-example";

    @Autowired
    ConnectionFactory connectionFactory;

    private MessageSender messageSender;

    private MessageConsumer messageConsumer;

    private ThreadPoolConsumer<UserMessage> threadPoolConsumer;

    /**
     * 生产者测试
     * <p>
     */
    @Test
    @PostConstruct
    public void senderExample() {
        UserMessage userMessage = new UserMessage();
        userMessage.setId(1);
        userMessage.setName("赵启成");
        MQAccessBuilder mqAccessBuilder = new MQAccessBuilder(connectionFactory);
        try {
            //"direct"
            messageSender = mqAccessBuilder.buildMessageSender(EXCHANGE, ROUTING, QUEUE);
            System.out.print("向通道中发送完成的结果为" + messageSender.send(userMessage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 消费者测试
     * <p>
     */
    @Test
    @PostConstruct
    public void consumeExample() {
        MQAccessBuilder mqAccessBuilder = new MQAccessBuilder(connectionFactory);
        try {
            messageConsumer = mqAccessBuilder.buildMessageConsumer(EXCHANGE, ROUTING, QUEUE, new UserMessageProcess());
            System.out.print("消费通道中的消息结果为" + messageConsumer.consume());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用线程来向通道中发送消息
     *
     * @throws IOException
     */
    @Test
    @PostConstruct
    public void sendByPoolExample() throws IOException {

        MQAccessBuilder mqAccessBuilder = new MQAccessBuilder(connectionFactory);
        //"direct"
        messageSender = mqAccessBuilder.buildMessageSender(EXCHANGE, ROUTING, QUEUE);

        new Thread(new Runnable() {
            int id = 0;

            @Override
            public void run() {
                while (true) {
                    DetailRes send = messageSender.send(new UserMessage(id++, "" + System.nanoTime()));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 使用线程来消费   通道中的消息
     * <p>
     * 消费者  使用线程消费通道中的消息
     * <p>
     * 被@PreConstruct修饰的方法会在服务器卸载Servlet的时候运行，
     * 并且只会被服务器调用一次，类似于Servlet的destroy()方法。
     * 被@PreConstruct修饰的方法会在destroy()方法之后运行，在Servlet被彻底卸载之前。
     *
     * 在实际的使用过程中，消费者通常作为init方法配合@PostConstruct 注解一起使用，无时无刻的消费通道中的消息；
     */
    @Test
    @PostConstruct
    public void consumeByPoolExample() throws IOException {

        MQAccessBuilder mqAccessBuilder = new MQAccessBuilder(connectionFactory);
        MessageProcess<UserMessage> messageProcess = new UserMessageProcess();
        threadPoolConsumer = new ThreadPoolConsumer.ThreadPoolConsumerBuilder<UserMessage>()
                .setThreadCount(Constants.THREAD_COUNT).setIntervalMils(Constants.INTERVAL_MILS)
                .setExchange(EXCHANGE).setRoutingKey(ROUTING).setQueue(QUEUE)
                .setMQAccessBuilder(mqAccessBuilder).setMessageProcess(messageProcess)
                .build();
        threadPoolConsumer.start();
    }
}
