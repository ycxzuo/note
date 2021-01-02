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