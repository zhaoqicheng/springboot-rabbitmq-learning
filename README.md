##### 框架已经搭建完成，带有JUNIT测试，可直接运行

------------



**`参考：[普通链接](http://www.mdeditor.com/)，改造部分：与springboot框架结合`**

__`对rabbitmq的封装，有几个目标：`__
- `1 提供send接口`
- `2 提供consume接口`
    - `(1) 处理成功，从队列中删除消息`
    - `(2) 处理失败(网络问题，程序问题，服务挂了)，将消息重新放回队列`
- `3 保证消息的事务性处理`
- `4 使用线程来消费通道中的消息`
- `5 自动重连机制`
  `为了保证rabbitmq的高可用性，我们使用rabbitmqCluster模式，并配合haproxy。这样，在一台机器down掉时或者网络发生抖动时，就会发生当前连接失败的情况，如果不对这种情况做处理，就会造成当前的服务不可用。在spring-rabbitmq中，已实现了connection的自动重连，但是connection重连后，channel的状态并不正确。因此我们需要自己捕捉ShutdownSignalException异常，并重新生成channel`
  
  
__`对rabbitmq的封装，有几个目标：`__