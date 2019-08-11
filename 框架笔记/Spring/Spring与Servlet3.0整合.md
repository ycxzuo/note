# Spring与Servlet3.0整合

Servlet3.0 的知识在 [servlet](D:\mygit\note\框架笔记\SpringBoot\servlet\Servlet相关笔记.md) 中已经提及，其中讲到自动装配 `ServletContainerInitializer` 接口是 Spring 和 Serrvlet 整合的重要枢纽

Spring 的 `SpringServletContainerInitializer`实现了 `ServletContainerInitializer`  接口，并且其关心的类是 `WebApplicationInitializer` 的子类

```java
@HandlesTypes(WebApplicationInitializer.class)
public class SpringServletContainerInitializer implements ServletContainerInitializer {
    @Override
	public void onStartup(@Nullable Set<Class<?>> webAppInitializerClasses, ServletContext servletContext)
			throws ServletException {

		List<WebApplicationInitializer> initializers = new LinkedList<>();

		if (webAppInitializerClasses != null) {
			for (Class<?> waiClass : webAppInitializerClasses) {
				// Be defensive: Some servlet containers provide us with invalid classes,
				// no matter what @HandlesTypes says...
				if (!waiClass.isInterface() && !Modifier.isAbstract(waiClass.getModifiers()) &&
						WebApplicationInitializer.class.isAssignableFrom(waiClass)) {
					try {
						initializers.add((WebApplicationInitializer)
								ReflectionUtils.accessibleConstructor(waiClass).newInstance());
					}
					catch (Throwable ex) {
						throw new ServletException("Failed to instantiate WebApplicationInitializer class", ex);
					}
				}
			}
		}

		if (initializers.isEmpty()) {
			servletContext.log("No Spring WebApplicationInitializer types detected on classpath");
			return;
		}

		servletContext.log(initializers.size() + " Spring WebApplicationInitializers detected on classpath");
		AnnotationAwareOrderComparator.sort(initializers);
		for (WebApplicationInitializer initializer : initializers) {
			initializer.onStartup(servletContext);
		}
	}

}
```

首先先看一下 `WebApplicationInitializer` 的实现类

![web](http://tva1.sinaimg.cn/large/0060lm7Tly1g5ovcboop2j31hc03k74v.jpg)

![web2](http://tva1.sinaimg.cn/large/0060lm7Tly1g5ovf6w1lxj30iw034dg2.jpg)

其中 4 个抽象类的源码解读如下

1. AbstractContextLoaderInitializer -> 负责注册根容器到 ServletContext

   需要实现的抽象方法是 `AbstractContextLoaderInitializer#createRootApplicationContext`

2. AbstractDispatcherServletInitializer -> 先创建一个 web 的 ioc 容器ServletAppContext（createServletApplicationContext()）

   并创建一个 DispatcherServlet（createDispatcherServlet()），再将 ServletAppContext 添加到 DispatcherServlet

   再将 DispatcherServlet 添加到 ServletContext 中

   需要实现的抽象方法是 `AbstractDispatcherServletInitializer#getServletMappings`

3. AbstractAnnotationConfigDispatcherServletInitializer -> 使用注解的方式初始化 DispatcherServlet 容器，实现了 createRootApplicationContext() 方法，创建的 `AnnotationConfigWebApplicationContext` 注册到 ServletContext 中，但是要注册的配置留给实现类实现抽象方法 `AbstractAnnotationConfigDispatcherServletInitializer#getRootConfigClasses`

   实现了 createServletApplicationContext() 方法，创建 `AnnotationConfigWebApplicationContext` 注册到 ServletContext 中，但是要注册的配置留给实现类实现抽象方法`AbstractAnnotationConfigDispatcherServletInitializer#getServletConfigClasses`

   需要实现的抽象方法是上述两个抽象方法注册配置，这两个配置的生命周期是不同的，一个与 ServletContext 同步的，一个与 DispatcherServlet 同步

4. AbstractReactiveWebInitializer -> 注册的 `AnnotationConfigWebApplicationContext` 

### WebApplicationInitializer

给增加 Spring 的对象增加了 3 个声明周期：

* request
* session
* application

注册的 ServletContext 属性名称为 org.springframework.web.context.WebApplicationContext.ROOT



### AbstractClassLoaderInitializer

注册根容器 RootApplicationContext 的地方，如何创建留给实现类去实现

创建一个 ContextLoaderListener （之前 web.xml 监听类），将 RootApplicationContext 塞入监听器作为根 WebApplicationContext 来管理加载器。并且可以重写 getRootApplicationContextInitializers() 方法将要应用于 context 的 ApplicationContextInitializer 实例列表放入 RootApplicationContext ，最后监听器会被添加到 ServletContext 中



### AbstractDispatcherSerlvetInitializer

创建的 Web 容器负责扫描控制层组件，如数据校验，视图渲染，国际化等，其实现是留给实现类去实现

创建 DispatcherServlet 本质上去是调用 super(webApplicationContext) 持有 Web 容器，并调用了 `FrameworkServlet#setDispatchOptionsRequest` 方法将 OPTIONS 请求方式设为了支持（true）

可以重写 `AbstractDispatcherServletInitializer#getServletApplicationContextInitializers` 方法使 servlet 在创建时，指定创建时要应用到的应用程序上下文初始值设定项



此 ApplicationContext 只能注册 100 个监听器

```java
protected FilterRegistration.Dynamic registerServletFilter(ServletContext servletContext, Filter filter) {
		String filterName = Conventions.getVariableName(filter);
		Dynamic registration = servletContext.addFilter(filterName, filter);

		if (registration == null) {
			int counter = 0;
			while (registration == null) {
				if (counter == 100) {
					throw new IllegalStateException("Failed to register filter with name '" + filterName + "'. " +
							"Check if there is another filter registered under the same name.");
				}
				registration = servletContext.addFilter(filterName + "#" + counter, filter);
				counter++;
			}
		}

		registration.setAsyncSupported(isAsyncSupported());
		registration.addMappingForServletNames(getDispatcherTypes(), false, getServletName());
		return registration;
	}
```



### AbstractAnnotationConfigDispatcherServletInitializer

实现了 `AbstractContextLoaderInitializer#createRootApplicationContext` 和 `AbstractDispatcherServletInitializer#getServletMappings` 两个方法，注册了 AnnotationConfigWebApplicationContext 作为根节点，调用 `register(Class<?>... annotatedClasses)  ` 将根配置和servlet配置持有。如何获取配置需要实现类去实现抽象方法 `AbstractAnnotationConfigDispatcherServletInitializer#getRootConfigClasses` 和 `AbstractAnnotationConfigDispatcherServletInitializer#getServletConfigClasses`

