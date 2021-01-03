# Spring 国际化

## Spring 国际化使用场景

* 普通国际化文案
* Bean Validation 校验国际化文案
* Web 站点页面渲染
* Web MVC 错误消息提示



## Spring 国际化接口

* 核心接口
  * org.springframework.context.MessageSource
    * org.springframework.context.support.ResourceBundleMessageSource
    * org.springframework.context.support.ReloadableResourceBundleMessageSource
    * org.springframework.context.support.StaticMessageSource
* 主要概念
  * 文案模板编码（code）
  * 文案模板参数（args）
  * 区域（Locale）
  



## 层次性 MessageSource

* Spring 层次性接口回顾
  * org.springframework.beans.factory.HierachicalBeanFactory
  * org.springframework.context.ApplicationContext
  * org.springframework.bean.factory.config.BeanDefinition
* Spring 层次性国际化接口
  * org.springframework.context.HierachicalMessageSource



## Java 国际化标准实现

* 核心接口
  * 抽象实现：java.util.ResourceBundle
    * 核心方法：java.util.ResourceBundle#getBundleImpl
      * baseName：基准名称
      * locale：地区
      * loader：类加载器，由于 ClassLoader 有自己加载的范围
  * Properteis 资源实现：java.util.PropertyResourceBundle
    * 只能传入编码为 ISO-8859-1 的配置文件 
  * 举例实现：java.util.ListResourceBundle
    * 利用二维数组的方式硬编码存储
* ResourceBundle 核心特性
  * Key - Value 设计
  * 层次性设计
  * 缓存设计
  * 字符编码控制：`java.util.ResourceBundle.Control` (@since 1.6)
  * Control SPI 扩展：`java.util.spi.ResourceBundleControlProvider` (@since 1.8)



## Java 文本格式化

* 核心接口
  * java.text.messageFormat
* 基本用法
  * 设置消息格式模式：new MessageFormat(...)
  * 格式化：format(new Object[]{...})

* 消息格式模式
  * 格式元素：{ArgumentIndex(, FormatType,  (ForamtStyle))}
  * FormatType：消息格式类型，可选项，每种类型在 number、date、time 和 choice 类型选其一
  * FormatStyle：消息各式风格，可选项，包括：short、medium、long、full、integer、currency、 pecent
* 高级用法
  * 重置消息格式模式：
    * java.text.MessageFormat#applyPattern
  * 重置 java.util.Locale
    * java.text.MessageFormat#setLocale
    * java.text.MessageFormat#applyPattern
  * 重置 java.text.Format
    * java.text.MessageFormat#setFormat



## MessageSource 开箱即用实现

* 基于 ResourceBundle + MessageFormat 组合 MessageSource 实现
  * org.springframework.context.support.ResourceBundleMessageSource
* 可重载 Properties + MessageForamt 组合 MessageSource 实现
  * org.springframework.context.support.ReloadableResourceBundleMessageSource
* 静态编程式增加到 `Map<String, Map<Locale, MessageHolder>>` 中的 MessageSource 实现
  * org.springframework.context.support.StaticMessageSource

其重要方法主要是 `org.springframework.context.support.AbstractMessageSource#getMessage(java.lang.String, java.lang.Object[], java.util.Locale)` 获取，其中这两个开箱即用的分支在 `org.springframework.context.support.AbstractMessageSource#resolveCode` 方法中



## MessageSource 内建依赖

MessageSource 内建 Bean 可能来源

* 预注册 Bean 名称为："messageSource" 类型为 MessageSource Bean
* 默认内建实现：DelegatingMessageSource
  * 层次性查找 MessageSource 对象

ApplicationContext 本身就实现了 MessageSource 接口，Spring 注册 MessageSource 是地方在：`org.springframework.context.support.AbstractApplicationContext#initMessageSource`



## 扩展

### Spring Boot 为什么要预创建 MessageSource Bean

* AbstractApplicationContext 的实现决定了 MessageSource 内建实现
* Spring Boot 通过外部化配置简化 MessageSource Bean 构建
* Spring Boot 基于 Bean Validation 校验非常普遍



Spring Boot 自动化装配的时候，配置 MessageSource 的类为 `org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration`，其注入配置的先前条件有两个

* 当前 ApplicationContext 中（search = SearchStrategy.CURRENT）没有名称为 `messageSource` 的 Bean

* 满足 ResourceBundleCondition 的条件

  * 在 spring.messages.basename 外部化配置中配置了 MessageSource 的资源路径，默认是 classpath 下的 message.properties

    ```java
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String basename = context.getEnvironment().getProperty("spring.messages.basename", "messages");
        ConditionOutcome outcome = cache.get(basename);
        if (outcome == null) {
            outcome = getMatchOutcomeForBasename(context, basename);
            cache.put(basename, outcome);
        }
        return outcome;
    }
    ```

    返回的 ConditionOutcome 带有是否满足条件的属性，如果没找到文件，返回 false，不满足加载 MessageSource 的先前条件，如果满足，则加载 ResourceBundleMessageSource 作为 MessageSource



不满足加载条件，会找父 beanFactory 中国年是否有 MessageSource，如果也没有的话，会生成一个空的实现

```java
DelegatingMessageSource dms = new DelegatingMessageSource();
dms.setParentMessageSource(getInternalParentMessageSource());
this.messageSource = dms;
            ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
// 如果是默认的 empty messageSource 的话，是不会有 BeanDefinition 存在的，org.springframework.beans.factory.config.SingletonBeanRegistry.registerSingleton 注册的 Bean 没有 BeanDefinition，其生命周期不由 Spring 管理
beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
if (logger.isTraceEnabled()) {
    logger.trace("No '" + MESSAGE_SOURCE_BEAN_NAME + "' bean, using [" + this.messageSource + "]");
}
```



## 实现配置自动更新 MessageSource

主要技术

* Java NIO 2：java.nio.file.WatchService
* Java Concurrency：java.util.concuurent.ExecutorService
* Spring：org.springframework.context.support.AbstractMessageSource