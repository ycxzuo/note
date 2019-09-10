# tomcat 架构

## 介绍

tomcat有两个核心功能

* 处理 Socket 连接，将字节流与 Request 和 Respose 互相转化
* 加载和管理 Servlet，以及处理 Request 请求



这个两个核心功能对应了两个核心组件

* 连接器 Connector
* 容器 Container



## 连接器

tomcat 支持多种 I/O 模型和应用层协议

支持的 I/O 模型

* NIO：非阻塞 I/O，采用 Java NIO 类库
* NIO.2：异步 I/O，采用 JDK7 最新的 NIO.2 类库实现
* APR：采用 Apache 可移植运行库实现，是 C/C++ 编写的本地库



支持的应用层协议

* HTTP/1.1：大部分 Web 采用的访问协议
* AJP：用于和 Web 服务器集成（如 Apache）
* HTTP/2.0：对 1.1 版本的 Web 性能大幅度提升



tomcat 的组件关系图

![tomcat](http://tva1.sinaimg.cn/large/007X8olVly1g6t9rik0ylj30n50cwgly.jpg)



连接器的工作步骤或详细功能

* 监听网络端口
* 接受网络请求
* 读取网络请求字节流
* 根据应用层协议解析字节流，并转化为 Tomcat Request 对象
* 将 Tomcat Request 对象转成标准的 ServletRequest
* 调用 Servlet 容器的 service 方法得到 ServletRespose
* 将 ServletRespose 转化为 Tomcat Respose 对象
* 将 Tomcat Respose 转成网络字节流
* 将相应字节写回会给浏览器



连接器需要完成的 3 个高内聚的功能

* 网络通信
* 应用层协议解析
* Tomcat Request/Response 与 ServletRequest/ServletRespose 的转化

这三个功能被设计者设计了三个组件来实现分别是 Endpoint、Processor 和 Adapter

由于 I/O 模型和应用层协议可以自由组合，设计者设计了一个叫 ProtocolHandler 的接口来封装这两点变化

![protocol](http://tva1.sinaimg.cn/large/007X8olVly1g6tafhkbl0j315b0azt8x.jpg)

Endpoint、Processor 和 Adapter 的关系图

![conneter](http://tva1.sinaimg.cn/large/007X8olVly1g6taqt9tbdj30sv07874i.jpg)

### Endpoint

通信端点，即通信监听的接口，是具体的 Socket 接收和发送处理器，是对**传输层**的抽象，因此 Endpoint 是用来实现 TCP/IP 协议的

Endpoint 是一个接口，它对应的抽象类是 AbstractEndpoint，而 AbstractEndpoint 的具体子类，如 NioEndpoint 和 Nio2Endpoint 中，有两个重要的子组件：Acceptor 和 SocketProcessor

* Acceptor
  * 功能就是监听 Socket 连接请求
* SocketProcessor
  * 处理接收到的 Socket 请求，它实现了 Runnable 接口，在 run 方法里调用协议处理组件 Processor 进行处理。为了提高处理能力，SocketProcessor 被提交到线程池来执行。而这个线程池叫做执行器 Executor



### Processor

用来实现 HTTP 协议，Processor 接收来自 Endpoint 的 Socket，读取字节流解析成 Tomcat Request 和 Response 对象，并通过 Adapter 将其提交到容器处理，Processor 是对**应用层**协议的抽象

Processor 是一个接口，定义了请求的处理等方法。它的抽象实现类 AbstractProcessor 对一些协议共有的属性进行封装，没有对方法进行实现。具体实现有 AjpProcessor、Http11Processor 等，这些具体实现类实现了特定协议的解析方法和请求处理方式



### Adapter

由于协议不同，客户端发来的请求信息也不尽相同，Tomcat 定义了自己的 Request 类来存放这些请求信息， ProtocolHandler 接口负责解析请求并设生成 Tomcat Request 类。但是这个 Request 对象不是标准的 ServletRequest，也就意味着不能用 Tomcat Request 作为参数来调用容器，Tomcat 设计者的解决方案是引入 CoyoteAdapter

#### CoyoteAdapter

这是适配器模式的经典运用，连接器调用 CoyoteAdapter 的 service 方法，传入的是 Tomcat Request 对象，CoyoteAdapter 负责将 Tomcat Request 转成 ServletRequest，再调用容器的 service 方法



## 容器

Tomcat 的容器分成了 4 种容器，分别是 Engine、Host、Context、Wrapper。这 4 种容器是父子关系，关系图

![Container](http://tva1.sinaimg.cn/large/007X8olVly1g6tdvk0gjwj30rq0exglq.jpg)

Tomcat 通过分层的架构，是的 Servlet 容器具有很好的灵活性

* Engine
  * 表示引擎，用来管理多个虚拟主机，一个 Service 最多只能有一个 Engine
* Host
  * 表示一个虚拟主机，或者说一个站点，可以给 Tomcat 配置多个虚拟主机地址，而一个虚拟主机可以部署多个 Web 应用程序
* Context
  * 表示一个 Web 应用程序，一个 Web 程序中可能会有多个 Servlet
* Wrapper
  * 表示一个 Servlet

Tomcat 的 server.xml 配置文件可以看出其结构

```xml
<Server>					// 顶层组件，可以包括多个Service
    <Service>				// 顶层组件，可以包含一个 Engine 和多个 Connector
    	<Connector>			// 连接器组件
        </Connector>
        <Engine>			// 容器组件，处理 Service 的所有请求，包含多个 Host
        	<HOST>			// 容器组件，处理特定 Host 下客户端请求，包含多个 Context
            	<Context>	// 容器组件，为特定的 Web 应用处理所有的客户请求
                </Context>
            </HOST>
        </Engine>
    </Service>
</Server>
```

这些容器具有父子关系，形成一个树型结构，使用的设计模式中的**组合模式**，具体的实现方式是，所有容器组件都实现了 Container 接口，因此组合模式可以使得用户对但容器对象和组合容器对象的使用具有一致性。这里单容器对象指的是最底层的 Wrapper，组合容器对象指的是上面的 Context、Host 或者 Engine。

```java
public interface Container extends Lifecycle {
    public void setName(String name);
    public Container getParent();
    public void setParent(Container container);
    public void addChild(Container child);
    public void removeChild(Container child);
    public Container findChild(String name);
}
```

Lifecycle 接口用来统一管理各组件的生命周期



### 定位 Servlet 的过程

Tomcat 利用 Mapper 组件来完成定位 Servlet 的工作



#### Mapper 组件

其功能是将用户请求的 URL 定位到一个 Servlet，其原理是组件里保存了 Web 应用的配置信息，其实就是容器组件与访问路径的映射关系，如 Host 容器里配置的域名，Context 容器里的 Web 应用路径，以及 Wrapper 容器里 Servlet 映射的路径，可以想象成一个多层次的 Map

* 根据协议和端口号定位 Service 和 Engine
  * 由连接器可知每个连接器都监听的不同的端口，Tomcat 默认的 HTTP 连接器监听 8080 端口，默认的 AJP 连接器监听 8009 端口，一个连接器属于一哥 Service 组件，这样 Service 组件就确定了。而 一个 Service 组件只有一个容器组件，一个容器组件只对应一个 Engine 容器，所以 Engine 容器就确定了
* 根据域名定位 Host
  * Service 和 Engine 确定后，Mapper 组件通过 URL 中的域名去查找对应的 Host 容器
* 根据 URL 路径定位 Context 组件
  * Host 确定后，Mapper 根据 URL 的路径来匹配相应的 Web 应用的路径
* 根据 URL 路径找到 Wrapper 和 Servlet
  * Context 确定后，Mapper 再根据 web.xml 中配置的 Servlet 映射路径找到具体的 Wrapper 和 Servlet



#### Pipeline-Valve

在定位 Servlet 的过程中，不是只有 Servlet 会对请求进行处理，实际上每一层容器都会对请求做一些处理，调用过程就是使用的 Pipeline-Valve 管道

Pipeline-Valve 是责任链模式，一个请求处理的过程中有很多处理者依次对请求进行处理，每个处理者负责做自己相应的处理，处理完之后将再调用下一个处理者继续处理

Valve 表示一个处理点，比如权限验证和记录日志

```java
public interface Valve {
  public Valve getNext();
  public void setNext(Valve valve);
  public void invoke(Request request, Response response)
}
```

由于 Valve 是一个处理点，因此 invoke 方法就是来处理请求的，Valve 中有 getNext 和 setNext 方法，因此肯定有一个链表将 Valve 串起来了

```java
public interface Pipeline extends Contained {
  public void addValve(Valve valve);
  public Valve getBasic();
  public void setBasic(Valve valve);
  public Valve getFirst();
}
```

Pipeline 中有 addValve 方法，Pipeline 中维护了 Valve 链表，Valve 可以插入到 Pipeline 中，但是 pipeline 中没有 invoke 方法，因为整个调用链的触发是由 Valve 来完成的，Valve 完成自己的处理后调用 getNext.invoke 来触发下一个 Valve 调用

每一个容器都有一个 Pipeline 中还有个 getBasic 方法。这个 BasicValve 处于 Valve 链表的末端，他是 pipeline 中必不可少的一个 Valve，负责调用下层容器的 Pipeline 里的第一个 Valve

![Pipeline](http://tva1.sinaimg.cn/large/007X8olVly1g6u90nw1tlj310q0ffgmf.jpg)

这个责任链的调用是由 Adapter 处罚的，它调用 Engine 的第一个 Valve

```java
// Calling the container
connector.getService().getContainer().getPipeline().getFirst().invoke(request, response);
```

Wrapper 容器最后一个 Valve 会创建一个 Filter 链，并调用 doFilter 方法，最终会调用 Servlet 的 service 方法

Tomcat 的 Valve 和 Servlet 的 Filter 的区别

* Valve 是 Tomcat 的私有机制，与 Tomcat 的基础架构/API 是紧耦合的。Servlet API 是共有的标准，所有的 Web 容器包括 Jetty 都支持 Filter 机制
* Valve 工作在 Web 容器级别，拦截所有的应用请求；Filter 工作在应用级别，智能拦截某个 Web 应用的所有请求

![tomcat](http://tva1.sinaimg.cn/large/007X8olVly1g6uel9ysvej310s0kgwfn.jpg)



## Tomcat 的 Context 、Servlet 的 ServletContext 和 Spring 的 ApplicationContext 之间有什么关系

Tomcat 的 Context 是对应 Web 应用，而 Servlet 的 ServletContext 在 Servlet 规范中对应 Web 应用的上下文环境，所以，ServletContext 是 Tomcat 的 Context 具体实现的一个成员变量。Tomcat 源码中，ServletContext 对应 Tomcat 实现是 `org.apache.catalina.core.ApplicationContext`，Context 容器对应 Tomcat 的实现是 `org.apache.catalina.core.StandardContext`。ApplicationContext 是 StandardContext 的一个成员变量

在 Tomcat 启动过程中 ContextLoaderListener 会监听到容器初始化时间，它的 contextInitialized 方法中，Spring 会初始化全局的 Spring 根容器 ApplicationContext，初始化完毕后，Spring 将 ApplicationContext 存储到 ServletContext 之中

