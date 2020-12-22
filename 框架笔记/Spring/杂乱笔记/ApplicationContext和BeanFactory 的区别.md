# ApplicationContext 和 BeanFactory 的区别

## 共同点

都是 IoC 容器，并且 ApplicationContext 就是 BeanFactory

BeanFactory 提供了基本的 IoC 的能力



## 不同点

ApplicationContext 是 BeanFactory 的一个扩展，具有更多的企业级的能力，并且组合了 BeanFactory (org.springframework.context.support.AbstractRefreshableApplicationContext)

System.out.println(userRepository.getBeanFactory() == userRepository); // false
因为是组合关系，所以不等同

所以探究底层的时候用 getBeanFactory 而非直接替换

BeanDefinitionReader#loadBeanDefinitions 将 BeanDefinition 注册到 BeanFactory 中，其底层是调用 BeanDefinitionReaderUtils#registerBeanDefinition 方法注册，没有像 ConfigurableApplicationContext#refresh 方法内部 ConfigurableApplicationContext#registerBeanPostProcessors 阶段



## ApplicationContext 还具有其他功能

[参考资料](https://docs.spring.io/spring-framework/docs/5.2.11.RELEASE/spring-framework-reference/core.html#beans-introduction)

* 面向切面（AOP）
* 配置元信息（Configrution Metadata）
* 资源管理（Resources）
* 事件（Events）
* 国际化（i18n）
* 注解（Annotation）
* Environment 抽象（Environment Abstract）



AbstractApplicationContext 有两个实现

* GenericApplicationContext
  * 代表类 AnnotationConfigApplicationContext
  * 有一个 AtomicBoolean 类型的字段 refreshed 来保证 ApplicationContext 只会被 refresh 一次
* AbstractRefreshableApplicationContext
  * 代表类 AnnotationConfigWebApplicationContext 和 ClassPathXmlApplicationContext
  * 有一个 Boolean 类型的字段 allowBeanDefinitionOverriding 标志 BeanDefinition 是否可以被覆盖
  * 有一个 Boolean 类型的字段 allowCircularReferences 标志 BeanDefinition 是否支持循环引用