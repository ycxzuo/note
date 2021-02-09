# Spring 注解

## Spring 核心注解场景分类

* Spring 模式注解

| Spring 注解    | 场景说明           | 起始版本 |
| -------------- | ------------------ | -------- |
| @Repository    | 数据仓储模式注解   | 2.0      |
| @Component     | 通用组件模式注解   | 2.5      |
| @Service       | 服务模式注解       | 2.5      |
| @Controller    | Web 控制器模式注解 | 2.5      |
| @Configuration | 配置类模式注解     | 3.0      |

* 装配注解

| Spring 注解     | 场景说明                                    | 起始版本 |
| --------------- | ------------------------------------------- | -------- |
| @ImportResource | 替换 XML 元素 \<import>                     | 3.0      |
| @Import         | 导入 Configuration 类                       | 3.0      |
| @ComponentScan  | 扫描指定 package 下标注 Spring 模式注解的类 | 3.1      |

* 依赖注入注解

| Spring 注解 | 场景说明                            | 起始版本 |
| ----------- | ----------------------------------- | -------- |
| @Autowired  | Bean 依赖注入，支持多种依赖查找方式 | 2.5      |
| @Qualifier  | 细粒度的 @Autowired 依赖查找        | 2.5      |



## Spring 注解编程模型

* 编程模型
  * 元注解（Meta-Annotation）
  * Spring 模式注解（Stereotype Annotations）
  * Spring 组合注解（Composed Annotations）
  * Spring 注解属性别名和覆盖（Attribute Aliases and Overrides）



## Spring 元注解（Meta-Annotation）

* 举例
  * java.lang.annotation.Documented
  * java.lang.annotation.Inherited
  * java.lang.annotation.Repeatable



## Spring 模式注解（Stereotype Annotations）

* 理解 @Component “派生性”
  * 元标注 @Component 的注解在 XML 元素 \<component-scan> 或注解 @ComponentScan 扫描中”派生“了 @Component 的特性，并且从 Spring Framework 4.0 开始支持多层次”派生性“
* 举例
  * @Repository
  * @Service
  * @Controller
  * @Configuration
  * @SpringBootConfiguration（Spring Boot）
* @Component “派生性”原理
  * 核心组件：org.springframework.context.annotation.ClassPathBeanDefinitionScanner
    * org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
  * 资源处理：org.springframework.core.io.support.ResourcePatternResolver
  * 资源类元信息
    * org.springframework.core.type.classreading.MetadataReaderFactory
  * 类元信息：org.springframework.core.type.ClassMetadata
    * ASM 实现：org.springframework.core.type.classreading.ClassMetadataReadingVisitor
    * 反射实现：org.springframework.core.type.StandardAnnotationMetadata
  * 注解元信息：org.springframework.core.type.AnnotationMetadata
    * ASM 实现：org.springframework.core.type.classreading.AnnotationMetadataReadingVisitor
    * 反射实现：org.springframework.core.type.StandardAnnotationMetadata



## Spring 组合注解（Composed Annotations）

基本定义

* Spring 组合注解（Composed Annotations）中的注解允许是 Spring 模式注解与其他 Spring 功能性注解的任意组合



## Spring 注解属性别名（Attribute Aliases）

* 特性
  * 显性别名
  * 隐性别名
  * 传递性别名
* 工作原理
  * 接口：org.springframework.core.annotation.MergedAnnotations

*注意*：如果单独使用 getClass().getAnnotation(A.class);



## Spring 注解属性覆盖（Attribute Overrides）

* 特性
  * 显性覆盖
  * 传递性覆盖



## Spring @Enable 模块驱动

* @Enable 模块驱动
  * @Enable 模块驱动是以 @Enable 为前缀的注解驱动编程模型。所谓“模块”是指具备相同领域的功能组件集合，组合所形成一个独立的单元。比如 Web MVC 模块、AspectJ 代理模块、Caching（缓存）模块、JMX(Java 管理扩展)模块、Async（异步处理）模块等
* 举例说明
  * @EnableWebMvc
  * @EnableTransactionMannagement
  * @EnaleCaching
  * @EnaleMBeanExport
  * @EnableAsync
* @Enable 模块驱动编程模式
  * 驱动注解：@EnableXXX
  * 导入注解：@Import 具体实现
  * 具体实现
    * 基于 Configuration Class
    * 基于 ImportSelector
    * 基于 ImportBeanDefinitionRegistrar 接口实现



## Spring 条件注解

* 基于配置的条件注解：@org.springframework.context.annotation.Profile
  * 关联对象：org.springframework.core.env.Environment 中的 Profiles
  * 实现变化：从 Spring 4.0 开始，@Profile 基于 @Conditional 实现
* 基于编程的条件注解：@org.springframework.context.annotation.Conditional
  * 关联对象：org.springframework.context.annotation.Condition 具体实现
* @Conditional 实现原理
  * 上下文对象：org.springframework.context.annotation.ConditionContext
  * 条件判断：org.springframework.context.annotation.ConditionEvaluator
  * 配置阶段：org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase
  * 判断入口：org.springframework.context.annotation.ConfigurationClassPostProcessor
    * org.springframework.context.annotation.ConfigurationClassParser



## 课外资料

* Spring Boot 注解

| 注解                     | 场景说明                 | 起始版本 |
| ------------------------ | ------------------------ | -------- |
| @SpringBootConfiguration | Spring Boot 配置类       | 1.4.0    |
| @SpringBootApplication   | Spring Boot 应用引导类   | 1.2.0    |
| @EnableAutoConfiguration | Spring Boot 激活自动装配 | 1.0.0    |

* Spring Cloud 注解

| 注解                    | 场景说明                            | 起始版本 |
| ----------------------- | ----------------------------------- | -------- |
| @SpringCloudApplication | Spring Cloud 应用引导注解           | 1.0.0    |
| @EnableDiscoverClient   | Spring Cloud 激活服务发现客户端注解 | 1.0.0    |
| @EnableCircultBreaker   | Spring Cloud 激活熔断注解           | 1.0.0    |

