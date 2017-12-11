package com.example.learning.rest;

import com.example.learning.pojo.User;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zhaoqicheng on 2017/12/1.
 */
@RestController
@RequestMapping(value = "testMQRestSimple")
public class TestMQSendSimpleRest {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    /**
     * 普通的MQ自带的方法实现通道存入
     * <p>
     * 可实现：
     * 单生产--单消费
     * 单生产--多消费
     * 多生产--多消费
     *
     * @throws Exception
     */
    @PostMapping(value = "createMQBySimple")
    public void CreateTradeReportLogMQTask() throws Exception {
        /**
         * 在数据库表中创建记录本次创建操作
         */
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setId(i);
            user.setName("王明-" + i);
            user.setTelephone(i + "");
            /**
             * 仅仅指定了routingKey  会生成一个名字为 user-simple 的通道
             */
            this.rabbitTemplate.convertAndSend("user-simple", user);
        }

    }

    /**
     * 创建topic ExChange示例
     * <p>
     * topic 是RabbitMQ中最灵活的一种方式，可以根据binding_key自由的绑定不同的队列
     * <p>
     * 首先对topic规则配置，这里使用两个队列来测试（创建和绑定的topic.message和topic.messages两个队列），其中topic.message的
     * binding_key为“topic.message”，topic.messages的binding_key为“topic.#”
     * <p>
     * 通过这个例子可以得知：通道中的消息可以被重复消费
     *
     * @throws Exception
     */
    @PostMapping(value = "createMQBySimpleForTopic")
    public void CreateTradeReportLogMQTaskForTopic() throws Exception {

        String msg1 = "I am topic.mesaage msg======";
        System.out.println("sender1 : " + msg1);
        this.rabbitTemplate.convertAndSend("exchange", "topic.message", msg1);

        String msg2 = "I am topic.mesaages msg########";
        System.out.println("sender2 : " + msg2);
        this.rabbitTemplate.convertAndSend("exchange", "topic.messages", msg2);

    }

    /**
     * 创建fanout ExChange示例
     * <p>
     * Fanout 就是我们熟悉的广播模式或者订阅模式，给Fanout转发器发送消息，绑定了这个转发器的所有队列都收到这个消息。
     * <p>
     * 这里使用三个队列来测试（也就是在Application类中创建和绑定的fanout.A、fanout.B、fanout.C）
     * 这三个队列都和Application中创建的fanoutExchange转发器绑定。
     * <p>
     * 由以下结果可知：就算fanoutSender发送消息的时候，指定了routing_key为"abcd.ee"，但是所有接收者都接受到了消息
     *
     * @throws Exception
     */
    @PostMapping(value = "createMQBySimpleForFanout")
    public void CreateTradeReportLogMQTaskForFanout() throws Exception {
        String msgString = "fanoutSender :hello i am fanoutSender";
        System.out.println(msgString);
        this.rabbitTemplate.convertAndSend("fanoutExchange", "abcd.ee", msgString);
    }

    @Autowired
    private CallBackSender callBackSender;

    @PostMapping("/callback")
    public void callbak() {
        callBackSender.send();
    }
}
