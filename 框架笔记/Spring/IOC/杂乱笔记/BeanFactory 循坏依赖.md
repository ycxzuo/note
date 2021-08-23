# BeanFactory 循坏依赖

* 循环依赖开关（方法）：AbstractAutowireCapableBeanFactory#setAllowCircularReferences
* 单例工程（属性）：DefaultSingletonBeanRegistry#singletonFactories
* 获取早期未处理 Bean（方法）：AbstractAutowireCapableBeanFactory#getEarlyBeanReference
* 早期未处理 Bean（属性）：DefaultSingetonBeanRegistry#earlySingletonObjects

## 三个 Map 的功能

* `singletonObjects`（一级 Map），里面保存了所有已经初始化好的单例 Bean
* `earlySingletonObjects`（二级 Map），里面会保存从三级 Map 获取到的正在初始化的 Bean，保存的同时会移除三级 Map 中对应的 ObjectFactory 实现类，在完全初始化好某个 Bean 时会移除二级 Map 中对应的早期对象
* `singletonFactories`（三级 Map），里面保存了正在初始化的 Bean 对应的 ObjectFactory 实现类，调用其 getObject() 方法返回正在初始化的 Bean 对象（仅实例化还没完全初始化好）