# Lifecycle 接口

## Lifecycle 接口的使用

### 描述

这是一个开始或者结束生命周期的通用的接口控制，用于异步场景，但是这个接口不支持自动启动功能

```java
public interface Lifecycle {
    /**
     * 启动当前组件
     * 如果组件已经在运行，不应该抛出异常
     * 在容器的情况下，这会将 开始信号 传播到应用的所有组件中去。
     */
    void start();
    /**
     * (1)通常以同步方式停止该组件，当该方法执行完成后,该组件会被完全停止。当需要异步停止行为时，考虑实现SmartLifecycle 和它的 stop(Runnable) 方法变体。注意，此停止通知在销毁前不能保证到达:在常规关闭时，{@code Lifecycle} bean将首先收到一个停止通知，然后才传播常规销毁回调;然而，在上下文的生命周期内的热刷新或中止的刷新尝试上，只调用销毁方法对于容器，这将把停止信号传播到应用的所有组件
     */
    void stop();

    /**
     *  检查此组件是否正在运行。
     *  1. 只有该方法返回false时，start方法才会被执行。
     *  2. 只有该方法返回true时，stop(Runnable callback)或stop()方法才会被执行。
     */
    boolean isRunning();

}
```



### 测试实现类 TestLifecycle

```java
@Component
public class TestLifecycle implements Lifecycle {

    private static volatile boolean isRunning = false;

    @Override
    public void start() {
        System.out.println("lifecycle is start");
        isRunning = true;
    }

    @Override
    public void stop() {
        System.out.println("lifecycle is stop");
        isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
```



### 启动方式 LifecycleApplication

```java
public class LifecycleApplication {
    public static void main(String[] args) {
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(TestLifecycle.class);
        context.start();
        context.close();
    }
}
```



### 注意事项

仅仅实现 Lifecycle 接口的话，需要手动调用 `org.springframework.context.support.AbstractApplicationContext#start` 方法才能触发，SpringBoot 自启动容器时是不会触发实现 Lifecycle 接口的 start() 方法



### Spring 中的使用

* `org.springframework.context.ConfigurableApplicationContext`

  * `org.springframework.context.support.AbstractApplicationContext`

* `org.springframework.context.SmartLifecycle`
  
* `org.springframework.boot.web.servlet.context.WebServerGracefulShutdownLifecycle`
  
* `org.springframework.context.LifecycleProcessor`

  * `org.springframework.context.support.DefaultLifecycleProcessor`

  



## SmartLifecycle 接口

### 描述

优先级并且可以设置是否自启动时生效的扩展的 Lifecycle 的扩展

```java
public interface SmartLifecycle extends Lifecycle, Phased {

	/**
	 * 默认都是最后执行，最先销毁
	 */
    int DEFAULT_PHASE = Integer.MAX_VALUE;


	/**
	 * 自启动生效，默认是 true
	 */
	default boolean isAutoStartup() {
		return true;
	}

	/**
	 * 停止时调用的方法，callback 是一个异步执行的功能
	 */
	default void stop(Runnable callback) {
		stop();
		callback.run();
	}

	/**
	 * 获取优先级，默认是开始时最后初始化，销毁时最先执行销毁，数值越大，开始时越后执行，结束时越先执行
	 */
	@Override
	default int getPhase() {
		return DEFAULT_PHASE;
	}

}

```



### 测试实现类 TestSmartLifecycle

```java
@Component
public class TestSmartLifecycle implements SmartLifecycle {

    private volatile boolean isRunning = false;

    @Override
    public void start() {
        System.out.println("SmartLifecycle is start");
        isRunning = true;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void stop(Runnable callback) {
        System.out.println("SmartLifecycle is stop(Runnable) ing");
        callback.run();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("SmartLifecycle is stop(Runnable)");
        callback.run();
        isRunning = false;
    }

    @Override
    public void stop() {
        System.out.println("SmartLifecycle is stop");
        isRunning = false;
    }

}
```



### 启动方式

* 如果设置 isAutoStartup() 为 true，则会在自动启动的时候调用
* 如果设置 isAutoStartup() 为 false，则会在手动启动的时候调用



