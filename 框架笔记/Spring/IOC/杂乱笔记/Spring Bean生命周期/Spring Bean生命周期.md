# Spring Bean 生命周期

## Spring Bean 元信息配置阶段

* `BeanDefinition` 配置
  * 面向资源
    * XML 配置：`org.springframework.beans.factory.xml.XmlBeanDefinitionReader`
    * Properties 资源配置：`org.springframework.beans.factory.support.PropertiesBeanDefinitionReader`
  * 面向注解
  * 面向 API



### Spring Bean 元信息解析阶段

* 面向资源 `BeanDefinition` 解析
  * `BeanDefinitionReader`
  * XML 解析器：`BeanDefinitionParser`
* 面向注解 `BeanDefinition` 解析
  * `AnnotatedBeanDefinitionReader`



## Spring Bean 注册阶段

* `BeanDefinition` 注册接口
  * `BeanDefinitionRegistry`

### 注意

在执行 `DefaultListableBeanFactory#registerBeanDefinition` 方法时，会判断 `hasBeanCreationStarted()` 是否为 true

* 如果是 false，则会加锁然后再注册 `BeanDefinition`，原因是在 IoC 容器初始化的时候，`BeanDefinition` 是先注册的，然后会将 BeanDefinition  保存在 一个 beanDefinitionMap 之中，并且用 beanDefinitionNames 保存这个 BeanDefinition 注册的顺序，最后增加一个冻结的标志位
* 如果是 true，则表示已经初始化过 `BeanDefinition`，这是后面用户在 BeanDefinition 已经注册过后，由重新注册 `BeanDefinition`，此时不知道是否是线程安全的，所以加锁，并且代码中会有这段注释 `// Cannot modify startup-time collection elements anymore (for stable iteration)`



## Srping BeanDefinition 合并阶段

* `BeanDefinition` 合并
  * 父子 `BeanDefiniiton` 合并
    * 当前 `BeanDefinition` 查找
    * 层次性 `BeanDefinition` 查找

### 注意

创建 `BeanDefinition` 的时候都是 `GenericBeanDeifinition`，merge 操作主要是 `AbstractBeanFactory#getMergedBeanDefinition(String)`然后获取其父 `BeanDefinition`，看有没有

* 有：拿到父的 BeanName，递归调用 `getMergedBeanDefinition` 方法合并，并把父的 `BeanDefinition` 拷贝设置为 `RootBeanDeifinition`（深拷贝），最后用子的 `BeanDefinition` 覆盖父的 `BeanDefinition`，子 `BeanDefinition` 没有的则保留父 `BeanDefinition`，最后拷贝 `GenericBeanDeifinition`  为 `RootBeanDeifinition` （深拷贝）
* 没有：则判断是不是 `RootBeanDeifinition`
  * 是：返回拷贝副本
  * 否：拷贝 `GenericBeanDeifinition`  为 `RootBeanDeifinition` （深拷贝）

将最后合并好的 `BeanDefinition` 放入缓存 mergedBeanDefinitions 字段中方便下次存取



## Spring Bean Class 加载阶段

* `ClassLoader` 类加载
* `Java Security` 安全机制
* `ConfigurableBeanFactory` 临时 `ClassLoader`(TempClassLoader)

`AbstractBeanDefinition` 的 beanClass 字段是 `Object` 类型的，在最初是 `String` 类型，然后会经过 `APPClassLoader` 加载，并将 `String` 类型的 beanClass 字段转换为 `Class` 类型，对应的解析 beanClass 的方法是 `AbstractBeanFactory#resolveBeanClass`

### 注意

ClassLoader 和 Class.forName 的区别

ClassLoader：加载类，但是不会导致类的初始化

Class.forName：加载类，并且初始化



## Spring Bean 实例化阶段（前）

非主流生命周期：Bean 实例化前阶段

* `InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation`

拦截实例化阶段，可以用来返回一个代理对象而跳过实例化阶段，如果返回 null 的话就会走之前默认的实例化过程，走 doCreateBean 方法



## Spring Bean 实例化阶段（中）

实例化方式

* 传统的实例化方式
  * 实例化策略：`InstantiationStrategy`
