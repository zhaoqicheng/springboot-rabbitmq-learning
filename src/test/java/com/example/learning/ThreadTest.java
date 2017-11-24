package com.example.learning;

/**
 * Created by zhaoqicheng on 2017/11/24.
 */
public class ThreadTest {

    public static void main(String[] args) {
        //线程使用示例一：
        new Thread() {
            public void run() {
                while (true) {
                    try {
                        System.out.println("线程输出");

                        //休眠两秒
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            ;
        }.start();
    }
}