## LifecycleProcessor 接口 

### 描述

在 ApplicationContext 中处理生命周期 bean 的策略接口

```java
public interface LifecycleProcessor extends Lifecycle {

	/**
	 * 对于自启动的组件，通知容器上下文调用 refresh 时调用
	 */
	void onRefresh();

	/**
	 * 对于自启动的组件，通知容器上下文关闭时调用
	 */
	void onClose();

}
```



### 注意事项

callback.run() 要是不掉用的话会阻塞，虽然是并行，但是源码是利用 CountDownLatch 来实现阻塞等待并行线程返回或者超时后自动中断，避免无限等待



### Spring 中的使用

* `org.springframework.boot.web.servlet.context.WebServerStartStopLifecycle`
* `org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration`



### 测试实现类 TestLifecycleProcessor

为了不影响功能，选择继承 DefaultLifecycleProcessor

```java
@Component(value = "lifecycleProcessor")
public class TestLifecycleProcessor extends DefaultLifecycleProcessor {

    @Override
    public void onRefresh() {
        System.out.println("TestLifecycleProcessor onRefresh");
        super.onRefresh();
    }

    @Override
    public void onClose() {
        System.out.println("TestLifecycleProcessor onClose");
        super.onClose();
    }

    @Override
    public void start() {
        System.out.println("TestLifecycleProcessor start");
        super.start();
    }

    @Override
    public void stop() {
        System.out.println("TestLifecycleProcessor stop");
        super.stop();
    }

    @Override
    public boolean isRunning() {
        System.out.println("TestLifecycleProcessor isRunning");
        return super.isRunning();
    }
}
```



### 启动方式

* 使用普通的 `org.springframework.context.support.AbstractApplicationContext#start` 会触发 start() 方法
* 使用容器上下文自启动的话，就会触发 onRefresh() 方法



### 注意事项

* 如果要替代 `org.springframework.context.support.DefaultLifecycleProcessor` 产生效果，必须使得 bean 的名称为 lifecycleProcessor

  ```java
  public abstract class AbstractApplicationContext extends DefaultResourceLoader
  		implements ConfigurableApplicationContext {
      
      public static final String LIFECYCLE_PROCESSOR_BEAN_NAME = "lifecycleProcessor";
      
      protected void initLifecycleProcessor() {
          ConfigurableListableBeanFactory beanFactory = getBeanFactory();
          // 判断是否有名为 lifecycleProcessor 的 bean
          if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
              this.lifecycleProcessor =
                  beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
              if (logger.isTraceEnabled()) {
                  logger.trace("Using LifecycleProcessor [" + this.lifecycleProcessor + "]");
              }
          }
          else {
              DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
              defaultProcessor.setBeanFactory(beanFactory);
              this.lifecycleProcessor = defaultProcessor;
              beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
              if (logger.isTraceEnabled()) {
                  logger.trace("No '" + LIFECYCLE_PROCESSOR_BEAN_NAME + "' bean, using " +
                               "[" + this.lifecycleProcessor.getClass().getSimpleName() + "]");
              }
          }
      }
  }
  ```

  在 SpringBoot 中

  ```java
  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(LifecycleProperties.class)
  public class LifecycleAutoConfiguration {
  
  	@Bean(name = AbstractApplicationContext.LIFECYCLE_PROCESSOR_BEAN_NAME)
  	@ConditionalOnMissingBean(name = AbstractApplicationContext.LIFECYCLE_PROCESSOR_BEAN_NAME)
  	public DefaultLifecycleProcessor defaultLifecycleProcessor(LifecycleProperties properties) {
  		DefaultLifecycleProcessor lifecycleProcessor = new DefaultLifecycleProcessor();
  		lifecycleProcessor.setTimeoutPerShutdownPhase(properties.getTimeoutPerShutdownPhase().toMillis());
  		return lifecycleProcessor;
  	}
  
  }
  ```

  

### Spring 中的使用

* `org.springframework.context.support.DefaultLifecycleProcessor`



