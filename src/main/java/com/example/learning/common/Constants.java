package com.example.learning.common;

/**
 * Created by littlersmall on 16/5/19.
 */
public class Constants {

    /**
     * 线程数量
     */
    public final static int THREAD_COUNT = 5;

    /**
     * 间隔处理时间（mils）
     */
    public final static int INTERVAL_MILS = 0;

    /**
     * 消费失败后等待时间(mils)
     */
    public static final int ONE_SECOND = 1 * 1000;

    /**
     * 异常后线程休眠时间
     */
    public static final int ONE_MINUTE = 1 * 60 * 1000;

    /**
     * MQ消息retry时间
     */
    public static final int RETRY_TIME_INTERVAL = ONE_MINUTE;

    /**
     * MQ消息有效时间
     */
    public static final int VALID_TIME = ONE_MINUTE;
}

