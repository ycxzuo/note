# Spring概览

## Core Container

提供 IoC/DI 的特性，基础是 BeanFactory

### Core

主要包含 Spring 框架基本的核心工具类

### Beans

它包含访问配置文件、创建和管理 Bean 以及进行 IoC/DI 操作相关的所有类

### Context

构建于Core 和 Beans 模块基础上，提供了一种类似于 JNDI 注册器的框架式的对象访问方法。继承了 Beans 的特性，为 Spring 核心提供了大量扩展，添加了对国际化（I18N）的支持（例如资源绑定）、事件传播、资源加载和对 Context 的透明创建的支持。`ApplicationContext` 接口是 Context 模块的关键

### Expression Language

简称 Spring El 表达式，用于在运行时查询和操作对象。支持设置/获取属性的值，属性的分配，方法的调用，访问数组上下文（accessiong the context of arrays）、容器和索引器、逻辑和算术运算符、命名变量以及从 Spring 的 IoC 容器中根据名称检索对象。也支持 list 投影、选择和一般的 list 聚合



## Data Access/Integration

与数据仓储的类

### JDBC

Java DataBase Connectivity，对 JDBC 抽象，消除冗长的 JDBC 编码和解析数据库厂商特有的错误代码

### ORM

ObjectRelationalMapping，封装包提供了常用的“对象/关系”映射APIs的集成层。 其中包括JPA、JDO、Hibernate 和iBatis 。利用 ORM 封装包，可以混合使用所有 Spring 提供的特性进行“对象/关系”映射，如前边提到的简单声明性事务管理

### OXM

Object XML Mapping，提供一个 Object/XML 映射实现的抽象，映射实现包括 JAXB、Castor、XMLBeans、JiBX 和 XStream

### JMS

Java Messages Service，主要包括一些创建和消费消息的特性

### TX

支持编程和声明式的事务管理



## Web

建立 WebApplicationContext 的集成

### Web

提供了面向基础的 Web 的继承特性，如多文件上传、使用 servlet listeners 初始化 IoC 容器及 WebApplicationContext

### Web-mvc

包含 Spring 的 mvc 实现，与 Servlet 结合

### Web-struts

提供对 Strust 的支持，在 3.0 版本已经移除



## AOP

提供一个符合 AOP 联盟标准的实现

### Aspects

提供了对 AspectJ 的集成

### Instrumentation

提供 class instumentation 支持和 classloader 实现



## Test

支持 JUnit 和 TestNG 组件进行测试