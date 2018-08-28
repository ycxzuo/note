# Spring Boot之SpringApplication

快速创建项目地址:https://start.spring.io

## Spring Boot 启动类

### 启动方式

1. 直接在main()用Spring Boot引导类SpringApplication启动

```java
@SpringBootApplication
public class SpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }
}
```

2. 直接在main()用SpringApplicationBuilder启动(fluent API)

```java
@SpringBootApplication
public class SpringBootDemoApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringBootDemoApplication.class).run(args);
    }   
}
```

3. 在main()中使用spring的方式启动.

```java
@SpringBootApplication
public class SpringBootDemoApplication {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(SpringBootDemoApplication.class);
        context.refresh();
    }
}
```

可以看到这个类就只有一个main方法,却可以跑起整个Spring Boot项目,为了解决这个疑惑,先从这个注解开始了解

### `@SpringBootApplication`

`@SpringBootApplication`注解的定义(springboot1.2.0版本引入):

```java
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
		@Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {
    ...
}
```

`@Inherited`:该注解可以被继承.

`@SpringBootConfiguration` = `@Configuration`该类为配置类.可被扫描.

`@EnableAutoConfiguration`:开启自动装配功能.

`@ComponentScan`:开启自动扫描.

#### `@Component ` Stereotype Annotations(套用注解)

* `@Component`(Spring2.5版本引入)

  * `@Controller`(Spring2.5版本引入)

    ```java
    @Component
    public @interface Controller {
    	...
    }
    ```

  * `@RestController`(Spring4.0版本引入)

    ```java
    @Controller
    @ResponseBody
    public @interface RestController {
        ...
    }
    ```

    * `@Controller`

  * `@Service`(Spring2.5版本引入)

    ```java
    @Component
    public @interface Service {
        ...
    }
    ```

  * `@Repository`(Spring2.0版本引入,2.0版本只能用在Dao层)

    ```java
    @Component
    public @interface Repository {
        ...
    }
    ```

  * `@Configuration`(Spring3.0版本引入)

    ```java
    @Component
    public @interface Configuration {
        ...
    }
    ```

#### `@ComponentScan `

该注解如何扫描到注解的呢?全局搜索使用到ComponentScan.class的位置,不难找到用的地方

```java
class ConfigurationClassParser {
    protected final SourceClass doProcessConfigurationClass(ConfigurationClass configClass, SourceClass sourceClass)
			throws IOException {
        if (!componentScans.isEmpty() &&
				!this.conditionEvaluator.shouldSkip(sourceClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN)) {
			for (AnnotationAttributes componentScan : componentScans) {
				// The config class is annotated with @ComponentScan -> perform the scan immediately
				Set<BeanDefinitionHolder> scannedBeanDefinitions =
						this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());
				// Check the set of scanned definitions for any further config classes and parse recursively if needed
				for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
					BeanDefinition bdCand = holder.getBeanDefinition().getOriginatingBeanDefinition();
					if (bdCand == null) {
						bdCand = holder.getBeanDefinition();
					}
					if (ConfigurationClassUtils.checkConfigurationClassCandidate(bdCand, this.metadataReaderFactory)) {
						parse(bdCand.getBeanClassName(), holder.getBeanName());
					}
				}
			}
		}
        ...
    }
    ...
}
```

`ConfigurationClassParser`此类便为ComponentScan的处理类.

看见this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName())方法便可以联想到是此处进行的遍历解析.

```java
class ComponentScanAnnotationParser {
    public Set<BeanDefinitionHolder> parse(AnnotationAttributes componentScan, final String declaringClass) {
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(this.registry,
				componentScan.getBoolean("useDefaultFilters"), this.environment, this.resourceLoader);
        ...
        return scanner.doScan(StringUtils.toStringArray(basePackages));
    }
    ...
}
```

可以看见在此处,new了一个ClassPathBeanDefinitionScanner实例.点进去看下该构造方法:

