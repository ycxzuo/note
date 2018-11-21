# Spring Cloud Eureka

## 概述

Eureka-> 找到了，有了

基于 Netflix Eureka 的二次封装，由两部分组成

* Eureka Server 注册中心
* Eureka Client 服务注册

在 Spring Cloud 中担任服务注册和发现的任务，是重要的基础功能，它有**心跳监测，健康检查，负载均衡**等功能



## 服务中心

### 客户端发现

客户端在注册中心找到所有服务端的地址，然后根据某种负载均衡机制找到服务端

* 优点
  * 简单直接，不需要代理的介入
  * 知道所有的可用的服务器
* 缺点
  * 客户端得自己实现一套负载均衡算法

### 服务端发现

由代理去众多服务端中挑选出一个服务器地址，然后供客户端调用

* 优点
  * 服务端对于客户端不可见
* 缺点
  * 需要代理的介入



## Eureka Server

### 服务构建

#### 导入依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

#### 启动main方法上加上`@EnableEurekaServer`

#### 更改配置文件

```yml
# 项目名称
spring:
  application:
    name: eureka-server

# 注册中心端口配置
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    # 关闭自己注册自己
    register-with-eureka: false
  # 关闭安全监测（Eureka对于经常上下线的服务表示是上线状态，建议设ture默认）
  server:
    enable-self-preservation: true

# 服务启动端口
server:
  port: 8761
```

#### 启动项目



### Eureka 高可用

使用 Eureka 集群，然后两两注册



## Eureka Client

### 服务构建

#### 导入依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

#### 启动main方法上加上`@EnableDiscoveryClient`

#### 更改配置文件

```yml
# 启动端口
server:
  port: 8081

# 服务名称
spring:
  application:
    name: eureka-client

# 注册中心地址
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  # 点击Eureka页面的链接的IP值
  instance:
    hostname: clientName
```

#### 启动项目