* 构造器依赖注入
  * `AbstractAutowireCapableBeanFactory#autowireConstructor` 

### 注意：

构造器注入查找顺序如下：

* 类型查找
* 通过 `@Qualifier` 进行过滤
* `@Primary` 优先， `@Priority` 其次，最后通过名称查找（字段或者参数名称），主要原因是 `DefaultListableBeanFactory#determineAutowireCandidate` 导致

该阶段主要实现是在 `AbstractAutowireCapableBeanFactory#doCreateBean` 返回一个 BeanWarpper，其中放的是 Bean 的构造参数

`doCreateBean` 的主要方法

* `AbstractAutowireCapableBeanFactory#populateBean` 给 BeanWrapper 中的 Bean 实例进行属性值填充
* `AbstractAutowireCapableBeanFactory#initializeBean` 实例化 Bean 的前置和后置操作
* `AbstractBeanFactory#registerDisposableBeanIfNecessary` 注册工厂关闭时的方法回调



## Spring Bean 实例化阶段（后）

Bean 属性赋值判断，要进行自动填充则返回 true ，要自己填充或者不需要初始化值就返回 false

* `InstantiationAwareBeanPostProcessor#postProcessAfterInstantiation`

`AbstractAutowireCapableBeanFactory#populateBean` 这个方法会判断 `postProcessAfterInstantiation` 的返回值是什么，默认是 true，并继续后续的赋值操作，但是如果是 false，则直接返回



## Spring Bean 属性赋值前阶段

* Bean 属性元信息
  * `PropertyValue`
* Bean 属性赋值前回调
  * Spring 1.2 ~ 5.0：`InstantiationAwareBeanPostProcessor#postProcessorPropertyValues`
  * spring 5.1：`InstantiationAwareBeanPostProcessor#postProcessorProperties`

赋值的地方在 `AbstractAutowireCapableBeanFactory#applyPropertyValues`



## Spring Bean Aware 接口回调阶段

* Spring Aware 接口
  * BeanNameAware `AbstractAutowireCapableBeanFactory#invokeAwareMethods`
  * BeanClassLoaderAware `AbstractAutowireCapableBeanFactory#invokeAwareMethods`
  * BeanFactoryAware `AbstractAutowireCapableBeanFactory#invokeAwareMethods`
  * EnvironmentAware
  * EmbeddedValueResolverAware
  * ResourceLoaderAware
  * ApplicationEventPublisherAware
  * MessageSourceAware
  * ApplicationContextAwar x vc c de

### 注意

在使用 BeanFactory 的时候，只会激活前三个 Spring Aware 回调

在使用 ApplicationContext 的时候，会激活所有的 Spring Aware 回调



## Spring Bean 初始化阶段（前）

* 已完成
  * Bean 实例化
  * Bean 属性赋值
  * Bean Aware 接口回调
* 方法回调
  
  * `BeanPostProcessor#postProcessorBeforeInitialization` 这个方法会判断 `postProcessorBeforeInitialization`  的返回值是什么，默认是实例化的 Bean，继续后续的赋值操作，但是如果是 null，则直接返回处理到该阶段的 Bean 对象
  
    



## Spring Bean 初始化阶段

Bean 初始化（Initialization）

* `@PostConstruct`
  * 此注解是 Java 标准的注解，它是在 `InitDestroyAnnotationBeanPostProcessor#postProcessorBeforeInitialization` 方法中执行（初始化前阶段），需要依赖有读取 `@PostConstruct` 注解能力的 `BeanFactory`（通过增加 `CommonAnnotationBeanPostProcessor` 来增加此能力）
* 实现 `InitializaingBean#afterPropertiesSet` 方法
  * `AbstractAutowireCapableBeanFactory#invokeInitMethods` 中 `((InitializingBean) bean).afterPropertiesSet()`
* 自定义初始化方法
  * `AbstractAutowireCapableBeanFactory#invokeInitMethods` 中 `invokeCustomInitMethod(beanName, bean, mbd)` 运用反射调用初始化方法





## Spring Bean 初始化阶段（后）

方法回调

