# Servlet 生命周期

该生命周期通过 `javax.servlet.Servlet` 的 init、service 和 destroy 这些 API 来表示，所有 Servlet 必须直接或间接的实现 GenericServlet 或 HttpServlet 抽象类

* 加载和实例化
* 初始化（init）
* 请求处理（service）
* 终止服务（destroy）



## 加载和实例化

Servlet 容器负责加载和实例化 Servlet

加载和实例化时机由容器决定，可以是容器启动时，也可以延迟初始化直到容器决定有请求需要处理时

当 Servlet 引擎启动后，Servlet 容器必须定位所需要的 Servlet 类



## 初始化

一旦一个 Servlet 对象实例化完毕，容器接下来必须在处理客户端请求之前初始化该 Servlet 实例。初始化的目的是以便 Servlet 能读取到持久化配置数据，初始化一些代价高的资源（比如 JDBC API 连接），或者执行一些一次性的动作。init 方法定义在 Servlet 接口中，并且提供一个唯一的 ServletConfig 接口实现的对象作为参数，该对象每个 Servlet 实例一个

### 初始化时的错误条件

初始化阶段可能抛出 UnavailableException 或 ServletException 异常。在这种情况下，Servlet 容器必须释放它，**destroy 方法不应该被调用**

UnavailableException 异常有一个 seconds 属性，就是，容器在创建和初始化一个新的 Servlet 实例之前必须等待多少时间

### 使用工具时的注意事项

如果 Servlet 是被触发的静态初始化，没有经历 init 初始化，开发人员不应该假设其处于活动的容器环境内



## 请求处理

客户端请求由 ServletRequest 类型的 request 表示，Servlet 封装的响应 response 应该是 ServletResponse 类型，这两个对象（request 和 response） 是由容器通过参数传递到 Servlet 接口的 service 方法的

### 多线程问题

Servlet 容器可以并发的发送多个请求到 Servlet 的 service 方法

* 开发人员实现 SingleThreadModel 接口，由容器保证一个 service 方法同一时间点仅被一个请求线程调用（不推荐）
* Servlet 容器通过串行化访问 Servlet 的请求，或者维护一个 Servlet 实例池，如果 service 方法被 Synchronized 修饰，就不能使用池化方案，只能使用串行的方式进行处理

### 请求处理时的异常

Servlet 在处理请求时可能抛出 ServletException 或 UnavailableException 异常。

* ServletException 表示在处理请求时出现了一些错误，容器应该采取适当的措施处理掉这个请求
* UnavailableException 表示 servlet 目前无法处理请求，或者临时性的或者永久性的
  * 永久性的需要 Servlet 容器移除这个 Servlet，调用它的 destroy 方法，并释放 Servlet 实例，被拒绝的请求返回 404
  * 临时性的这段时间容器可以选择路由任何请求到 Servlet。这段时间内被容器拒绝的请求返回 503，同时会返回一个Retry-After头指示此Servlet什么时候可用

### 异步处理

Filter 和 Servlet 在生成响应之前必须等待一些资源或事件以便完成请求处理。Servlet 中等待是一个低效操作，因为是阻塞的，许多线程为了等待一个缓慢的资源可能引起线程饥饿，且降低整个 Web 容器的服务质量

3.0 引入了异步处理请求能力，使线程可以返回到容器，从而执行更多的任务。当开始异步处理请求时，另一个线程或回调可以产生响应，或者调用完成或请求分派，这样，他可以在容器上下文使用 AsyncContext.dispatch 方法运行。顺序如下：

1. 请求被接收到，通过一些列如用于校验等标准的 Filter 之后被传递到 Servlet
2. Servlet 发出请求参数及内容体从而确定请求类型
3. 该 Servlet 发出请求去获取一些资源或数据
4. Servlet 不产生响应并返回
5. 所请求的资源变为可用，此时处理线程继续处理事件，要么在同一个线程，要么通过 AsyncContext 分派到容器的其他线程执行（AsyncContext.start(Runnable)）方法，

*注意：从一个同步的 Servlet 分派到另一个异步 Servlet 是非法的。不过与该点不同的是当应用调用 startAsync 时将抛出 IllegalStateException*

### 线程安全

**除了 startAsync 和 complete 方法，请求和响应对象的实现都不保证线程安全**

### 升级处理

HTTP/1.1，Upgrade 通用头（general-header）允许客户端指定其支持和希望使用的其他通信协议

* 如果找到合适的切换协议，那么新的协议将在之后的通信中使用
* 如果没有，则继续使用之前的协议

Servlet 容器本身不知道任何协议升级

协议处理封装在 HttpUpgradeHandler 协议处理器（HttpServletRequest.upgrade方法启动升级处理）。在容器和 HttpUpgradeHandler 协议处理器之间通过字节流进行数据读取或写入

退出 Servlet#service 方法之后，Servlet 容器完成所有过滤器的处理并标记连接已交给 HttpUpgradeHandler 协议处理器处理。然后调用 HttpUpgradeHandler 协议处理器的 init 方法，传入一个 WebConnection 以允许 HttpUpgradeHandler 协议处理器访问数据流

* *一旦请求被升级，Servlet 过滤器将不会被调用*
* 协议处理器（ProtocolHandler）可以使用非阻塞 IO（non blocking IO）消费和生产消息
* 当处理 HTTP 升级时，开发人员负责线程安全的访问 ServletInputStream 和 ServletOutputStream
* 当升级处理已经完成，将调用HttpUpgradeHandler#destroy方法



## 终止服务

当 Servlet 容器确定 Servlet 应该从服务中移除时，将调用 Servlet 接口的 destroy 方法以允许 Servlet 释放它使用的任何资源和保存任何持久化的状态

在 Servlet 容器调用 destroy 方法之前，它必须让当前正在执行 service 方法的任何线程完成执行，或者超过了服务器定义的时间限制

一旦调用了 Servlet 实例的 destroy 方法，容器无法再路由其他请求到该 Servlet 实例了。如果容器需要再次使用该 Servlet，它必须用该 Servlet 类的一个新的实例

在 destory 方法完成后， Servlet 容器必须释放 Servlet 实例以便被垃圾回收