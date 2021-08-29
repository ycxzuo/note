# AOP 编程模型

## 注解驱动

* 激活：需要 `@EnableAspectJAutoProxy`
* Aspect：`@Aspect`
* Pointcut：`@Pointcut`
* Advice：`@Before` `@AfterReturning` `@AfterThrowing` `@After` `@Around`
* Introduction：`@DeclareParents`



## XML 配置驱动

对应实现 Spring Extensble XML Authoring

* 激活：\<aop:aspectj-autoproxy/>
* 配置：\<aop:config/>
* Aspect：\<aop:aspectj-aspect/>
* Pointcut：\<aop:pointcut/>
* Advice：\<aop:around/> \<aop:before/> \<aop:returning/> \<aop:throwing/> \<aop:after/>
* Introduction：\<aop:declare-parents/>
* 代理 Scope：\<aop:scoped-proxy/>



## Java API