* `BeanPostProcessor#postProcessorAfterInitialization` 这个方法会判断 `postProcessorAfterInitialization` 的返回值是什么，默认是实例化的 Bean，继续后续的赋值操作，但是如果是 null，则直接返回处理到该阶段的 Bean 对象

  

## Spring Bean 初始化完成阶段

方法回调

* Spring 4.1+：`SmartInitializingSingleton#afterSingletonsInstantiated` 这个方法会在 `ApplicationContext` 场景中，`ConfigurableApplicationContext#refresh` 方法的 `ConfigurableApplicationContext#finishBeanFactoryInitialization` 最后进行调用，该方法会触发所有的非延迟加载 Bean 的初始化，其作用是利用依赖查找方式（`AbstractBeanFactory#getBean`）按照 `BeanDefinition` 注册的顺序初始化每个非延迟加载的 Bean，最后判断是不是实现了 `SmartInitializingSingleton` 接口，如果实现了，则调用其 `SmartInitializingSingleton#afterSingletonsInstantiated` 方法对实例化后的 Bean 进行进一步操作



## Spring Bean 销毁前阶段

方法回调

* `DestructionAwareBeanPostProcessor#postProcessBeforeDestruction` 该方法需要注册到 `BeanFactory` 的 postProcessor 中，但是好像在调用 `AbstractApplicationContext#close` 方法时不会被回调



## Spring Bean 销毁阶段

Bean 销毁（destruction）

* `@preDestroy`
  * 此注解是 Java 标准的注解，它是在 `DestructionAwareBeanPostProcessor#postProcessBeforeDestruction` 方法中执行，需要依赖有读取 `@preDestroy` 注解能力的 `BeanFactory`（通过增加 `CommonAnnotationBeanPostProcessor` 来增加此能力）
* 实现 `DisposableBean#destroy` 方法
  * `DisposableBeanAdapter#destroy` 中 `((DisposableBean) this.bean).destroy();`
* 自定义初始化方法
  * `DisposableBeanAdapter#invokeCustomDestroyMethod` 中 `destroyMethod.invoke(this.bean, args);` 运用反射调用销毁方法



## Spring Bean 垃圾收集

Bean 垃圾回收（GC）

* 关闭 Spring 容器（应用上下文）
* 执行 GC
* Spring Bean 覆盖 finalize() 方法被回调



## 小结

* ApplicaitonContext 相关的 Aware 回调也是基于 BeanPostProcessor实现，即 ApplicationContextAwareProcessor
* BeanFactoryPostProcessor 必须有 Spring ApplicationContext 执行，无法直接与 BeanFactory 进行交互，而 BeanPostProcessor 则直接与 BeanFactory 关联，是 N ：1 的关系
* BeanFactory 处理 Bean 的过程
  1.注册 BeanDefinition：registerBeanDefinition()
  2.BeanDefinition 的合并阶段：getMergedLocalBeanDefinition()，比如 user 和 superUser 最后都变为 RootBeanDefinition
  3.创建 Bean：createBean()
  4.将 Bean 类型从 string 变为 class 类型：resolveBeanClass()
  5.Bean 实例化前工作：resolveBeforeInstantiation()，比如可以直接返回自定义的 Bean 对象
  6.开始实例化 Bean：doCreateBean()
  7.实例化 Bean：createBeanInstance()
  8.Bean实例化后：postProcessAfterInstantiation() 返回 false 即不再处理 Bean 属性
  9.Bean 属性赋值前对属性处理：postProcessProperties()
  10.Bean 属性赋值：applyPropertyValues()
  11.Bean 初始化阶段：initializeBean()
  12.初始化前 Aware 接口回调(非 ApplicationContextAware)：比如 BeanFactoryAware
  13.初始化前回调：applyBeanPostProcessorsBeforeInitialization()，比如 @PostConstructor
  14.初始化：invokeInitMethods()。比如实现 InitializingBean 接口的 afterPropertiesSet() 方法回调
  15.初始化后的回调：applyBeanPostProcessorsAfterInitialization()
  16.bean重新的填充覆盖来更新 Bean：preInstantiateSingletons()
  17.Bean销毁前：postProcessBeforeDestruction()
  18.Bean销毁：比如 @PreDestroy