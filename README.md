#### 框架已经搭建完成，带有JUNIT测试，可直接运行

------------



**`参考：http://www.jianshu.com/p/4112d78a8753`**

__`对rabbitmq的封装，有几个目标：`__
- `1 与springboot框架结合`
- `2 提供send接口`
- `3 提供consume接口`
    - `(1) 处理成功，从队列中删除消息`
    - `(2) 处理失败(网络问题，程序问题，服务挂了)，将消息重新放回队列`
- `4 保证消息的事务性处理`
- `5 使用线程来消费通道中的消息`
- `6 自动重连机制`<br>
     `为了保证rabbitmq的高可用性，我们使用rabbitmqCluster模式，并配合haproxy。这样，在一台机器down掉时或者网络发生抖动时，就会发生当前连接失败的情况，如果不对这种情况做处理，就会造成当前的服务不可用。在spring-rabbitmq中，已实现了connection的自动重连，但是connection重连后，channel的状态并不正确。因此我们需要自己捕捉ShutdownSignalException异常，并重新生成channel`
  
  
__`rabbitMq的常用命令：`__
- `1 启动rabbitMQ:  rabbitmq-server`
- `2 停止应用:  rabbitmqctl stop_app`
- `3 查看所有的通道:  rabbitmqctl list_queues`
- `4 再次启动应用:  rabbitmqctl start_app`
- `5.清除消息队列中的所有数据:  先执行2、再执行：rabbitmqctl reset 、最后执行4`
- `6.关闭rabbitMQ:  rabbitmqctl stop`

__`rabbitMq的占用端口和管理界面：`__
- `管理界面：在浏览器中输入http://127.0.0.1:15672/，用户名和密码（默认为guest）`
- `默认安装的rabbitMq占用端口为：5672`

__`rabbitMq的基本概念学习（易忘、复习务必重新看）：`__  
`什么是rabbitMq？`   
`多系统、异构系统之间的数据交换的中间件`
`参考资料：https://www.cnblogs.com/diegodu/p/4971586.html`  



