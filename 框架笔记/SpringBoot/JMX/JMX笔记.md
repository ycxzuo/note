# JMX学习笔记

## 介绍

JMX (Java Management Extensions)，提供构建分布式、Web、模块化工具，以及管理和监控设备和应用的动态解决方案。从 Java 5 开始， JMX API 作为 Java 平台的一部分



## 规范

* JSR 3：JMX 1.0、JMX 1.1 和 JMX 1.2 （作为 Java 5 的一部分）
* JMX 1.4：2006.11.09 （作为 Java 6 的一部分）
* JSR 255：JMX 2.0
* JSR 160：JXM Remote API 1.0
* JSR 262：JXM Remote API for Web Services



## 优势

* 激活应用管理无需大量投资
* 提供伸缩性管理架构
* 整合现有的管理解决方案
* 对现有的 Java 技术起到杠杆作用
* 能够扛起来未来管理概念
* 定义面向接口管理



## 架构概况

* 设备级别 （Instrumentation Level）
  * 管理 Bean（MBeans）
    * 标准 Mbeans
      * 设计和实现最为简单，Bean 的管理通过接口方法来描述。MXBean 是一种特殊标准 MBean，它使用开放 MBean 的概念，允许通用管理，同时简化编码
    * 动态 Mbeans
      * 必须实现指定的接口，不过他在运行时能让管理接口发挥最大弹性
    * 开放 Mbeans
      * 动态 MBean，提供通用管理所依赖的基本数据类型以及用户友好的自描述信息
    * 模型 Mbeans
      * 同样也是动态 MBean，在运行时能完全可配置和自描述，微动态的设备资源提供带有默认行为的 MBean 泛型类
  * 通知模型（Notification Model）
    * 允许 MBean 广播管理事件，这种操作称之为通知。管理应用和其他对象注册成监听器
  * MBean 元数据类（MetaData Class）
    * 元信息包含描述所有 MBean 管理接口的组件接口，其中包括：
      * 属性（Attribute）
      * 操作（Operation）
      * 通知（Notification）
      * 构造器（Constructor）
* 代理级别（Agent Level）
  * MBean 服务器
    * 是一个在代理上的 MBean 的注册器，它仅用作暴露 MBean 的管理接口，而非其引用对象
  * 代理服务
    * 代理服务是在 MBean 服务器上能够执行已注册 MBean 的管理操作，其中包括以下代理服务：
      * 动态类加载
      * 监控
      * 定时器
      * 服务关系
* 分布式服务级别（Distributed Services Level）
* 可添加管理协议 API



## JMX 核心 API

### 标准 MBeans

* MBean
  * 接口的类名称必须以 `MBean` 为后缀，例如 XXXMBean，那么它的实现类名必须是 XXX
* MXBean
  * 接口的类名称必须以 `MXBean` 为后缀
    * 例如 `java.lang.management.MemoryManagerMXBean`
  * 或者接口标记 `@javax.management.MXBean` 注解



### 动态 MBeans

* 管理资源实现 `javax.management.DynamicMBean` 接口
  * 简化 API：`javax.management.StandardMBean`



### MBean 元信息类（Meta Data Class）

* 属性：javax.management.MBeanAttributeInfo
* 操作：java.managenment.MBeanOperationInfo
* 构造器：javax.management.MBeanConstructorInfo
* 参数：javax.management.MBeanParamenterInfo
* 通知：javax.management.MBeanNotificationInfo
* Bean：javax.management.MBeanInfo



### 开放 MBeans

#### 基本数据类型

* java.lang.Boolean
* java.lang.Byte
* java.lang.character
* java.lang.Double
* java.lang.Float
* java.lang.Integer
* java.lang.Long
* java.lang.Short
* boolean[]
* byte[]
* char[]
* double[]
* float[]
* int[]
* long[]
* short[]
* java.lang.String
* java.lang.Void (operation return only)
* java.math.BigDecimal
* java.math.BigInteger
* java.util.Date
* javax.management.ObjectName
* javax.management.openmbean.compositeDate (interface)
* javax.management.openmbean.TabularData (interface)



#### 开放 MBean 元信息类（Meta Data Class）

- 属性：javax.management.openmbean.OpenMBeanAttributeInfo
- 操作：java.managenment.openmbean.OpenMBeanOperationInfo
- 构造器：javax.management.openmbean.OpenMBeanConstructorInfo
- 参数：javax.management.openmbean.OpenMBeanParamenterInfo
- 通知：javax.management.openmbean.OpenMBeanNotificationInfo
- Bean：javax.management.openmbean.OpenMBeanInfo



### 模型 MBeans

* 参考 JMX 规范



### 代理相关（Agent）

* MBean 服务器：javax.management.MBeanServer
* 管理工厂：java.lang.management.ManagementFactory



## JMX 客户端

### JConsole

### VisualVM

### JXM Remote API （JSR-160）



## JMX Spring Boot 整合

### 核心组件

* 资源管理
  * 组件：ManagedResource
  * 注解：@ManagedResource
* Spring JMX组件：MBeanExportor
* Spring Boot 自动装配：JMXAutoConfiguration

