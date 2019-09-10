# Tomcat 组件

Tomcat 可以通过 bin 目录下的 startup.sh/startup.bat 来启动 Tomcat

![启动](http://tva1.sinaimg.cn/large/007X8olVly1g6ujlgu8djj317b06it8v.jpg)

1. Tomcat 本质上是一个 Java 程序，因此 startup.sh/startup.bat 脚本会启动一个 JVM 来运行 Tomcat 的启动类 BootStrap
2. BootStrap 的主要任务是初始化 Tomcat 的类加载器，并创建 Catalina。
3. Catalina 是一个启动类，它通过解析 server.xml、创建相应的组件，并调用 Server 的 start 方法
4. Server 组件的职责就是管 Service 组件，它会负责调用 Service 的 start 方法
5. Service 组件的职责就是管理连接器和顶层容器 Engine，因此他会调用连接器和 Engine 的 start 方法

此时 Tomcat 的启动完成了。

BootStrap 是启动类加载器，它初始化了类加载器

## Catalina

Catalina 主要的任务是创建 Server，它不是直接 new 一个 Server 实例就完事了，而是需要解析 server.xml，把在 server.xml 里配置的各种组件一一创建出来，接着调用 Server 组件的 init 方法和 start 方法，这样整个 Tomcat 就启动起来了，作为管理者，Cataline 还需要处理各种异常情况，例如当我们通过 `Ctrl + C` 关闭 Tomcat 时，Tomcat 将如何优雅地停止并清理资源呢？因此 Catalina 在 JVM 中注册了一个**关闭钩子**

```java
public void start() {
    //1. 如果持有的 Server 实例为空，就解析 server.xml 创建出来
    if (getServer() == null) {
        load();
    }
    //2. 如果创建失败，报错退出
    if (getServer() == null) {
        log.fatal(sm.getString("catalina.noServer"));
        return;
    }

    //3. 启动 Server
    try {
        getServer().start();
    } catch (LifecycleException e) {
        return;
    }

    // 创建并注册关闭钩子
    if (useShutdownHook) {
        if (shutdownHook == null) {
            shutdownHook = new CatalinaShutdownHook();
        }
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    // 用 await 方法监听停止请求
    if (await) {
        await();
        stop();
    }
}
```

关闭钩子实质上就是调用了 Server 的 stop 方法，会释放和清理所有的资源



## Server 组件

Server 组件的具体实现类是 StandardServer，Server 继承了 LifecycleBase，它的生命周期被统一管理，并且它的子组件是 Service，因此它还需要管理 Service 的生命周期，也就是说在启动时调用 Service 组件的启动方法，在停止时调用它们的停止方法。Server 在内部维护了若干个 Service 组件，它是以数组来保存的

```java
@Override
public void addService(Service service) {

    service.setServer(this);

    synchronized (servicesLock) {
        // 创建一个长度 +1 的新数组
        Service results[] = new Service[services.length + 1];
        
        // 将老的数据复制过去
        System.arraycopy(services, 0, results, 0, services.length);
        results[services.length] = service;
        services = results;

        // 启动 Service 组件
        if (getState().isAvailable()) {
            try {
                service.start();
            } catch (LifecycleException e) {
                // Ignore
            }
        }

        // 触发监听事件
        support.firePropertyChange("service", null, service);
    }

}
```

数组长度是在添加过程中动态扩展的，当添加一个新的 Service 实例时，会创建一个新数组并把原来数组内容复制到新数组，这样做的目的就是节省内存空间

Server 组件还有一个重要的任务是启动一个 Socket 来监听停止端口，这就是为什么能通过 shutdown 命令来关闭 Tomcat。

Catalina 的启动最后一行调用了 Server 的 await 方法，在 await 方法里会创建一个 Socket 监听 8005 端口，并在一个死循环里接受 Socket 上的连接请求，如果有新的连接到来就创建连接，然后从 Socket 中读取数据，如果读到的数据是停止命令 `SHUTDOWN`，就退出循环，进入 stop 流程



## Service 组件

Service 组件的具体实现类是 StandardService，我们先来看它的定义及关键的成员变量

```java
public class StandardService extends LifecycleBase implements Service {
    // 名字
    private String name = null;
    
    //Server 实例
    private Server server = null;

    // 连接器数组
    protected Connector connectors[] = new Connector[0];
    private final Object connectorsLock = new Object();

    // 对应的 Engine 容器
    private Engine engine = null;
    
    // 映射器及其监听器
    protected final Mapper mapper = new Mapper();
    protected final MapperListener mapperListener = new MapperListener(this);
|
```

StandardService 继承了 LifecycleBase 抽象类，此外 StandardService 中还有一些我们熟悉的组件，例如 Server、Connector、Engine 和 Mapper

那为什么还有一个 MapperListener？这是因为 Tomcat 支持热部署，当 Web 应用的部署发生变化时，Mapper 中的映射信息也要跟着变化，MapperListener 就是一个监听器，它监听容器的变化，并把信息更新到 Mapper 中，这是**观察者模式**

作为管理角色的组件，最重要的是维护其他组件的生命周期。此外在启动各组件时，要注意他们的依赖关系，也就是说，要注意启动的顺序

```java
protected void startInternal() throws LifecycleException {

    //1. 触发启动监听器
    setState(LifecycleState.STARTING);

    //2. 先启动 Engine，Engine 会启动它子容器
    if (engine != null) {
        synchronized (engine) {
            engine.start();
        }
    }
    
    //3. 再启动 Mapper 监听器
    mapperListener.start();

    //4. 最后启动连接器，连接器会启动它子组件，比如 Endpoint
    synchronized (connectorsLock) {
        for (Connector connector: connectors) {
            if (connector.getState() != LifecycleState.FAILED) {
                connector.start();
            }
        }
    }
}
```

Service 先启动了 Engine 组件，再启动 Mapper 监听器，最后才是启动连接器，这很好理解，因为内层组件启动好才能对外提供服务，才能启动外层的连接器组件，而 Mapper 也依赖容器组件，容器组件启动好了才能监听它们的变化，因此 Mapper 和MapperListener 在容器组建之后启动，组件停止的顺序跟启动顺序相反



## Engine 组件

Engine 本质是一个容器，因此它继承了 ContainerBase 基类，并且实现了 Engine 接口

```java
public class StandardEngine extends ContainerBase implements Engine {
}
```

Engine 的子容器是 Host，所以它持有了一个Host 容器数组，这些功能被抽象到了 ContainerBase 中， ContainerBase 中有这样一个结构

```java
// key 是容器名称， value 是该容器所管理的子容器
protected final HashMap<String, Container> children = new HashMap<>();
```

ContainerBase 用 HashMap 保存了它的子容器，并且 ContainerBase 还实现了子容器的增删改查，甚至连子组件的启动和停止都提供了默认实现，例如 ContainerBase 会用专门的线程池来启动子容器

```java
for (int i = 0; i < children.length; i++) {
   results.add(startStopExecutor.submit(new StartChild(children[i])));
}
```

Engine 在启动 Host 子容器时就直接重用了这个方法

我们知道容器组件最重要的功能是处理请求，而 Engine 容器对请求的处理其实就是把请求转发给某一个 Host 子容器来处理，具体是通过 Valve 来实现的

我们知道每一个容器组件都有一个 Pipeline，而 Pipeline 中有一个基础阈 Basic Valve，而 Engine 容器的基础阈定义如下

```java
final class StandardEngineValve extends ValveBase {

    public final void invoke(Request request, Response response)
      throws IOException, ServletException {
  
      // 拿到请求中的 Host 容器
      Host host = request.getHost();
      if (host == null) {
          return;
      }
  
      // 调用 Host 容器中的 Pipeline 中的第一个 Valve
      host.getPipeline().getFirst().invoke(request, response);
  }
  
}
```

基础阈实现非常简单，就是把请求转发到 Host 容器，从代码中可以看出，处理请求的 Host 容器对象是从请求中拿到的，这是因为请求在到达 Engine 容器之前，Mapper 组件已经对请求进行了路由处理，Mapper 组件通过请求的 URL 定位了相应的容器，并把容器对象保存到了请求对象中



## 小结

这样设计需要考虑两个方面

* 要选用合适的数据结构来保存组件，比如 Server 用数组来保存 Service 组件，并且才去动态扩容的方式，这是因为数组结构简单，占用内存小；ContainerBase 用 HashMap 来保存子容器，虽然 Map 占用内存多一点，但是通过 Map 来快速的查找子容器。
* 根据子组件依赖关系决定它们的启动和停止顺序，以及如何优雅的停止，防止异常情况下的资源泄漏，这是管理者应该考虑的事情

