# Around Advice

## 注解配置 Around Advice

```java
@Around("publicMethodPointcut()")
public Object aroundPublicMethodPointcut(ProceedingJoinPoint joinPoint) throws Throwable {
    System.out.println("before public method pointcut");
    return joinPoint.proceed();
}
```

必须调用 `joinPoint.proceed()` 才会执行代理方法



## XML 配置 Around Advice

```xml
<aop:config>
	<aop:around method="aroundMethod" pointcut-ref="publicMethod" />
</aop:config>

```



## API 配置 Around Advice

由于 `@Around` 的含义与 `MethodInteceptor` 相同，所以没有给与其他特定的 API 实现 
