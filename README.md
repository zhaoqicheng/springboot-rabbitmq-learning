框架已经搭建完成，带有JUNIT测试，可直接运行

借鉴与：http://www.jianshu.com/p/4112d78a8753

改造部分：
与springboot框架结合


对rabbitmq的封装，有几个目标：
1 提供send接口
2 提供consume接口
    (1) 处理成功，从队列中删除消息
    (2) 处理失败(网络问题，程序问题，服务挂了)，将消息重新放回队列
3 保证消息的事务性处理
4 使用线程来消费通道中的消息
5 自动重连机制
  为了保证rabbitmq的高可用性，我们使用rabbitmq Cluster模式，并配合haproxy。
  这样，在一台机器down掉时或者网络发生抖动时，就会发生当前连接失败的情况，如果不对这种情况做处理，
  就会造成当前的服务不可用。在spring-rabbitmq中，已实现了connection的自动重连，但是connection重连后，channel的状态并不正确。
  因此我们需要自己捕捉ShutdownSignalException异常，并重新生成channel
  
感谢这篇干货：
    http://www.jianshu.com/p/4112d78a8753

注：
    在test文件夹下的所有的JUNIT测试均可通过。
    
其他测试：
localhost:8080/testMQRestComplex/createMQByComplex

测试发送失败的充实机制：
    将mq停止再往通道中发送消息不成功，程序会尝试重试链接mq发送消息。