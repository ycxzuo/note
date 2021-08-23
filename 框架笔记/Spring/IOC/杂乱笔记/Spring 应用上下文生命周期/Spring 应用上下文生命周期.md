# Spring 应用上下文生命周期

## Spring 应用上下文启动准备阶段

AbstractApplicationContext#prepareRefresh() 方法

* 启动时间：startupDate
* 状态标识：closed(false)、active(true)
* 初始化 PropertySource：initPropertySources()
* 检查 Environment 中必须属性
* 初始化事件监听器集合
* 初始化早期 Spring 事件集合



## BeanFactory 创建阶段

* AbstractApplicationContext#obtainFreshBeanFactory() 方法

  * 刷新 Spring 应用上下文底层 BeanFactory：refreshBeanFactory()
    * 创建 BeanFactory：createBeanFactory()
    * 设置 BeanFactory id
    * 设置“是否允许 BeanDefinition 重复定义”：customizeBeanFactory(DefaultListableBeanFactory)
    * 设置"是否允许循环引用（依赖）"：customizeBeanFactory(DefaultListableBeanFactory)
    * 加载 BeanDefinition：loadBeanDefinitions(DefaultListableBeanFactory) 方法
    * 关联新建 BeanFactory 到 Spring 应用上下文
  * 返回 Spring 应用上下文底层 BeanFactory：getBeanFactory()

  

## BeanFactory 准备阶段

* AbstractApplicationContext#prepareBeanFactory(ConfigurableListableBeanFactory) 方法
  * 关联 ClassLoader
  * 设置 Bean 表达式处理器
  * 添加 PropertyEditorRegistrar 实现：ResourceEditorRegistrar
  * 添加 Aware 回调接口 BeanPostProcessor 实现：ApplicationContextAwareProcessor
  * 忽略 Aware 回调接口作为依赖注入接口
  * 注册 ResolvableDependency 对象：BeanFactory、ResourceLoader、ApplicationEventPublisher 以及 ApplicationContext
  * 注册 ApplicationListenerDetector 对象
  * 注册 LoadTimeWeaverAwareProcessor 对象
  * 注册单例对象：Environment、Java System Properties 以及 OS 环境变量



## BeanFactory 后置处理阶段

* AbstractApplicationContext#postProcessorBeanFactory(ConfigurableListableBeanFactory) 方法
  * 由子类覆盖该方法
* AbstractApplicationContext#invokeBeanFactoryPostProcessor(ConfigurableListableBeanFactory) 方法
  * 调用 BeanFactoryProcessor 或 BeanDefinitionRegistry 后置处理方法
    * 处理当前上下文的 BeanDefinitionRegistryPostProcessor
    * 处理 BeanFactory 的 BeanDefinitionRegistryPostProcessor（优先级：PriorityOrdered > Ordered > 无）
    * 处理当前上下文的 BeanFactoryPostProcessor
    * 处理 BeanFactory 的 BeanFactoryPostProcessor（优先级：PriorityOrdered > Ordered > 无）
  * 注册 LoadTimeWeaverAwareProcessor 对象



## BeanFactory 注册 BeanProcessor 阶段

* AbstractApplicationContext#registerBeanPostProcessors(ConfigurableListableBeanFactory) 方法
  * 注册 PriorityOrdered 类型的 BeanPostProcessor Beans
  * 注册 Ordered 类型的 BeanPostProcessor Beans
  * 注册普通 BeanPostProcessor Beans
  * 注册 MergedBeanDefinitionPostProcessor Beans
  * 注册 ApplicationListenerDetector 对象



## 初始化内建 Bean：MessageSource

* AbstractApplicationContext#initMessageSource() 方法
  * MessageSource 内建 Bean 可能来源

    * 预注册 Bean 名称为："messageSource" 类型为 MessageSource Bean
    * 默认内建实现：DelegatingMessageSource
      * 层次性查找 MessageSource 对象



## 初始化内建 Bean：Spring 事件广播器

* AbstractApplicationContext#initApplicationEventMulticaster() 方法
  * 查找名为 `applicationEventMulticaster` 的 Bean
    * 如果有，则设置为 Spring 的事件广播器
    * 如果没有，则使用 SimpleApplicationEventMulticaster 作为 Spring 的事件广播器



## Spring 应用上下文刷新阶段

* AbstractApplicationContext#onfresh() 方法
  * 子类覆盖该方法
    * AbstractRefreshableWebApplicationContext#onRefresh
    * GenericWebApplicationContext#onRefresh
      * ServletWebServerApplicationContext#onRefresh
    * ReactiveWebServerApplicationContext#onRefresh
    * StaticWebApplicationContext#onRefresh



## Spring 事件监听器注册阶段

* AbstractApplicationContext#registerListeners() 方法
  * 添加当前应用上下文所关联的 ApplicationListener 对象（集合）
  * 添加 BeanFactory 所注册的 ApplicationListener Beans
  * 广播早期 Spring 事件



## BeanFactory 初始化完成阶段

* AbstractApplicationContext#finishBeanFactoryInitialization(ConfigurableListableBeanFactory) 方法
  * BeanFactory 关联 ConversionService Bean，如果存在
  * 添加 StringValueResolver 对象
  * 依赖查找 LoadTimeWeaverAware Bean
  * BeanFactory 临时 Classloader 置为 null
  * BeanFactory 冻结配置
  * BeanFactory 初始化非延迟单例 Beans



## Spring 应用上下文刷新完成阶段

* AbstractApplicationContext#finishRefresh() 方法
  * 清除 ResourceLoader 缓存：clearResourceCaches() @since 5.0
  * 初始化 LifecycleProcessor 对象： initLifecycleProcessor()
  * 调用 LifecycleProcessor#onReFresh() 方法
  * 发布 Spring 应用上下文已刷新事件：ContextRefreshedEvent
  * 向 MBeanServer 托管 Live Beans



## Spring 应用上下文启动阶段

* AbstractApplicationContext#start() 方法
  * 启动 LifecycleProcessor
    * 依赖查找 Lifecycle Beans
    * 启动 Lifecycle Beans
  * 发布 Spring 应用上下文已启动事件：ContextStartedEvent



## Spring 应用上下文停止阶段

* AbstractApplicationContext#stop() 方法
  * 停止 LifecycleProcessor
    * 依赖查找 Lifecycle Beans
    * 停止 Lifecycle Beans
  * 发布 Spring 应用上下文已停止事件：ContextStoppedEvent



## Spring 应用上下文关闭阶段

* AbstractApplicationContext#close() 方法
  * 状态标识：active(false)、closed(true)
  * Live Beans JMX 撤销托管
    * LiveBeansView,unregisterApplicationContext(ConfigurableApplicationContext)
  * 发布 Spring 应用上下文已关闭事件：ContextClosedEvent
  * 关闭 LifecycleProcessor
    * 依赖查找 Lifecycle Beans
    * 停止 Lifecycle Beans
  * 销毁 Spring Beans
  * 关闭 BeanFactory
  * 回调 onClose()
  * 移除 Shutdown Hook 线程（如果曾注册）



## 其他

在 Spring 应用上下文调用 refresh 之前，可以自定义一些组件的初始化，比如自定义 Environmen，也可以使用 SpringApplicationContextInitializer#initialize(ConfigurableApplicationContext) 自定义 ConfigurableApplicationContext 的初始化，在 Spring Boot 中用的比较多