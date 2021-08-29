# Spring AOP 代理实现

## JDK 动态代理实现

基于接口的代理 - JDKDynamicAopProxy



## CGLIB 动态代理实现

基于类代理（字节码提升）- CGLibAopProxy



## AspectJ 适配

AspectJProxyFactory

### 语法

* Aspect
* Join Points
* Pointcuts
  * 切入点筛选
  * `pointcut move() : call(void Point.setX(int)) || call(void Point.setY(int))`
* Advice
  * 筛选后进行动作
  * `before(): move(){System.out.println("success")}`
* Introduction



## Spring AOP 和 AspectJ 的区别

1. AspectJ 是对 AOP 功能的全部实现，而 Spring 只是部分实现
2. Spring AOP 仅支持代理的 AOP 实现，字段级别就无法实现
3. Spring AOP 仅支持方法级别的 AOP，AspectJ 支持字段级别
