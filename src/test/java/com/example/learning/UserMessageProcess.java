package com.example.learning;


import com.example.learning.common.DetailRes;

/**
 * Created by littlersmall on 16/6/28.
 */
public class UserMessageProcess implements MessageProcess<UserMessage> {
    @Override
    public DetailRes process(UserMessage userMessage) {
        System.out.println(userMessage);

        return new DetailRes(true, "");
    }
}
