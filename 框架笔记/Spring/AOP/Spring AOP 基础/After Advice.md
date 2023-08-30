# After Advice

## 注解配置 After Advice

* After Advice 注解
  * 方法返回后 - `@AfterReturning`
  * 异常发生后 - `@AfterThrowing`
  * finally 执行 - `@After`
* 默认执行顺序（重点，注意）
  * [5.2.7 及之后](https://docs.spring.io/spring-framework/docs/5.2.7.RELEASE/spring-framework-reference/core.html#aop-ataspectj-advice-ordering)
    * `@AfterThrowing`
    * `@After`
    * `@AfterReturning`
    * 只有代理方法异常才会触发 `@AfterThrowing` 的方法，否则不会出现
  * 5.2.7 之前
    * `@After`
    * `@AfterReturning`/`@AfterThrowing`
    * 无论是不是代理方法，即使是 before 中出现异常也会触发 `@AfterThrowing` 的方法



## XML 配置 After Advice

### 声明方式

```xml
<bean id="echoService" class="com.yczuoxin.spring.aop.overview.service.impl.DefaultEchoService"/>

<bean id="aspectXmlConfiguration" class="com.yczuoxin.spring.aop.feature.aspect.AspectXmlConfiguration"/>

<aop:config>
        <aop:aspect id="AspectXmlConfiguration" ref="aspectXmlConfiguration">
            <aop:pointcut id="publicMethod" expression="execution(* com.yczuoxin.spring.aop.overview.service.impl.DefaultEchoService.echo(..))"/>
            <aop:before method="beforePublicMethodPointcut" pointcut="execution(public * *(..))"/>
            <aop:after method="afterPublicMethodPointcut" pointcut-ref="publicMethod"/>
            <aop:around method="aroundPublicMethodPointcut" pointcut-ref="publicMethod"/>
            <aop:before method="beforePublicMethodPointcut" pointcut-ref="publicMethod"/>
            <aop:after-returning method="afterReturningPublicMethodPointcut" pointcut-ref="publicMethod"/>
            <aop:after-throwing method="afterThrowingPublicMethodPointcut" pointcut-ref="publicMethod"/>
        </aop:aspect>
    </aop:config>
```

### 属性配置

* pointcut
  * Pointcut 表达式内容
* pointcut-ref
  * Pointcut 表达式名称



### 默认执行顺序

* 根据声明的顺序会有各种奇怪的方法执行顺序



### 与 aop:around 及 aop:before 标签的执行顺序

默认情况下，before 和 aroud 中代理方法前执行的语句一定在 after 及 after 派生的方法之前执行，其余根据声明顺序执行



## API 配置 After Advice

核心接口 - org.springframework.aop.AfterAdvice

* 类型：标记接口
* 扩展
  * org.springframework.aop.AfterReturningAdvice
  * org.springframework.aop.ThrowsAdvice
* 接受对象 - org.springframework.aop.AdvisedSupport
  * 基础实现类 - org.springframework.aop.ProxyCreatorSupport
    * 常见实现类
      * org.springframework.aop.ProxyFactory
      * org.springframework.aop.ProxyFactoryBean
      * org.springframework.aop.aspectj.annotation.AspectJProxyFactory
