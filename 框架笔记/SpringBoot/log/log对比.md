# 常用 Log 对比

## log4j

### 介绍

Log For Java，该项目已经停止维护 `(End of Life)`，

### 概念

#### log4j API

* 日志对象 (org.apache.log4j.Logger)

  * 是最核心的 API，使用 `Logger.getLogger("ROOT");` 获得
  * 是 org.apache.log4j.Category 的子类

* 日志级别 (org.apache.log4j.Level)

  * 级别
    * OFF
    * FATAL
    * ERROR
    * WARN
    * INFO
    * DEBUG
    * TRACE
    * ALL
  * 是 org.apache.log4j.Priority 的子类

* 日志管理器 (org.apache.log4j.LogManager)

  * 职责
    * 初始化默认 log4j 配置 (读取 log4j.xml 或者 log4j.properties)
    * 维护日志仓储 (org.apache.log4j.spi.LoggerRepository)
    * 维护日志对象 (org.apache.log4j.Logger)

* 日志仓储 (org.apache.log4j.LoggerRepository)

  * 职责
    * 管理日志级别阈值 (org.apache.log4j.Level)
    * 管理日志对象 (org.apache.log4j.Logger)

* 日志附加器 (org.apache.log4j.Appender)

  * 是将日志事件 (org.apache.log4j.spi.LoggingEvent) 具体输出的介质，如控制台，文件系统等
  * 关联零个或多个日志过滤器 (org.apache.log4j.spi.Filter)，这些过滤器形成的过滤链
  * 职责
    * 附加日志事件 (org.apache.log4j.spi.LoggingEvent)
    * 关联日志布局 (org.apache.log4j.Layout)
    * 关联日志过滤器 (org.apache.log4j.spi.Filter)
    * 关联错误处理器 (org.apache.log4j.spi.ErrorHandler)
  * ![结构图](http://tva1.sinaimg.cn/large/007X8olVly1g6m5jzqnwzj30vd0m6769.jpg)
  * 实现
    * 控制台 (org.apache.log4j.ConsoleAppender)
    * 文件
      * 普通方式 (org.apache.log4j.FileAppender)
      * 滚动方式 (org.apache.log4j.RollingFileAppender)
      * 每日规定方式 (org.apache.log4j.DailyRollingFileAppender)
    * 网络
      * Socket 方式 (org.apache.log4j.net.SocketAppender)
      * JMS 方式 (org.apache.log4j.net.JMSAppender)
      * SMTP 方式 (org.apache.log4j.net.SMTPAppender)
    * 异步 (org.apache.log4j.AsyncAppender)

* 日志过滤器 (org.apache.log4j.spi.Filter)

  * 用于决策当前日志事件 (org.apache.log4j.spi.LoggingEvent) 是否需要在执行所关联的日志附加器中执行，决策有三种
    * DENY：日志事件跳过日志附加器的执行
    * ACCEPT：日志附加器立即执行日志事件
    * NEUTRAL：跳过当前过滤器，让下一个过滤器决策

* 日志格式布局 (org.apache.log4j.Layout)

  * 用于格式化日志事件为可读性的文本内容
  * 内建实现
    * 简单格式 (org.apache.log4j.SimpleLayout)
    * 模式格式 (org.apache.log4j.PatternLayout)
    * 提升模式格式 (org.apache.log4j.EnhancedPatternLayout)
    * HTML 格式 (org.apache.log4j.HTMLLayout)
    * XML 格式 (org.apache.log4j.XMLLayout)
    * TTCC 格式 (org.apache.log4j.TTCCLayout)  Time-Thread-Category-nested Context information

* 日志事件 (org.apache.log4j.spi.LoggingEvent)

  * 用于承载日志信息的对象，包括
    * 日志名称
    * 日志内容
    * 日志级别
    * 异常信息（可选）
    * 当前线程名称
    * 时间戳
    * 嵌入诊断上下文（NDC）
    * 映射诊断上下文（MDC）

* 日志配置器 (org.apache.log4j.Configurator)

  * 提供外部配置文件配置 log4j 行为的API、log4j 内建了两种实现

    * Perperties (org.apache.log4j.PropertyConfigurator)

      ```properties
      ### 设置 ###
      log4j.rootLogger = DEBUG,stdout
      ### 输出信息到控制抬 ###
      log4j.appender.stdout = org.apache.log4j.ConsoleAppender
      log4j.appender.stdout.layout = org.apache.log4j.PatternLayout
      log4j.appender.stdout.layout.ConversionPattern = [%t] [%d{yyyy-MM-dd HH:mm:ss}] %-5p %l - %m%n
      ```

    * XML (org.apache.log4j.xml.DOMConfigurator) ，优先级高于 Properties

      ```xml
      <?xml version="1.0" encoding="UTF-8"?>
      <!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
      <log4j:configuration xmlns:log4j='http://jakarta.apache.org/log4j/' >
          <appender name="myConsole" class="org.apache.log4j.ConsoleAppender">
              <layout class="org.apache.log4j.PatternLayout">
                  <param name="ConversionPattern"
                      value="[%t] [%d{yyyy-MM-dd HH:mm:ss}] %-5p %l - %m%n" />
              </layout>
              <!--过滤器设置输出的级别-->
              <filter class="org.apache.log4j.varia.LevelRangeFilter">
                  <param name="levelMin" value="debug" />
                  <param name="levelMax" value="warn" />
                  <param name="AcceptOnMatch" value="true" />
              </filter>
          </appender>
      
          <appender name="myFile" class="org.apache.log4j.RollingFileAppender">
              <param name="File" value="D:/output.log" /><!-- 设置日志输出文件名 -->
              <!-- 设置是否在重新启动服务时，在原有日志的基础添加新日志 -->
              <param name="Append" value="true" />
              <param name="MaxBackupIndex" value="10" />
              <layout class="org.apache.log4j.PatternLayout">
                  <param name="ConversionPattern" value="%p (%c:%L)- %m%n" />
              </layout>
          </appender>
      
          <appender name="activexAppender" class="org.apache.log4j.DailyRollingFileAppender">
              <param name="File" value="E:/activex.log" />
              <param name="DatePattern" value="'.'yyyy-MM-dd'.log'" />
              <layout class="org.apache.log4j.PatternLayout">
                  <param name="ConversionPattern" value="[%d{MMdd HH:mm:ss SSS\} %-5p] [%t] %c{3\} - %m%n" />
              </layout>
          </appender>
      
          <!-- 指定logger的设置，additivity指示是否遵循缺省的继承机制-->
          <logger name="com.runway.bssp.activeXdemo" additivity="false">
              <appender-ref ref="activexAppender" />
          </logger>
      
          <!-- 根logger的设置-->
          <root>
              <priority value ="debug"/>
              <appender-ref ref="myConsole"/>
              <appender-ref ref="myFile"/>
          </root>
      </log4j:configuration>
      ```

      

* 日志诊断上下文 (org.apache.log4j.NDC, org.apache.log4j.MDC)

  * 作为日志内容的一部分，为其提供辅助信息，如当前 HTTP 请求 URL

  * 嵌入诊断上下文 org.apache.log4j.NDC

    * 缺陷，push 多少要 pop 完全

    * 以堆栈形式存储诊断信息

      ```java
      NDC.push("Dorothy");
      NDC.push("Parker");
      NDC.pop();
      NDC.pop();
      PatternLayout layout = new PatternLayout("%x - %m%n");
      NDC.remove()
      ```

      

  * 映射诊断上下文 org.apache.log4j.MDC

    * Key-Value的方式存储诊断信息

      ```java
      MDC.put("first", "Dorothy");
      MDC.put("last", "Parker");
      PatternLayout layout = new PatternLayout("%c %X{first} %X{last} %m%n")
      ```



#### 引入依赖

```xml
<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.17</version>
</dependency>
```



## Java Logging

### 介绍

Java标准的日志框架，也称为 Java Logging API，即 JSR 47。从 Java 1.4 版本开始，Java Logging 称为 Java SE 的功能模块，其实现类存放在 `java.util.logging` 包下



### 特点

* Java 天然性
* Java Security 整合
* Java 国际化/本地化 整合



### 整体架构

![logging](http://tva1.sinaimg.cn/large/007X8olVly1g6mlpo87tlj30f403w3yh.jpg)



### 概念

#### Java Logging API

* 日志对象 (java.util.logging.Logger)
* 日志级别 (java.util.logging.Level)
* 日志管理器 (java.util.logging.LogManager)
* 日志处理器 (java.util.logging.Handler)
* 日志过滤器 (java.util.logging.Filter)
* 日志格式器 (java.util.logging.Formatter)
* 日志记录 (java.util.logging.LogRecord)
* 日志权限 (java.util.logging.LoggingPermission)
* 日志 JMX 接口 (java.util.logging.LoggingMXBean)



## Logback

### 介绍

Log4j 的替代者，在架构和特征上有着相当的提升

### [提升](https://logback.qos.ch/reasonsToSwitch.html)

* 执行速度更快，内存占用更小
* Slf4 无缝整合
* 自动重载配置文件
* 自动移除老的归档日志
* 自动压缩归档日志文件
* 条件化配置文件

### 配置文件

logback-spring.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration debug="true" scan="true" scanPeriod="1 seconds">

    <contextName>logback</contextName>

    <!--ConsoleAppender 用于在屏幕上输出日志-->
    <appender name="stdout" class="ch.qos.logback.core.ConsoleAppender">
        <!--定义了一个过滤器,在LEVEL之下的日志输出不会被打印出来-->
        <!--这里定义了DEBUG，也就是控制台不会输出比ERROR级别小的日志-->
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>DEBUG</level>
        </filter>
        <!-- encoder 默认配置为PatternLayoutEncoder -->
        <!--定义控制台输出格式-->
        <encoder>
            <pattern>%d [%thread] %-5level %logger{36} [%file : %line] - %msg%n</pattern>
        </encoder>
    </appender>

    <!--root是默认的logger 这里设定输出级别是debug-->
    <root level="trace">
        <appender-ref ref="stdout"/>
    </root>

    <!--对于类路径以 com.example.logback 开头的Logger,输出级别设置为warn,并且只输出到控制台-->
    <!--这个logger没有指定appender，它会继承root节点中定义的那些appender-->
    <logger name="com.yczuoxin.springboot.test16" level="INFO"/>

</configuration>
```

### 依赖

```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
</dependency>
```



## Log4j2

### 介绍

Log4j 的替代者，在性能方面提升非常显著

### [提升](https://logging.apache.org/log4j/2.x/)

* 执行速度更快，内存占用更小
* 避免锁
* 自动重载配置文件
* 高级过滤
* 插件



# SpringBoot 集成

SpringBoot 1.4 版本废弃了对于 log4j 的支持，默认使用 logback，在 spring boot 启动时就会加载 `LoggingApplicationListener` 来初始化日志工具

读取默认文件的流程

`LoggingApplicationListener#initialize` -> `LoggingApplicationListener#initializeSystem` -> `AbstractLoggingSystem#initialize` -> `AbstractLoggingSystem#initializeWithConventions` -> `AbstractLoggingSystem#getSpringInitializationConfig` + `LogbackLoggingSystem#getStandardConfigLocations` 