```java
public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {
    public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,
			Environment environment, @Nullable ResourceLoader resourceLoader) {

		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;

		if (useDefaultFilters) {
			registerDefaultFilters();
		}
		setEnvironment(environment);
		setResourceLoader(resourceLoader);
	}
    ...
}
```

此处可以看见有一个`registerDefaultFilters()`,是其父类`ClassPathScanningCandidateComponentProvider`的方法:

```java
protected void registerDefaultFilters() {
    this.includeFilters.add(new AnnotationTypeFilter(
					((Class<? extends Annotation>) ClassUtils.forName("javax.inject.Named", cl)), false));
    ...
}
```

解析 -> `ComponentScanAnnotationParser`

注册扫描 -> `ClassPathScanningCandidateComponentProvider`

## SpringApplication

### SpringApplication类型推断

`SpringApplication()`->`deduceWebApplicationType()`

在实例化`SpringApplication`时,便调用了`deduceWebApplicationType()`:

```java
private WebApplicationType deduceWebApplicationType() {
	if (ClassUtils.isPresent(REACTIVE_WEB_ENVIRONMENT_CLASS, null)
			&& !ClassUtils.isPresent(MVC_WEB_ENVIRONMENT_CLASS, null)) {
		return WebApplicationType.REACTIVE;
	}
	for (String className : WEB_ENVIRONMENT_CLASSES) {
		if (!ClassUtils.isPresent(className, null)) {
			return WebApplicationType.NONE;
		}
	}
	return WebApplicationType.SERVLET;
}
```

在不手动去设置的情况下:默认是Spring MVC

* `WebApplicationType.REACTIVE` : Spring WebFlux
  - `DispatcherHandler`(核心类)
  - `spring-boot-starter-webflux` 存在
  - `Servlet` 不存在
  - `spring-boot-starter-web` 不存在
* `WebApplicationType.NONE` : 非 Web 类型
  - `Servlet` 不存在
  - Spring Web 应用上下文 `ConfigurableWebApplicationContext`  不存在
  - `spring-boot-starter-web` 不存在
  - `spring-boot-starter-webflux` 不存在
* `WebApplicationType.SERVLET` : Spring MVC
  - `spring-boot-starter-web` 存在

可以人工设置SpringApplication类型

```java
@SpringBootApplication
public class SpringBootDemoApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SpringBootDemoApplication.class);
        // 设置为非Web类型
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}
```

### Spring的事件监听

#### spring内置的事件

```java
@Configuration
public class SpringDemo {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.addApplicationListener(event -> System.err.println(event.getClass().getName()));
        context.refresh();
        context.close();
    }
}
```

```
org.springframework.context.event.ContextRefreshedEvent
org.springframework.context.event.ContextClosedEvent
```

* `ContextRefreshedEvent`

  * `ApplicationContextEvent`

    * `ApplicationEvent`

  * 添加监听事件

    `refresh()`->`AbstractApplicationContext.registerListeners()`

    ->`getApplicationEventMulticaster().addApplicationListener(listener)`

  * 发布事件

    `refresh()`->`AbstractApplicationContext.finishRefresh()`

    ->`publishEvent(new ContextRefreshedEvent(this));`

* `ContextClosedEvent`

  * `ApplicationContextEvent`

    - `ApplicationEvent`

  * 发布事件

    `close()`->`AbstractApplicationContext.doClose()`

    ->`publishEvent(new ContextClosedEvent(this));`

#### Spring自定义事件

```java
@Configuration
public class SpringDemo {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.addApplicationListener(event -> System.err.println(event));
        context.refresh();
        context.publishEvent("发布事件");
        context.publishEvent(new MyEvent("发布自定义事件"));
        context.close();
    }

    private static class MyEvent extends ApplicationEvent {
        public MyEvent(Object source) {
            super(source);
        }
    }
}
```

```
org.springframework.context.event.ContextRefreshedEvent...
org.springframework.context.PayloadApplicationEvent...
com.yczuoxin.springbootdemo.SpringDemo$MyEvent[source=发布自定义事件]
org.springframework.context.event.ContextClosedEvent...
```

