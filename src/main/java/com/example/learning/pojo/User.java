package com.example.learning.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by zhaoqicheng on 2017/12/8.
 */
@Setter
@Getter
@ToString
public class User implements Serializable {

    private Integer id;

    private String name;

    private String telephone;

}
