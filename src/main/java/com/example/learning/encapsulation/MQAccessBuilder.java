package com.example.learning.encapsulation;

import com.example.learning.common.Constants;
import com.example.learning.common.DetailRes;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;
import org.springframework.amqp.rabbit.support.MessagePropertiesConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by zhaoqicheng on 16/5/11.
 *
 * 参考：
 *  感谢：
 *  代码来源：
 *  http://www.jianshu.com/p/4112d78a8753
 *  http://www.jianshu.com/p/6579e48d18ae
 *
 *  rabbitMQ的基础定义：
 *  https://www.cnblogs.com/diegodu/p/4971586.html
 *
 *  rabbitMQ的其他困惑，方法调用规则：
 *  http://blog.csdn.net/revivedsun/article/details/53055250
 *  http://blog.csdn.net/omaverick1/article/details/52330068
 *
 */
@Slf4j
public class MQAccessBuilder {

    private ConnectionFactory connectionFactory;

    /**
     * 构造方法
     */
    public MQAccessBuilder(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * 构造方法一  使用direct作为路由方式，它会把消息路由到那些binding key与routing key完全匹配的Queue中。
     *
     * @param exchange
     * @param routingKey
     * @param queue
     * @return
     * @throws IOException
     */
    public MessageSender buildMessageSender(final String exchange, final String routingKey, final String queue) throws IOException {
        return buildMessageSender(exchange, routingKey, queue, "direct");
    }

    /**
     * 构造方法二  使用topic作为路由方式
     * <p>
     * routing key为一个句点号“. ”分隔的字符串（我们将被句点号“. ”分隔开的每一段独立的字符串称为一个单词），如“stock.usd.nyse”、“nyse.vmw”、“quick.orange.rabbit”
     * binding key与routing key一样也是句点号“. ”分隔的字符串
     * binding key中可以存在两种特殊字符“*”与“#”，用于做模糊匹配，其中“*”用于匹配一个单词，“#”用于匹配多个单词（可以是零个）
     *
     * @param exchange
     * @param routingKey
     * @return
     * @throws IOException
     */
    public MessageSender buildTopicMessageSender(final String exchange, final String routingKey) throws IOException {
        return buildMessageSender(exchange, routingKey, null, "topic");
    }


    /**
     * 生产者：
     * <p>
     * 1 构造template, exchange, routingkey等
     * 2 设置message序列化方法
     * 3 设置发送确认
     * 4 构造sender方法
     *
     * @param exchange
     * @param routingKey
     * @param queue
     * @param type
     * @return
     * @throws IOException
     */
    public MessageSender buildMessageSender(final String exchange, final String routingKey,
                                            final String queue, final String type) throws IOException {
        Connection connection = connectionFactory.createConnection();
        //1
        if (type.equals("direct")) {
            buildQueue(exchange, routingKey, queue, connection, "direct");
        } else if (type.equals("topic")) {
            buildTopic(exchange, connection);
        }

        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        /**
         * 当mandatory标志位设置为true时，如果exchange根据自身类型和消息routeKey无法找到一个符合条件的queue，
         * 那么会调用basic.return方法将消息返还给生产者；
         *
         * 当mandatory设为false时，出现上述情形broker会直接将消息扔掉。
         *
         * 当immediate标志位设置为true时，如果exchange在将消息route到queue(s)时发现对应的queue上没有消费者，
         * 那么这条消息不会放入队列中。当与消息routeKey关联的所有queue(一个或多个)都没有消费者时，
         * 该消息会通过basic.return方法返还给生产者。
         *
         *
         * 概括来说，mandatory标志告诉服务器至少将该消息route到一个队列中，否则将消息返还给生产者；
         * immediate标志告诉服务器如果该消息关联的queue上有消费者，则马上将消息投递给它，
         * 如果所有queue都没有消费者，直接把消息返还给生产者，不用将消息入队列等待消费者了。
         */
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setExchange(exchange);
        rabbitTemplate.setRoutingKey(routingKey);
        //2
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        RetryCache retryCache = new RetryCache();

        /**
         * 设置发送确认
         * 释义:
         * 1.异步的接收从rabbitmq返回的ack确认信息
         * 2.收到ack后调用confirmCallback函数
         *
         * 注:在confirmCallback中是没有原message的，所以无法在这个函数中调用重发，confirmCallback只有一个通知的作用。
         *
         * 确认消息是否到达broker服务器，也就是只确认是否正确到达exchange中即可，只要正确的到达exchange中，broker即可确认该消息返回给客户端ack。
         */
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            /**
             * 通过回调返回值ack判断是否发送
             * 失败 则打印消费失败原因
             */
            if (!ack) {
                log.info("send message failed: " + cause + correlationData.toString());
            } else {

                /**
                 * 1 在本地缓存已发送的message
                 * 2 通过confirmCallback或者被确认的ack，将被确认的message从本地删除
                 * 3 定时扫描本地的message，如果大于一定时间（3min）未被确认，则重发
                 */
                retryCache.del(correlationData.getId());
            }
        });