在没有指定事件类型,事件类型是`PayloadApplicationEvent`(`ApplicationEvent`的子类),其使用的地方在于

```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader
		implements ConfigurableApplicationContext {
	ApplicationEvent applicationEvent;
	if (event instanceof ApplicationEvent) {
		applicationEvent = (ApplicationEvent) event;
	}
	else {
		applicationEvent = new PayloadApplicationEvent<>(this, event);
		if (eventType == null) {
			eventType = ((PayloadApplicationEvent) applicationEvent).getResolvableType();
		}
	}
	...
}
```

* `ApplicationEvent`
  * 需要是ApplicationEvent类或其子类
* `PayloadApplicationEvent`
  * 自定义发布的事件

消息的发送的流程:

* `ApplicationEventMulticaster.multicastEvent()`
  * `AbstractApplicationEventMulticaster`
    * `SimpleApplicationEventMulticaster.invokeListener(listener, event)`最终实现类

`ApplicationEvent`Spring事件的类型,即事件消息

`ApplicationListener`Spring事件的监听器

`ApplicationEventMulticaster`Spring事件的发布器

### Spring Boot的事件

```java
@SpringBootApplication
public class SpringBootDemoApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SpringBootDemoApplication.class);
        app.addListeners(event -> {
            System.err.println("监听到事件:" + event.getClass().getName());
        });
        app.run(args).close();
    }
}
```

```
监听到事件:org.springframework.boot.context.event.ApplicationStartingEvent
监听到事件:org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
监听到事件:org.springframework.boot.context.event.ApplicationPreparedEvent
监听到事件:org.springframework.context.event.ContextRefreshedEvent
监听到事件:org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent
监听到事件:org.springframework.boot.context.event.ApplicationStartedEvent
监听到事件:org.springframework.boot.context.event.ApplicationReadyEvent
监听到事件:org.springframework.context.event.ContextClosedEvent
监听到事件:org.springframework.boot.context.event.ApplicationFailedEvent(异常情况)
```

在Spring 官网上可以看见对于Spring Boot内置事件的讲解

1. An `ApplicationStartingEvent` is sent at the start of a run but before any processing, except for the registration of listeners and initializers.
2. An `ApplicationEnvironmentPreparedEvent` is sent when the `Environment` to be used in the context is known but before the context is created.
3. An `ApplicationPreparedEvent` is sent just before the refresh is started but after bean definitions have been loaded.
4. An `ApplicationStartedEvent` is sent after the context has been refreshed but before any application and command-line runners have been called.
5. An `ApplicationReadyEvent` is sent after any application and command-line runners have been called. It indicates that the application is ready to service requests.
6. An `ApplicationFailedEvent` is sent if there is an exception on startup.

可在spring.factories文件中看到Spring Boot默认的配置信息`META-INF/spring.factories`

```java
# Application Listeners
org.springframework.context.ApplicationListener=\
org.springframework.boot.ClearCachesApplicationListener,\
org.springframework.boot.builder.ParentContextCloserApplicationListener,\
org.springframework.boot.context.FileEncodingApplicationListener,\
org.springframework.boot.context.config.AnsiOutputApplicationListener,\
org.springframework.boot.context.config.ConfigFileApplicationListener,\
org.springframework.boot.context.config.DelegatingApplicationListener,\
org.springframework.boot.context.logging.ClasspathLoggingApplicationListener,\
org.springframework.boot.context.logging.LoggingApplicationListener,\
org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener
```

监听类

* `Application`
  * `SmartApplicationListener`
    * `ConfigFileApplicationListener.onApplicationEvent()`

事件类型

* `SpringApplicationEvent`
  * `ApplicationEnvironmentPreparedEvent`

利用`ApplicationEventMulticaster`去加载文件.

Spring Boot利用这一套监听事件驱动去装载默认配置和application.properties或者application.yml文件.本质就是Spring Framework的监听事件.



`SpringApplication`

* Spring运用(`ApplicationContext`)生命周期控制驱动Bean
* Spring 事件/监听（`ApplicationEventMulticaster`）机制加载或者初始化组件



