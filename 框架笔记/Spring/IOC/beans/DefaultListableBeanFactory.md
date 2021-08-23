# DefaultListableBeanFactory

先来看 DefaultListableBeanFactory 的类层次关系图

![DefaultListableBeanFactory](http://tva1.sinaimg.cn/large/0060lm7Tly1g5r804a07tj31bu0g3my2.jpg)



## 各个类的作用

### AliasRegistry

接口，定义对 alias 的简单增删改查等操作

### BeanFactory

接口，定义获取 bean 及 bean 的各种属性的操作

### SingletonBeanRegistry

接口，定义对单例的注册及获取

### SimpleAliasRegistry

AliasRegistry 的实现类，使用 ConcuurentHashMap 做容器，别名 alias 为 key，bean 的 name 作为 value

```java
private final Map<String, String> aliasMap = new ConcurrentHashMap<>(16);
```

### BeanDefinitionRegistry

继承 AliasRegistry 接口，在 AliasRegistry 的基础上，增加了对于 BeanDefinition 的增删改查等操作

### ListableBeanFactory

继承 BeanFactory 接口，增加了根据不同条件获取 bean 列表的方式

### HierarchicalBeanFactory

继承 BeanFactory 接口，增加了对 parentBeanFactory 操作

### DefaultSingletonBeanRegistry

SingletonBeanRegistry 的实现类并且继承了 SimpleAliasRegistry，其中有很多 Map 和 Set 容器去放 bean，并实现了 SingletonBeanRegistry 各个函数

### ConfigurableBeanFactory

继承 HierarchicalBeanFactory/SingletonBeanRegistry 接口，提供配置 BeanFactory 相关操作的定义

### FactoryBeanRegistrySupport

继承 DefaultSingletonBeanRegistry 的抽象类，添加了对 FactoryBean 的操作功能

### AutowireCapableBeanFactory

继承 BeanFactory 接口，提供创建 bean、自动注入、初始化以及应用 bean 的后处理器

### AbstractBeanFactory

继承 FactoryBeanRegistrySupport，实现 ConfigurableBeanFactory 接口的抽象类，综合 FactoryBeanRegistrySupport 和 ConfigurableBeanFactory 的功能

### ConfigurableListableBeanFactory

继承 ListableBeanFactory、AutoWireCapableBeanFactory、ConfigurableBeanFactory 接口，BeanFactory 配置清单，指定忽略类型及接口等

### AbstractAutowireCapableFactoryBean

继承 AbstractBeanFactory 抽象类，实现 AutowireCapableFactoryBean 接口，