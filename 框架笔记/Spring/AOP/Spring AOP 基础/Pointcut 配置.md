# Pointcut 配置

## Annotation 配置

```java
@Aspect
public class AspectConfiguration {

    @Pointcut("execution(public * *())")
    private void publicMethodPointcut() {}

    @Before("publicMethodPointcut()")
    public void beforePublicMethodPointcut() {
        System.out.println("before public method pointcut");
    }
}
```

```java
@Configuration
@EnableAspectJAutoProxy
public class AspectJAnnotatedPointcutDemo {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(AspectConfiguration.class, AspectJAnnotatedPointcutDemo.class);
        context.refresh();
        AspectJAnnotatedPointcutDemo bean = context.getBean(AspectJAnnotatedPointcutDemo.class);
        bean.print();
        context.close();
    }

    public void print() {
        System.out.println("print method execute...");
    }

}
```



## XML 配置

```xml
    <aop:aspectj-autoproxy/>

    <bean id="echoService" class="com.yczuoxin.spring.aop.overview.service.impl.DefaultEchoService"/>

    <bean id="aspectXmlConfiguration" class="com.yczuoxin.spring.aop.feature.aspect.AspectXmlConfiguration"/>

    <aop:config>
        <aop:aspect id="AspectXmlConfiguration" ref="aspectXmlConfiguration">
            <aop:pointcut id="publicMethod" expression="execution(public * *(..))"/>
            <aop:before method="beforePublicMethodPointcut" pointcut-ref="publicMethod"/>
        </aop:aspect>
    </aop:config>
```



## API 配置

* 核心 API - org.springframework.aop.Pointcut
  * org.springframework.aop.ClassFilter
  * org.springframework.aop.MethodMatcher
* 适配实现 - DefaultPointcutAdvisor

