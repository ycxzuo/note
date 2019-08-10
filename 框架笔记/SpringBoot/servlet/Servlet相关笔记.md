# Servlet 相关笔记(3.0)

## 组件

### Servlet组件注册

`javax.servlet.ServletContext#addServlet()`

`javax.servlet.ServletRegistration`



### Filter组件注册

`javax.servlet.ServletContext#addFilter()`

`javax.servlet.FilterRegistration`



### 监听器注册

`javax.servlet.ServletContext#addListener()`

`javax.servlet.AsyncListener`



### 自动装配

#### 初始器

`javax.servlet.Servlet.ServletContainerInitializer`

将实现类全部读进来，他是利用了 Java 的 ServiceLoader 会读取 `META-INF/service/` 路径下的文件，servlet 则是放在 `META-INF/services/javax.servlet.ServletContainerInitializer` ，这是使用 spi 接口调用，里面写的类必须是实现了 `ServletContainerInitializer` 接口的类

#### 类型过滤

`@javax.servlet.annotation.HandlesTypes`

用来标注该关注出哪些类，但是其关注的 class 的 Set 中是该标记类的子类（包含子接口、抽象类、实现类），但是不包含自己

#### *注意*

springboot 就是使用自动装配来达到效果，`org.springframework.web.SpringServletContainerInitializer` 就是其中之一



## 生命周期

### Servlet 生命周期

#### 初始化

当容器启动或者第一次执行时，`Servlet#init(ServletConfig)` 方法被执行，初始化当前 Servlet

#### 处理请求

当 HTTP 请求到达容器时， `Servlet#service(ServletRequest, ServletResponse)` 方法被执行，来处理请求

#### 销毁

当容器关闭时，容器将会调用 `Servlet#destroy` 方法被执行，销毁当前 Servlet

#### *注意*

一般使用的是 `javax.servlet.http.HttpServlet` 作为 WEB 容器，它实现了 Servlet 接口，在底层调用 `HttpServlet#service(ServletRequest, ServletResponse)` 方法时，会将 `ServletRequest` 和 `ServletResponse` 其强转为 `HttpServletRequest` 和 `HttpServletResponse`

### Filter 生命周期

#### 初始化

当容器启动时，`Filter#init(FilterConfig)` 方法被执行，初始化当前 Filter

#### 处理请求

当 HTTP 请求到达容器时，`Filter#doFilter(ServletRequest, ServletResponse, FilterChain)` 方法被执行，来拦截请求，在 `Servlet#service(ServletRequest, ServletResponse)` 方法调用前执行

#### 销毁

当容器关闭时，容器将会调用 `Filter#destroy` 方法被执行，销毁当前 Filter



## Servlet On Spring Boot

### 组件扫描

* `@org.springframework.boot.web.servlet.ServletComponentScan`
  * 指定包路径扫描
    * String[] value() default {}
    * String[] basePackages() default {}
  * 指定类扫描
    * Class<?>[] basePackages() default {}



### 注解方式注册

#### Servlet 组件

1. 扩展 `javax.servlet.Servlet`
   * `javax.servlet.http.HttpServlet`
   * `org.springframework.web.servlet.FrameworkServlet`
2. 标记 `javax.servlet.annotation.WebServlet`

#### Filter 组件

1. 实现 `javax.servlet.Filter`
   * `org.springframework.web.filter.OncePerRequestFilter`
2. 标记 `@javax.servlet.annotation.WebFilter`

#### 监听器组件

1. 实现 Listener 接口
   * `javax.servlet.ServletContextListener`
   * `javax.servlet.http.HttpSessionListener`
   * `javax.servlet.http.HttpSessionActivationListener`
   * `javax.servlet.ServletRequestListener`
   * `javax.servlet.http.HttpSessionBindingListener`
   * `javax.servlet.ServletRequestAttributeListener`
2. 标记 `@javax.servlet.annotation.WebListener`

#### *注意*

当拿不到 request 的时候，可以利用 `org.springframework.web.context.request.RequestContextHolder` 来获取

```java
RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        ServletContext context = request.getServletContext();
```

每个请求过来时 `org.springframework.web.context.request.RequestContextListener#requestInitialized(ServletRequestEvent)` 方法中，会在 `RequestContextHolder` 注册请求信息，其实际上存在 `ThreadLocal` 中



### Spring Boot API 方式注册

#### Servlet 组件

1. 扩展 `javax.servlet.Servlet`
   - `javax.servlet.http.HttpServlet`
   - `org.springframework.web.servlet.FrameworkServlet`
2. 组装 Servlet
   * Spring Boot 1.4.0 开始支持
     * `org.springframework.web.filter.OncePerRequestServlet`
   * Spring Boot 1.4.0 之前
     * `org.springframework.boot.context.servlet.embedded.ServletRegistrationBean`
3. 暴露 Spring Bean
   * `@Bean`

#### Filter 组件

1. 扩展 `javax.servlet.Filter`
   - `org.springframework.web.filter.OncePerRequestFilter`
2. 组装 Servlet
   - Spring Boot 1.4.0 开始支持
     - `org.springframework.boot.web.servlet.FilterRegistrationBean`
   - Spring Boot 1.4.0 之前
     - `org.springframework.boot.context.servlet.embedded.FilterRegistrationBean`
3. 暴露 Spring Bean
   - `@Bean`

#### 监听器组件

1. 实现 Listener 接口

   - `javax.servlet.ServletContextListener`
   - `javax.servlet.http.HttpSessionListener`
   - `javax.servlet.http.HttpSessionActivationListener`
   - `javax.servlet.ServletRequestListener`
   - `javax.servlet.http.HttpSessionBindingListener`
   - `javax.servlet.ServletRequestAttributeListener`

2. 组装 Listener 

   * Spring Boot 1.4.0 开始
     * `org.springframework.boot.web.servlet.ServletListenerRegistrationBean`
   * Spring Boot 1.4.0 之前
     * `org.springframework.boot.context.servlet.embedded.ServletListenerRegistrationBean`

3. 暴露 Spring Bean
   * `@Bean`


