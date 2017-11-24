package com.example.learning;


import com.example.learning.common.DetailRes;

/**
 * Created by littlersmall on 16/5/12.
 *
 * send接口
 *
 */
public interface MessageSender {
    DetailRes send(Object message);
}
