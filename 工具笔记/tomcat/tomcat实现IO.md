# Tomcat 实现 I/O

## NioEndpoint 组件

Tomcat 的 NioEndpoint 组件实现了 I/O 多路复用模型

### 总体工作流程

对于 Java 的多路复用器的使用，无非是两步

* 创建一个 Selector，在它身上注册各种感兴趣的事件，然后调用 select 方法，等待感兴趣的事情发生
* 感兴趣的事情发生了，比如可读了，这时便创建一个新的线程从 Channel 中读取数据



Tomcat 的 NioEndpoint 组件虽然实现比较复杂，但基本原理就是上面两步，这个组件包含以下组件

![NioEndpoint](http://tva1.sinaimg.cn/large/007X8olVly1g7bzux3ft5j30yh0nwak4.jpg)

* LimitLatch
  * 是连接控制器，它负责控制最大连接数，NIO 模式下默认是 10000，达到这个阈值后，连接请求被拒绝
* Acceptor
  * 跑在一个单独的线程里，它在一个死循环里调用 accept 方法来接收新连接，一旦有新的连接请求到来，accept 方法返回一个 Channel 对象，接着吧 Channel 对象交给 Poller 去处理
* Poller
  * 本质是一个 Selector， 也跑在单独线程里，Poller 在内部维护一个 Channel 数组，他在死循环里不断检测 Channel 的数据就绪状态，一旦有 Channel 可读，就生成一个 SocketProcessor 任务对象扔给 Executor 去处理
* SocketProcessor
* Executor
  * 本质是一个线程池，负责运行 SocketProcessor 任务类，SocketProcessor 的 run 方法会调用 Http11Processor 来读取和解析请求数据，Http11Processor 是应用层协议的封装，它会调用容器获得响应，再把相应通过 Channel 写出
* Http11Processor
  * 线程池在执行 SocketProcessor 是会调用 Http11Processor 去处理请求，Http11Processor 会通过 NioSocketWrapper 读写数据
* NioSocketWrapper
  * 读写数据并发送



#### LimitLatch

用来控制连接个数，当连接数达到最大时，阻塞线程，直到后续组件处理完一个连接口将连接数 -1，**注意达到最大连接数后，操作系统底层还是会接收客户端连接，但用户层已经不再接收**

```java
public class LimitLatch {
    private class Sync extends AbstractQueuedSynchronizer {
     
        @Override
        protected int tryAcquireShared() {
            long newCount = count.incrementAndGet();
            if (newCount > limit) {
                count.decrementAndGet();
                return -1;
            } else {
                return 1;
            }
        }

        @Override
        protected boolean tryReleaseShared(int arg) {
            count.decrementAndGet();
            return true;
        }
    }

    private final Sync sync;
    private final AtomicLong count;
    private volatile long limit;
    
    // 线程调用这个方法来获得接收新连接的许可，线程可能被阻塞
    public void countUpOrAwait() throws InterruptedException {
      sync.acquireSharedInterruptibly(1);
    }

    // 调用这个方法来释放一个连接许可，那么前面阻塞的线程可能被唤醒
    public long countDown() {
      sync.releaseShared(0);
      long result = getCount();
      return result;
   }
}
```

从上面的代码可以看出，LimitLatch 内部定义了内部类 Sync，而 Sync 扩展了 AQS，AQS 是 Java 并发包中的一个核心类，它在内部维护一个状态和一个线程队列，可以用来控制线程什么时候挂起，什么时候唤醒。我们可以扩展它来实现自己的同步器，实际上 Java 并发包里的锁和条件变量等等都是通过 AQS 来实现的。

* 用户线程通过调用 LimitLatch 的 countUpAwait 方法拿到锁，如果暂时无法获取，这个线程会被阻塞到 AQS 的队列中，那 AQS 怎么知道是阻塞还是不阻塞用户线程呢？这是由 AQS 的使用者来决定的，也就是内部类 Sync 来决定的，因为 Sync 类重写了 AQS 的 tryAcquireShared() 方法，它的实现逻辑是如果当前连接数 count 小于 limit，线程能获取锁，返回 1，否则返回 -1
* 如果用户线程被阻塞到了 AQS 的队列，会被 Sync 类重写的 AQS 的 tryReleaseShared() 方法去唤醒，其实就是当一个连接请求处理完了，这是可以接收一个新连接，这样前面阻塞的线程将会被唤醒



#### Acceptor

实现了 Runnable 接口，因此可以跑在单线程里，一个端口号只能对应一个 ServerSocketChannel，因此这个 ServerSocketChannel 是在多个 Acceptor 线程之间共享的，它是 Endpoint 的属性，由 Endpoint 完成初始化和端口绑定

```java
serverSock = ServerSocketChannel.open();
serverSock.socket().bind(addr,getAcceptCount());
serverSock.configureBlocking(true);
```

从上面代码可以看出

* bind 方法的第二个参数表示操作系统的等待队列长度，我在上面提到，当应用层面的连接数到达最大值时，操作系统可以继续接收连接，那么操作系统能继续接收的最大连接数就是这个队列长度，可以通过 acceptCount 参数配置，默认是 100
* ServerSocketChannel 被设置成阻塞模式，也就是说它是以阻塞的方式接收连接的

ServerSocketChannel 通过 accept() 接受新的连接，accept() 方法返回获得 SocketChannel 对象，然后将 SocketChannel 对象封装在一个 PollerEvent 对象中，并将 PollerEvent 对象压入 Poller 的 Queue 里，这是典型的 生产者-消费者 模式，Acceptor 与 Poller 线程之间通过 Queue 通信



#### Poller

本质是一个 Selector，它内部维护一个 Queue

```java
private final SynchronizedQueue<PollerEvent> events = new SynchronizedQueue<>();
```

SynchronizedQueue 的方法比如 offer、poll、size 和 clear 方法，都使用了 synchronized 关键字进行修饰，用来保证同一时刻只有一个 Acceptor 线程对 Queue 进行读写。同时有多个 Poller 线程在运行，每个 Poller 线程都有自己的 Queue，每个 Poller 线程可能同时被多个 Acceptor 线程调用来注册 PllerEvent。同样，Poller 的个数可以通过 pollers 参数配置

Poller 不断的通过内部的 Selector 对象向内核查询 Channel 的状态，一旦可读就生成任务类 SocketProcessor 交给 Executor 去处理，Poller 的另一个重要任务是循环遍历检查自己的所管理的 SocketChannel 是否已经超时，如果有超时，就关闭这个 SocketSelector



#### SocketProcessor

Poller 会创建 SocketProcessor 任务类交给线程池处理，而 SocketProcessor 实现了 Runnable 接口，用来定义 Executor 中线程所执行的任务，主要就是调用 Http11Processor 组件来处理请求。Http11Processor 读取 Channel 的数据生成 ServletRequest 对象

Http11Processor 并不是直接读取 Channel 的，这是因为 Tomcat 支持同步非阻塞 I/O 模型和异步 I/O 模型，在 Java API 中，相应的 Channel 类也是不一样的，比如有 AsynchronusSocketChannel 和 SocketChannel，为了对 Http11Processor 屏蔽这些差异，Tomcat 设计了一个包装类叫做 SocketWrapper，Http11Processor 只调用 SocketWrapper 的方法去读写数据，在 NioEndpoint 中 SocketProcessor 持有 NioSocketWarpper



#### Executor

Executor 是 Tomcat 定制的线程池，它负责创建真正干活的工作线程，来执行 SocketProcessor 的 run 方法，也就是解析请求并通过容器来处理请求，最后会调用我们的 servlet



### 高并发思路

高并发就是能快速的处理大量的请求，需要合理设计线程模型让 CPU 忙起来，尽量不要让线程阻塞，因为一阻塞，CPU 就闲下来了。另外就是有多少任务，就用相应规模的线程去处理。NioEndpoint 要完成三件事

* 接收连接
* 检测 I/O 事件
* 处理请求

最核心的是把这三件事情分开，用不同规格的线程数去处理，比如用专门的线程组跑 Acceptor，并且 Acceptor 的个数可配置，用专门的线程组去跑 Poller，Poller 的个数也可以配置，最后执行具体任务也由专门的线程池来处理，也可以配置线程池的大小



### 总结

*I/O 模型是为了解决内存和外部设备速度差异的问题，我们平时说的阻塞或非阻塞是指应用程序发起 I/O 操作时，是立即返回还是等待；异步和同步是指程序在与内存通信时，数据从内核空间到应用空间的拷贝，是由内核主动发起还是由应用程序来触发*



## Nio2Endpoint 组件

Nio 与 Nio2 最大的区别是 Nio 是同步的，而 Nio2 是异步的，异步的特点是应用程序不需要自己去触发数据从内核空间到用户空间拷贝。**Nio2 内核主动将数据拷贝到用户空间并通知应用程序，期间应用程序都是非阻塞的；**Nio 则是等待应用程序通过 Selector 来查询，当数据就绪后，应用程序再发起一个 read 调用，这是内核再把数据从内核空间拷贝到用户空间，这个拷贝阶段应用程序是阻塞的

首先，应用程序在调用 read API 的同时告诉内核两件事

* 数据准备好了以后拷贝到哪个 Buffer
* 调用哪个回调函数去处理这些数据

之后，内核接到这个 read 指令后，等待网卡数据到达，数据到了后，产生硬件中断，内核在中断程序把数据从网卡拷贝到内核空间，接着做 TCP/IP 协议层面的数据解包和充足，再把数据拷贝到应用程序指定的 Buffer，最后调用应用程序指定的 Buffer，最后调用应用程序指定的回调函数

![IO多路复用和异步对比](http://tva1.sinaimg.cn/large/007X8olVly1g7bsntv700j312u0i20vt.jpg)

在异步模式下，应用程序调用 read 后就做自己的事情，内核则是负责一系列操作，最大限度提高了 I/O 通信的效率



### Java Nio2

用 Java 的 Nio2 API 写一个服务器端程序

```java
public class Nio2Server {

   void listen(){
      //1. 创建一个线程池
      ExecutorService es = Executors.newCachedThreadPool();

      //2. 创建异步通道群组
      AsynchronousChannelGroup tg = AsynchronousChannelGroup.withCachedThreadPool(es, 1);
      
      //3. 创建服务端异步通道
      AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open(tg);

      //4. 绑定监听端口
      assc.bind(new InetSocketAddress(8080));

      //5. 监听连接，传入回调类处理连接请求，this 是 Nio2Server 自己
      assc.accept(this, new AcceptHandler()); 
   }
}
```

上述代码主要做了 5 件事情

* 创建一个线程池，这个线程池用来执行来自系统内核的回调请求
  * 在异步 I/O 模型里，应用程序不知道数据在什么时候到达，因此向内核注册回调函数，当数据到达时，内核就会调用这个回调函数，同时为了提高处理速度，会提供一个线程池给内和使用，这样不会耽误内核线程的工作，内核只需要把工作交给线程池就立即返回了
* 创建一个 AsynchrousChannelGroup，并绑定一个线程池
* 创建一个 AsynchrousServerSocketChannel，并绑定到 AsynchrousChannelGroup
* 绑定一个端口监听
* 调用 accept 方法开始监听连接请求，同时传入一个回调类去处理连接请求



处理连接的回调类 **AccpetHandler** 是什么样的

```java
//AcceptHandler 类实现了 CompletionHandler 接口的 completed 方法。它还有两个模板参数，第一个是异步通道，第二个就是 Nio2Server 本身
public class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Nio2Server> {

   // 具体处理连接请求的就是 completed 方法，它有两个参数：第一个是异步通道，第二个就是上面传入的 NioServer 对象
   @Override
   public void completed(AsynchronousSocketChannel asc, Nio2Server attachment) {      
      // 调用 accept 方法继续接收其他客户端的请求
      attachment.assc.accept(attachment, this);
      
      //1. 先分配好 Buffer，告诉内核，数据拷贝到哪里去
      ByteBuffer buf = ByteBuffer.allocate(1024);
      
      //2. 调用 read 函数读取数据，除了把 buf 作为参数传入，还传入读回调类
      channel.read(buf, buf, new ReadHandler(asc)); 

}
```

实现了 CompletionHandler 接口

```java
public interface CompletionHandler<V,A> {

    void completed(V result, A attachment);

    void failed(Throwable exc, A attachment);
}
```

CompletionHandler 有两个泛型参数 V 和 A，分别表示 I/O 调用的返回值和附件类，比如 accept 的返回值就是 AsynchronousSocketChannel，而附件类由用户自己决定，在 accept 的调用中，我们传入了一个 Nio2Server，因此 AcceptHandler 带有了两个模板参数 AsynchronousSocketChannel 和 Nio2Server

CompletionHandler 有两个方法

* completed
  * I/O 操作成功时调用，然后将上面两个模板参数，也就是说，Java 的 Nio2 在调用回调方法时，会把返回值和附件类当做参数传给 Nio2 的使用者
* failed
  * I/O 操作失败时调用



处理读的回调类 **ReadHandler**

```java
public class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {   
    // 读取到消息后的处理  
    @Override  
    public void completed(Integer result, ByteBuffer attachment) {  
        //attachment 就是数据，调用 flip 操作，其实就是把读的位置移动最前面
        attachment.flip();  
        // 读取数据
        ... 
    }  

    void failed(Throwable exc, A attachment){
        ...
    }
}
```

read 调用的返回值是一个整型数，所以我们回调方法第一个参数是整型，表示有多少数据被读取到了 Buffer 中，第二个参数是一个 ByteBuffer，这是因为我们在调用 read 方法时，把用来存放数据的 ByteBuffer 当作附件类传进去了，所以在回调方法里，有 ByteBuffer 类型的参数，我们直接从这个 ByteBuffer 里获取数据



### Nio2Endpoint

掌握了 Java Nio2 API 的使用以及服务端车功能虚的工作原理之后，再来理解 Tomcat 的异步 I/O 实现就不难了，我们先看看 Nio2Endpoint 有哪些组件

![Nio2Endpoint](http://tva1.sinaimg.cn/large/007X8olVly1g7bzwffb4ij30sv0nlqc0.jpg)

* LimitLatch
* Nio2Acceptor
* SocketProcessor
* Executor
* Http11Processor
* Nio2SocketWrapper

基本与 NioEndpoint 相似



### LimitLatch

是连接控制器，它负责控制最大连接数



### Nio2Acceptor

扩展了 Acceptor，用异步 I/O 的方式来接收连接，跑在一个单独地线程里，也是一个线程组，Nio2Acceptor 接收新的连接后，得到一个 AsynchronousSocketChannel，Nio2Acceptor 把 AsynchronousSocketChannel 封装成一个Nio2SocketWrapper，并创建一个 SocketProcessor 任务类交给线程池处理，并且 SocketProcessor 持有 Nio2SocketWrapper 对象



### Executor

执行 SocketProcessor 时，SocketProcessor 的 run 方法会调用 Http11Processor 来处理请求，Http11Processor 会通过 Nio2SocketWrapper 读取和解析请求数据，请求经过容器处理后，再把响应通过 Nio2SocketWrapper 写出



**Nio2Endpoint 中没有 Poller 组件，也就是没有 Selector，因为在异步 I/O 模式下，Selector 的工作交给内核来做了**



### NioAcceptor

和 NioEndpoint 一样，Nio2Endpoint 的基本思路使用 LimitLatch 组件来控制连接数，但是 Nio2Acceptor 的监听连接的过程不是一个死循环里不断地调用 accept 方法。而是通过回调函数来完成的

```java
serverSock.accept(null, this);
```

其实就是调用了 accept 方法，注意它的第二个参数是 this，表明 Nio2Acceptor 自己就是处理连接的回调类，因此 Nio2Acceptor 实现了 CompletionHandler 接口

```java
protected class Nio2Acceptor extends Acceptor<AsynchronousSocketChannel>
    implements CompletionHandler<AsynchronousSocketChannel, Void> {
    
@Override
public void completed(AsynchronousSocketChannel socket,
        Void attachment) {
        
    if (isRunning() && !isPaused()) {
        if (getMaxConnections() == -1) {
            // 如果没有连接限制，继续接收新的连接
            serverSock.accept(null, this);
        } else {
            // 如果有连接限制，就在线程池里跑 run 方法，run 方法会检查连接数
            getExecutor().execute(this);
        }
        // 处理请求
        if (!setSocketOptions(socket)) {
            closeSocket(socket);
        }
    } 
}
```

CompletionHandler 的两个模板参数分别是 AsynchronousServerSocketChannel 和 Void，completed 方法的处理逻辑比较简单

* 如果没有连接限制，继续在本线程中调用 accept 方法接收新的连接
* 如果有连接限制，就在线程池里跑 run 方法去接收新的连接，那为什么要跑 run 方法呢，因为在 run 方法里会检查连接数，当连接达到最大数时，线程可能会被 LimitLatch 阻塞，为什么要放在线程池里跑呢？这是因为如果放在当前线程里执行，completed 方法可能被阻塞，会导致这个回调方法一直不返回

接着 completed 方法会调用 setSocketOptions 方法，在这个方法里，会创建 Nio2SocketWrapper 和SocketProcessor，并交给线程池处理



### Nio2SocketWrapper

主要作用是封装 Channel，并提供接口给 Http11Processor 读写数据，在 Http11Processor 在调用 Nio2SocketWrapper 的read 方法时需要注册回调类，read 调用会立即返回，问题是立即返回后 Http11Processor 还没有读到数据，为了解决这个问题，Http11Processor 是通过 2 次 read 调用来完成数据读取操作的

* 第一次调用 read 方法，连接刚刚建立好后，Acceptor 创建 SocketProcessor 任务类交给线程池去处理，同时注册了回调函数 readCompletionHandler，因为数据没读到，Http11Processor 把当前的 Nio2SocketWrapper 标记为不完整。接着 SocketProcessor 线程被回收，Http11Processor 并没有阻塞等待数据。Http11Processor 维护了一个 Nio2SocketWrapper 列表，也就是维护了连接的状态
* 第二次调用 read 方法，当数据到达后，内核已经把数据拷贝到 Http11Processor 指定的 Buffer 里，同时回调类 readCompletionHandler 被调用，在这个新的 SocketProcessor 任务类持有原来那个 Nio2SocketWrapper，这一次 Http11Processor 可以通过 Nio2SocketWrapper 读取数据了，因为数据已经打了应用层的 Buffer

这个回调类 readCompletionHandler 的源码

```java
this.readCompletionHandler = new CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>>() {
    public void completed(Integer nBytes, SocketWrapperBase<Nio2Channel> attachment) {
        ...
        // 通过附件类 SocketWrapper 拿到所有的上下文
        Nio2SocketWrapper.this.getEndpoint().processSocket(attachment, SocketEvent.OPEN_READ, false);
    }

    public void failed(Throwable exc, SocketWrapperBase<Nio2Channel> attachment) {
        ...
    }
}
```

Nio2SocketWrapper 是作为附件类来传递的，这样在回调函数中能拿到所有的上下文



