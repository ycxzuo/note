# ApplicationContext 和 BeanFactory 的区别

## 共同点

都是 IoC 容器，并且 ApplicationContext 就是 BeanFactory

BeanFactory 提供了基本的 IoC 的能力



## 不同点

ApplicationContext 是 BeanFactory 的一个扩展，具有更多的企业级的能力，并且组合了 BeanFactory (org.springframework.context.support.AbstractRefreshableApplicationContext)

System.out.println(userRepository.getBeanFactory() == userRepository); // false
因为是组合关系，所以不等同

所以探究底层的时候用 getBeanFactory 而非直接替换



## ApplicationContext 还具有其他功能

[参考资料](https://docs.spring.io/spring-framework/docs/5.2.11.RELEASE/spring-framework-reference/core.html#beans-introduction)

* 面向切面（AOP）
* 配置元信息（Configrution Metadata）
* 资源管理（Resources）
* 事件（Events）
* 国际化（i18n）
* 注解（Annotation）
* Environment 抽象（Environment Abstract）