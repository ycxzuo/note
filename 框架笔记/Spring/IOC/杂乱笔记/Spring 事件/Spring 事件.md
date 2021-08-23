# Spring 事件

## Java 事件/监听器编程模型

* 设计模式：观察者模式扩展
  * 可观察对象：java.util.Observable
  * 观察者：java.util.Observer
* 标准化接口
  * 事件对象：java.util.EventObject
  * 事件监听器：java.util.EventListener



## 面向接口的事件/监听器设计模式

### 使用场景

| Java 技术规范   | 事件接口                               | 监听器接口                                |
| --------------- | -------------------------------------- | ----------------------------------------- |
| JavaBeans       | java.beans.PropertyChangedEvent        | java.beans.PropertyChangedListener        |
| Java AWT        | java.awt.event.MouseEvent              | java.awt.event.MouseListener              |
| Java Swing      | javax.swing.event.MenuEvent            | javax.swing.event.MenuListener            |
| Java Preference | java.util.prefs.PreferenceChangedEvent | java.utili.prefs.PreferenceChangeListener |



## 面向注解的事件/监听器设计模式

### 使用场景

| Java 技术规范 | 事件注解                       | 监听器注解                            |
| ------------- | ------------------------------ | ------------------------------------- |
| Servlet 30+   |                                | @javax.servlet.annotation.WebListener |
| JPA 1.0+      | @javax.persistence.PstPersist  |                                       |
| Java Common   | @PostConstruct                 |                                       |
| EJB 3.0+      | @javax.ejb.PrePassivate        |                                       |
| JSF 2.0+      | @javax.faces.event.ListenerFor |                                       |

PrePassivate（钝化）：类似持久化数据，等重启后进行激活



## Spring 标准事件：ApplicationEvent

* Java 标准事件 Java,util.EventObject 扩展
  * 扩展特性：事件发生时间戳
* Spring 应用上下文 ApplicationEvent 扩展：ApplicationContextEvent
  * Spring 应用上下文（ApplicationContext）作为事件源
  * 具体实现：
    * org.springframework.context.event.ContextClosedEvent
    * org.springframework.context.event.ContextRefreshedEvent
    * org.springframework.context.event.ContextStartedEvent
    * org.springframework.context.event.ContextStoppedEvent



## 基于接口的 Spring 事件监听器

### Java 标准事件监听器 Java.util.EventListener 扩展

* 扩展接口：org.springframework.context.ApplicationListener

* 设计特点：单一类型事件处理
* 处理方法：onApplicationEvent(ApplicationEvent)
* 事件类型：org.springframework.context.ApplicationEvent



## 基于注解的 Spring 事件监听器

### Spring 注解：@org.springframework.context.event.EventListener

| 特性                 | 说明                         |
| -------------------- | ---------------------------- |
| 设计特点             | 支持多 ApplicationEvent 类型 |
| 注解目标             | 方法                         |
| 是否支持异步执行     | 支持                         |
| 是否支持泛型类型事件 | 支持                         |
| 是否支持顺序控制     | 支持，配合 @Order 注解控制   |



## 注册 Spring ApplicationListener

* 方法一：ApplicationListener 作为 Spring Bean 注册
* 方法二：通过 ConfigurableApplicationContext API 注册



## Spring 事件发布器

* 方法一：
  * 获取 ApplicationEventPublisher
    * 依赖注入
* 方法二：通过 ApplicationEventMulticaster 发布 Spring 事件
  * 获取 ApplicationEventMuticaster
    * 依赖注入
    * 依赖查找



## Spring 层次性上下文事件传播

* 发生说明
  * 当 Spring 应用出现多层次 Spring 应用上下文（ApplicationContext）时，如 Spring MVC、Spring Boot 或 Spring Cloud 场景下，由子 ApplicationContext 发起 Spring 事件可能会传递到其 Parent ApplicationContext（直到 Root）的过程
* 如何避免
  * 定位 Spring 事件源（ApplicationContext）进行过滤处理



## Spring 内建事件

ApplicationContextEvent 派生事件

* ContextRefreshedEvent：Spring 应用上下文就绪事件
* ContextStartedEvent：Spring 应用上下文启动事件
* ContextStoppedEvent：Spring 应用上下文停止事件
* ContextClosedEvent：Spring 应用上下文关闭事件



## Spring 4.2 Payload 事件

* Spring Payload 事件：org.springframework.context.PayloadApplicationEvent
  * 使用场景：简化 Spring 事件发送，关注事件源主体
  * 发送方法：ApplicationEventPublisher#publishEvent(java.lang.Object)

*注意*：PayloadApplicationEvent 这个类不要随便的继承，因为泛型可能失效，最好直接使用，因为在调用 ResolvableType#forClassWithGenerics(java.lang.Class<?>, org.springframework.core.ResolvableType...) 获取类上面的泛型参数时，只拿到本类上面的泛型，如果自定义的类没有泛型参数，就拿不到，所以会报错：Mismatched number of generics specified



## 自定义 Spring 事件

* 扩展 org.springframework.context.ApplicationEvent
* 实现 org.springframework.context.ApplicationListener
* 注册 org.springframework.context.ApplicationListener



## 依赖注入 ApplicationEventPublisher

* 通过 ApplicationEventPublisherAware 回调接口
* 通过 AutoWired ApplicationEventPublisher

*注意*：ApplicationContextAwareProcessor 的 invokeAwareInterfaces 方法决定了执行顺序



## 依赖查找 ApplicationEventMulticaster

### 查找条件

