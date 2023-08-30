# Before Advice

## 注解配置 Before Advice

`@Before`

### 与 `@Around` 注解的执行顺序

默认情况下，`@Around` 的执行顺序要比 `@Before` 的优先，但是可以通过 @Order 注解或者实现 Ordered 接口调整执行顺序



## XML 配置 Before Advice

### 声明方式

```xml
<bean id="echoService" class="com.yczuoxin.spring.aop.overview.service.impl.DefaultEchoService"/>

<bean id="aspectXmlConfiguration" class="com.yczuoxin.spring.aop.feature.aspect.AspectXmlConfiguration"/>

<aop:config>
    <aop:aspect id="AspectXmlConfiguration" ref="aspectXmlConfiguration">
        <aop:pointcut id="publicMethod" expression="execution(* com.yczuoxin.spring.aop.overview.service.impl.DefaultEchoService.echo(..))"/>
        <aop:before method="beforePublicMethodPointcut" pointcut="execution(public * *(..))"/>
        <aop:around method="aroundPublicMethodPointcut" pointcut-ref="publicMethod"/>
        <aop:before method="beforePublicMethodPointcut" pointcut-ref="publicMethod"/>
    </aop:aspect>
</aop:config>
```

### 属性配置

* pointcut
  * Pointcut 表达式内容
* pointcut-ref
  * Pointcut 表达式名称

### 与 aop:around 标签的执行顺序

默认情况下，按照声明的顺序执行



## API 配置 Before Advice

### 核心接口 BeforeAdvice

* 类型
  * 标记接口
* 方法 JoinPoint 扩展
  * MethodBeforeAdvice
* 接收对象 AdvisedSupport
  * 基础实现类 ProxyCreatorSupport
    * 常见实现类
      * ProxyFactory
      * ProxyFactoryBean
      * AspectJProxyFactory





