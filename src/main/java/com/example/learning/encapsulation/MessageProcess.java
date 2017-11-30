package com.example.learning.encapsulation;


import com.example.learning.common.DetailRes;

/**
 * Created by littlersmall on 16/5/11.
 * MessageProcess(用户自定义处理接口，也就是处理消息的接口，处理完毕以后需要返回一个DetailRes对象来告诉rabbitMQ消息处理完毕，让MQ继续删除该条消息，否则会引起消息堆积。)
 *
 * 实现该接口，在收到队列中的消息的时候处理业务
 */
public interface MessageProcess<T> {
    DetailRes process(T message);
}
