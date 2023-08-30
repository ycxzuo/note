# Join Point 执行动作接口 - Advice

## Around Advice - Interceptor

* 方法拦截器 - MethodInterceptor
* 构造器拦截器 - ConstructInterceptor



## Before Advice

### 标准实现

* 接口
  * 标准接口 - BeforeAdvice
  * 方法级别 - MethodBeforeAdvice
* 实现
  * MethodBeforeAdviceInterceptor

将 `MethodBeforeAdvice` 注入 `MethodBeforeAdviceInterceptor`，实际是调用 `MethodBeforeAdviceInterceptor` 的 invoke 方法回调了 `MethodBeforeAdvice` 的 before 方法



### AspectJ 实现

* 实现
  * AspectJMethodBeforeAdvice

Spring 自己实现的，是基于 Java 的反射来找到注解中的元信息，最后进行调用



## After Advice

### 标准实现

* 接口
  * AfterAdvice
  * AfterReturningAdvice
  * ThrowsAdvice
* 实现
  * ThrowsAdviceInterceptor
    * 方法签名必须是 public void afterThrowing()，入参是一个参数或者四个参数才行，顺序也不能错，相同的异常类型，后者会覆盖前者（HashMap 存储，key 为异常的类型，如果没有正好匹配的，则从异常的父类开始匹配），例如
      * public void afterThrowing(Exception ex)
      * public void afterThrowing(RemoteException)
      * public void afterThrowing(Method method, Object[] args, Object target, Exception ex)
      * public void afterThrowing(Method method, Object[] args, Object target, ServletException ex)
  * AfterRuturningAdviceInterceptor