        /**
         * 触发场景：如果消息无法发送到指定的消息队列那么ReturnCallBack回调方法会被调用。
         *
         * 如果客户端通过调用setReturnCallback(ReturnCallback callback)注册了RabbitTemplate.ReturnCallback，
         * 那么返回将被发送到客户端。
         *
         * 一个RabbitTemplate只支持一个ConfirmCallback
         */
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, tmpExchange, tmpRoutingKey) -> {
            try {
                Thread.sleep(Constants.ONE_SECOND);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            log.info("send message failed: " + replyCode + " " + replyText);

            rabbitTemplate.send(message);
        });

        //4
        return new MessageSender() {
            {
                retryCache.setSender(this);
            }

            @Override
            public DetailRes send(Object message) {
                try {
                    /**在client端发送之前，先在本地缓存message,作为重发机制的开始，如果三分钟内没有收到发送成功的回调，则重发*/
                    String id = retryCache.generateId();
                    retryCache.add(id, message);
                    rabbitTemplate.correlationConvertAndSend(message, new CorrelationData(id));
                } catch (Exception e) {
                    return new DetailRes(false, "");
                }

                return new DetailRes(true, "");
            }
        };
    }


    public <T> MessageConsumer buildMessageConsumer(String exchange, String routingKey, final String queue,
                                                    final MessageProcess<T> messageProcess) throws IOException {
        return buildMessageConsumer(exchange, routingKey, queue, messageProcess, "direct");
    }

    public <T> MessageConsumer buildTopicMessageConsumer(String exchange, String routingKey, final String queue,
                                                         final MessageProcess<T> messageProcess) throws IOException {
        return buildMessageConsumer(exchange, routingKey, queue, messageProcess, "topic");
    }

    /**
     * 消费者：
     * <p>
     * 1 创建连接和channel
     * 2 设置message序列化方法
     * 3 构造consumer
     *
     * @param exchange
     * @param routingKey
     * @param queue
     * @param messageProcess
     * @param <T>
     * @return
     * @throws IOException
     */
    public <T> MessageConsumer buildMessageConsumer(String exchange, String routingKey, final String queue,
                                                    final MessageProcess<T> messageProcess, String type) throws IOException {
        final Connection connection = connectionFactory.createConnection();

        //1
        buildQueue(exchange, routingKey, queue, connection, type);

        //2
        final MessagePropertiesConverter messagePropertiesConverter = new DefaultMessagePropertiesConverter();
        final MessageConverter messageConverter = new Jackson2JsonMessageConverter();

        //3
        return new MessageConsumer() {
            QueueingConsumer consumer;

            {
                consumer = buildQueueConsumer(connection, queue);
            }

            @Override
            //1 通过delivery获取原始数据
            //2 将原始数据转换为特定类型的包
            //3 处理数据
            //4 手动发送ack确认
            public DetailRes consume() {
                QueueingConsumer.Delivery delivery;
                Channel channel = consumer.getChannel();

                try {
                    //1
                    delivery = consumer.nextDelivery();
                    Message message = new Message(delivery.getBody(),
                            messagePropertiesConverter.toMessageProperties(delivery.getProperties(), delivery.getEnvelope(), "UTF-8"));

                    //2
                    @SuppressWarnings("unchecked")
                    T messageBean = (T) messageConverter.fromMessage(message);

                    //3
                    DetailRes detailRes;

                    try {
                        detailRes = messageProcess.process(messageBean);
                    } catch (Exception e) {
                        detailRes = new DetailRes(false, "process exception: " + e);
                    }

                    //4  对于消息处理成功或者失败给出的信息
                    if (detailRes.isSuccess()) {
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    } else {
                        //避免过多失败log
                        Thread.sleep(Constants.ONE_SECOND);
                        log.info("process message failed: " + detailRes.getErrMsg());
                        channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                    }

                    return detailRes;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return new DetailRes(false, "interrupted exception " + e.toString());
                }
                //自动重连机制
                catch (ShutdownSignalException | ConsumerCancelledException | IOException e) {
                    e.printStackTrace();

                    try {
                        channel.close();
                    } catch (IOException | TimeoutException ex) {
                        ex.printStackTrace();
                    }

                    consumer = buildQueueConsumer(connection, queue);

                    return new DetailRes(false, "shutdown or cancelled exception " + e.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("exception : ", e);

                    try {
                        channel.close();
                    } catch (IOException | TimeoutException ex) {
                        ex.printStackTrace();
                    }

                    consumer = buildQueueConsumer(connection, queue);

                    return new DetailRes(false, "exception " + e.toString());
                }
            }
        };
    }

    private void buildQueue(String exchange, String routingKey,
                            final String queue, Connection connection, String type) throws IOException {
        Channel channel = connection.createChannel(false);

        if (type.equals("direct")) {
            channel.exchangeDeclare(exchange, "direct", true, false, null);
        } else if (type.equals("topic")) {
            channel.exchangeDeclare(exchange, "topic", true, false, null);
        }

        channel.queueDeclare(queue, true, false, false, null);
        channel.queueBind(queue, exchange, routingKey);

        try {
            channel.close();
        } catch (TimeoutException e) {
            e.printStackTrace();
            log.info("close channel time out ", e);
        }
    }

    private void buildTopic(String exchange, Connection connection) throws IOException {
        Channel channel = connection.createChannel(false);
        channel.exchangeDeclare(exchange, "topic", true, false, null);
    }

    private QueueingConsumer buildQueueConsumer(Connection connection, String queue) {
        try {
            Channel channel = connection.createChannel(false);
            QueueingConsumer consumer = new QueueingConsumer(channel);

            //通过 BasicQos 方法设置prefetchCount = 1。这样RabbitMQ就会使得每个Consumer在同一个时间点最多处理一个Message。
            //换句话说，在接收到该Consumer的ack前，他它不会将新的Message分发给它
            channel.basicQos(1);
            channel.basicConsume(queue, false, consumer);

            return consumer;
        } catch (Exception e) {
            e.printStackTrace();
            log.info("build queue consumer error : ", e);

            try {
                Thread.sleep(Constants.ONE_SECOND);
            } catch (InterruptedException inE) {
                inE.printStackTrace();
            }

            return buildQueueConsumer(connection, queue);
        }
    }

    //for test
    public int getMessageCount(final String queue) throws IOException {
        Connection connection = connectionFactory.createConnection();
        final Channel channel = connection.createChannel(false);
        final AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(queue);

        return declareOk.getMessageCount();
    }
}
