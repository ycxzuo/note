# BeanDefinition 概述

## 概念

BeanDefinition 是 Spring Framework 中定义 Bean 的配置元信息接口，其包括

* Bean 的类名（具体实现类）
* Bean 行为配置元素，如作用域、自动绑定的模式、生命周期回调等
* 其他 Bean 引用，又可称作合作者（Collaborators）或者依赖
* 配置设置，比如 Bean 属性（例如数据源的一些配置值）



## 元信息

| 属性                     | 说明                                          |
| ------------------------ | --------------------------------------------- |
| Class                    | Bean 全类名，必须是具体类，不能是接口或抽象类 |
| Name                     | Bean 的名称或者 ID                            |
| Scope                    | Bean的作用域（如：singleton、propotype）      |
| Constructor arguments    | Bean 构造器参数（用于依赖注入）               |
| Properties               | Bean 属性设置（用于依赖注入）                 |
| Autowiring mode          | Bean 自动帮绑定模式（如：通过名称byName）     |
| Lazy initialization mode | Bean 延迟初始化模式（延迟和非延迟）           |
| Initialization method    | Bean 初始化回调方法名称                       |
| Destruction method       | Bean 销毁回调方法名称                         |



## 构建方式

* BeanDefinitionBuilder
* GenericBeanDefinition



## Spring Bean 命名

* DefaultBeanNameGenerator
* AnnotationBeanNameGenerator



## Spring Bean 别名

### 价值

* 复用现有的 BeanDefinition

* 更具有场景化的命名方法，例如

  ```xml
  <alias name="myApp-dataSource" alias="subsystemA-dataSource"/>
  <alias name="myApp-dataSource" alias="subsystemB-dataSource"/>
  ```

  

## Bean Definition 注册

* XML 配置元信息
  * `<bean name="..." class="..." ...>`
* Java 注解配置元信息
  * @Bean
  * @Component
  * @Import
* Java API 配置元信息
  * 命名方式：BeanDefinitionRegistry#registerBeanDefinition(String, BeanDefinition)
  * 非命名方式：BeanDefinitionReaderUtils#registerWithGeneratedName(AbstrctBeanDefinition, BeanDefinitionRegistry)
  * 配置类方式：AnnotationBeanDefinitionReader#register(Class...)



## Bean 实例化

* 常规方式
  * 通过构造器（配置元信息：XML、Java 注解和 Java API）
  * 通过静态工厂方法（配置元信息：XML 和 Java API）
  * 通过 Bean 工厂方法（配置元信息：XML 和 Java API）
  * 通过 FactoryBean（配置元信息：XML、Java 注解和 Java API）
* 特殊方式
  * 通过 ServiceLoaderFactoryBean（配置元信息：XML、Java 注解和 Java API）
  * 通过 AutoWireCapableBeanFactory#createBean（java.lang.Class, int, boolean）
  * 通过 BeanDefinitionRegistry#registerBeanDefinition（String, BeanDefinition）



## Bean 的初始化

* @PostConstruct 标注方法（Java 标准）
* 实现 InitailizingBean 接口 afterPropertiesSet() 方法
* 自定义初始化方法
  * XML 配置：<bean init-method="init" ... />
  * Java 注解：@Bean(initMethod=“init”)
  * Java API：AbstractBeanDefinition#setInitMethodName(String)

执行顺序是从上到下，自定义方法基本最后都是使用 AbstractBeanDefinition#setInitMethodName(String) set 进 BeanDefinition 的



## Bean 延迟初始化

* XML 配置：<bean lazy-init="true" .../>
* Java 注解：@Lazy(true)



## Bean 销毁

* @PreDestroy 标注方法（Java 标准）
* 实现 DisposableBean 接口的 distroy() 方法
* 自定义销毁方法
  * XML 配置：<bean destroy-method="destroy" ... />
  * Java 注解：@Bean(destroy="destroy")
  * Java API：AbstractBeanDefinition#setDestroyMethodName(String)

执行顺序是从上到下，自定义方法基本最后都是使用 