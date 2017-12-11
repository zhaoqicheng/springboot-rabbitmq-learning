package com.example.learning.service;

import com.example.learning.common.DetailRes;
import com.example.learning.encapsulation.MessageProcess;
import com.example.learning.pojo.User;
import org.springframework.stereotype.Component;

/**
 * Created by zhaoqicheng on 2017/12/11.
 */
@Component
public class UserProcess implements MessageProcess<User> {

    @Override
    public DetailRes process(User message) {
        System.out.println("业务实现类");
        return new DetailRes(true, "消费成功");
    }
}
