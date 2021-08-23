# Spring Bean作用域

| 来源        | 说明                                                       |
| ----------- | ---------------------------------------------------------- |
| singleton   | 默认 Spring Bean 作用域，一个 BeanFactory 有且仅有一个实例 |
| prototype   | 原型作用域，每次依赖查找和依赖注入生成新的 Bean 对象       |
| request     | 将 Spring Bean 存储在 ServletRequest 上下文中              |
| session     | 将 Spring Bean 存储在 HttpSession 中                       |
| application | 将 Spring Bean 存储在 ServletContext 中                    |



## singleton Bean 作用域

其实都是 `BeanDefinition` 的一个属性，可以用 `isSingleton()` 来返回是不是单例的 Bean，此处 `isSingleton()` 与 `isPrototype()` 是两个方法，其实现是 `AbstractBeanDefinition` 中的 scope 字段的值来决定的，如果是 SCOPE_DEFAULT("") 或者 SCOPE_SINGLETON("singleton") 便是单例 Bean，如果是 SCOPE_PROTOTYPE("prototype") 就是原型 Bean



## prototype Bean 作用域

* 单一对象
  * singleton Bean 无论是依赖查找还是依赖注入，都是唯一的 Bean
  * prototype Bean 无论是依赖查找还是依赖注入，都是新生成的 Bean
* 集合对象
  * 使用依赖注入集合类型，singleton Bean 和 prototype Bean 都会存在一个，并且 prototype Bean 是重新生成的一个

### 注意事项

* Spring 容器没有办法管理 prototype Bean 的完整生命周期，也没有办法记录实例的存在。销毁回调方法将不会执行，可以利用 `BeanPostProcessor` 进行清理工作，方式是在使用这个 Bean 的销毁回调方法中进行销毁
* 无论是 singleton Bean 还是 prototype Bean，都会执行初始化方法回调，singleton Bean 会执行销毁方法回调，prototype Bean 不会执行销毁方法回调



## request Bean 作用域

* 配置
  * XML：<bean class="..." scope="request">
  * Java 注解：`@RequestScope` 或 `@Scope`（`WebApplicationContext.SCOPE_REQUEST`）
* 实现
  * API：`RequestScope`

解析过程在 `AbstractRequestAttributesScope` 的 get 方法中，每次 Request 请求都会生成一个新的 Bean



## session Bean 和 application Bean

session Bean 和 application Bean 其实跟 request 相似，就不单独讨论了



## 自定义 Bean 作用域

1. 实现 Scope

   1. `org.springframework.beans.factory.config.Scope`

2. 注册 Scope

   方式一：API `org.springframework.beans.factory.config.ConfigurableBeanFactory#registerScope`

   方式二：配置

   ```XML
   <bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
   	<property name="scopes">
           <map>
               <entry key="...">
               </entry>
           </map>
       </property>
   </bean>
   ```

   

