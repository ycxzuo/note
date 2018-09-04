# kafka总结

## 概述

kafka是一款分布式消息发布和订阅系统,具有高性能,高吞吐量的特点而被广泛应用于大数据的场景.kafka通过Zookeeper来管理集群配置及服务协同.Producer使用push模式将消息发布到broker,consumer通过监听使用pull模式从broker订阅和消费消息.

producer,consumer以及broker三者通过zookeeper管理协调请求和转发.



## 配置信息

### 发送端可选配置信息分析

#### asks

表示producer发送信息到broker上以后的确认值.有三个可选项

* 0: 表示producer不需要等待broker的消息确认.
  * 时延最小,风险最大(server宕机,数据丢失)
* 1: 表示producer只需要获得kafka集群中的leader节点确认即可.
  * 时延较小,确保leader金额点确认接收成功.
* all(-1): 需要ISR中所有的Replica给予接收确认,速度最慢,安全性最高.
  * 当ISR缩小到仅包含一个Replica,就不能保证一定能避免丢失数据.

#### batch.size

producer发送多个消息到broker上的同一个分区时,为了减少网络请求带来的性能开销,通过批量的方式来提交信息.,默认是16384byte=16kb,意味着当一批消息大小达到指定的batch.size的时候会统一发送.

#### linger.ms

producer默认会把两次发送的时间间隔内手机的所有请求进行一次聚合然后发送,以此来提高吞吐量.linger.ms就是为每次发送到broker的请求增加一些delay,以此来聚合更多的Message请求.(如TCP的小包等-停协议)

*Ps:如果同时配置了batch.size和linger.ms,只要满足其中一个要求就会发送请求到broker上.*

#### max.request.size

设置请求数据的最大字节数,防止较大的数据包影响吞吐量,默认为1MB.

### 消费端可选配置分析

#### group.id

可扩展且具有容错性的consumer机制.group中有多个消费者(consumer instance),他们共享一个公共的ID就是group ID.其中所有的消费者协调再一起来消费订阅的topics的所有partition.每个partition只能由同一个group内的一个消费者来消费.

#### enable.auto.commit

消费者消费消息后自动提交,只有当消息提交以后,消息才不会被再次接收到.

#### auto.commit.interval.ms

自动提交的频率,一般与enable.auto.commit配合使用.

#### auto,offset.reset

针对新的group ID中的消费之而言的,当有新group ID的消费者来消费指定的topic时,对于该参数的配置,会有不同的语义

* latest: 新的消费者将会从其他的消费者最后消费的offset出开始消费topic下的消息
* earliest: 新的消费者会从该topic最早的消息开始消费
* none: 新的消费者加入后,由于之前不存在offset,会抛异常.

#### max.poll.records

限制每次调用poll返回的消息数,这样可以更容易的预测每次poll间隔要处理的最大值.可以减少poll间隔



## 主要参数

### topic

topic是一个存储消息的逻辑概念,可以认为是一个消息集合.每条消息发送到kafka集群的消息都有一个类别.物理上说,不同的topic的消息是分开存储的.每个topic可以有多个生产者向它发送消息,也可以有多个消费者去消费其中的消息.

### partition

topic可以划分多个partition(至少一个),同一个topic下的不同分区包含的消息是不同的.每个消息在被添加到分区时,都会被分配一个offset(偏移量).每一条消息发送到broker时,会根据partition的规则选择存储到哪一个partition.如果partition规则设置合理,那么所有的消息会均匀的分布在不同的partition中,如同数据库分库分表.partition是以文件的形式存储在文件系统中.

### offset

它是消息在此partition中的唯一编号.kafka通过offset保证消息在分区内的顺序.offset不夸分区.所以kafka只保证分区内消息有序.对于应用层的消费来说,每次消费一个消息并且提交以后,会保存当前消费到的最近的哟个offset.

### metadata

topic/partition和broker的映射关系,每一个topic的每一个partition,需要知道对应的broker列表是什么.leader和follower是谁,这些信息都存在metadata内.



## 消息分发

### 分发策略

消息是kafka中的最小数据单元.一条消息由key,value组成,在发送消息时,我们可以指定key,nameproducer会根据key和partition机制(可配置扩展)来判断这条信息应该发送并存储到哪个partition中.消费端可以指定消费的分区.

### 默认分发机制

默认情况下,采用hash取模的分区算法.如果key为null,则会随机分配一个分区.随机值根据metadata.max.age.ms的时间范围随机选择一个,在这个时间范围内,key为null的消息会分配在一个分区,默认情况是10分钟更新一次.



## 消息消费

在实际应用中,每个topic都会有多个partitions好处在于一方面可以对broker上的信息进行分片,有效的减少了消息的容量,从而提高了I/O,另一方面提高了消费端的能力,一般会通过多个消费者去消费同一个topic,也就是消费端的负载均衡.