* Bean 名称："applicationEventMulticaster"
* Bean 类型："org.springframework.context.event.ApplicationEventMulticaster"



## ApplicationEventPublisher 底层实现

* 接口：org.springframework.context.event.ApplicationEventMulticaster
* 抽象类：org.springframework.context.event.AbstractApplicationEventMulticaster
* 实现类：org.springframework.context.event.SimpleApplicationEventMulticaster

ApplicationEventPublisher 利用 ApplicationContext 做桥接，调用的是 ApplicationEventMulticaster 发布的消息

*注意*：在 Spring 3 之前，不要将 ApplicationEventPublisher 和 BeanPostProcessor 或者 BeanFactoryPostProcessor 一起使用，因为在 register 这两个 PostProcessor 会导致所在的 Bean 提前初始化，但是此时 ApplicationEventMulticaster 还未初始化，会报空指针异常。后期 Spring 在 ApplicationEventMulticaster 还未初始化之前，使用 earlyApplicationEvents 来临时存储发布的消息，等初始化完成后，将 earlyApplicationEvents 设置为 null



## 同步和异步 Spring 事件广播

* 基于实现类：org.springframework.context.event.SimpleApplicationEventMulticaster
  * 模式切换：setTaskExecutor(java.util.concurrent.Executor) 方法
    * 默认模式：同步
    * 异步模式：如 java.util.concurrent.ThreadPoolExecutor
  * 设计缺陷：非基于接口契约编程
  * 影响范围：全局
* 基于注解：@org.springframework.context.event.EventListener
  * 模式切换
    * 默认模式：同步
    * 异步模式：标注 @org.springframework.scheduling.annatation.Async
  * 实现限制：无法直接实现同步/异步动态切换
  * 影响范围：局部（或方法）



## Spring 4.1 事件异常处理

Spring 3.0 错误接口：org.springframework.util.ErrorHandler

* 使用场景
  * Spring 事件（Event）
    * SimpleApplicationEventMulticaster Spring 4.1 开始支持
  * Spring 本地调度（Scheduling）
    * org.springframework.scheduling.concurrent.ConcurrentTaskScheduler
    * org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler



## Spring 事件/监听器实现原理

核心类：org.springframework.context.event.SimpleApplicationEventMulticaster

* 设计模式：观察者模式扩展
  * 被观察者：org.springframework.context.event.SimpleApplicationEventMulticaster
  * 观察者：org.springframework.context.ApplicationListener
    * API 添加
    * 依赖查找
  * 通知对象：org.springframework.context.ApplicationEvent
* 执行模式：同步/异步
* 异常处理：org.springframework.util.ErrorHandler
* 泛型处理：org.springframework.core.ResolvableType



### 注解监听流程

1. AnnotationConfigUtils#registerAnnotationConfigProcessors(BeanDefinitionRegistry) 注册部分 BeanDefinition 到 BeanDefinitionRegistry 里面，其中就包括 EventListenerMethodProcessor

2. EventListenerMethodProcessor#afterSingletonsInstantiated 会在实例化的中被调用，最终调用 EventListenerMethodProcessor#processBean

3. EventListenerMethodProcessor#processBean 会找到用户定义的（非 org.springframework 包下的，并且没有 Component 注解修饰） @EventListener 注解并把 listener 注册到 ApplicationContext 中



### AbstractApplicationEventMulticaster 小结

事件与监听器的关联关系缓存其实存储在成员变量 Map<ListenerCacheKey, ListenerRetriever> retrieverCache 之中

* ListenerCacheKey
  * eventType：事件的类型（ResolvableType）
  * sourceType：发生事件的源的类型（Class<?>）
* ListenerRetriever
  * applicationListeners：监听器的实例列表（有顺序的 LinkedHashSet）
  * applicationListenerBeans：监听器的 Bean 名称列表（有顺序的 LinkedHashSet）

全量的监听器则保存在成员变量 ListenerRetriever defaultRetriever 之中

*注意*：

1. 每次对监听器的增删都会导致缓存的清除

2. 同样的监听器（被代理的也算，因为 Set 做不到代理的去重，所以手动判断了一次：AopProxyUtils.getSingletonTarget(listener)）只会注册一次



## 课外补充

* Spring Boot 事件

  | 事件类型                            | 发生时机                                |
  | ----------------------------------- | --------------------------------------- |
  | ApplicationStartingEvent            | 当 Spring Boot 应用启动时               |
  | ApplicationStartedEvent             | 当 Spring Boot 应用启动完成时           |
  | ApplicationEnvironmentPreparedEvent | 当 Spring Boot Environment 实例已准备时 |
  | ApplicationPreparedEvent            | 当 Spring Boot 应用预备时               |
  | ApplicationReadyEvent               | 当 Spring Boot 应用完全可用时           |
  | ApplicationFailedEvent              | 当 Spring Boot 应用启动失败时           |

* Spring Cloud 事件

  | 事件类型                   | 发生时机                              |
  | -------------------------- | ------------------------------------- |
  | EnvironmentChangedEvent    | 当 Environment 实例配置属性发生变化时 |
  | HeartbeatEvent             | 当 DiscoveryClient 客户端发送心跳时   |
  | InstancePreRegisteredEvent | 当服务实例注册前                      |
  | InstanceRegisteredEvent    | 当服务实例注册后                      |
  | RefreshEvent               | 当 RefreshEndpoint 被调用时           |
  | RefreshScopeRefreshedEvent | 当 Refresh Scope Bean 刷新后          |

  