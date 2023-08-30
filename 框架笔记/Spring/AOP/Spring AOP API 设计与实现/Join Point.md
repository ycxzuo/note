# 接入点接口 - Join Point

## Interceptor 执行上下文 - Invocation

* 方法拦截器执行上下文 - MethodInvocation
  * 基于反射 - ReflectiveMethodInvocatioin
    * 基于 CGLIB - CglibAopProxy.CglibMethodInvocation
* 构造器拦截器执行上下文 - ConstructorInvocation（Spring 未实现）

