# 创建代理 AspectJ

## @AspectJ 注解驱动

### 激活 @AspectJ 模块

* 注解激活
  * `@EnableAspectJAutoProxy`
* XML 配置
  * <aop: aspectj-autoproxy>



### 声明 Aspect

`@Aspect`



## 编程式创建 @AspectJ 代理

实现类

`org.springframework.aop.aspectj.annotation.AspectJProxyFactory`
