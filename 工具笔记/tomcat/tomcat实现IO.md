# Tomcat 实现 I/O

## NioEndpoint 组件

Tomcat 的 NioEndpoint 组件实现了 I/O 多路复用模型

### 总体工作流程

对于 Java 的多路复用器的使用，无非是两步

* 创建一个 Selector，在它身上注册各种感兴趣的事件，然后调用 select 方法，等待感兴趣的事情发生
* 感兴趣的事情发生了，比如可读了，这时便创建一个新的线程从 Channel 中读取数据



Tomcat 的 NioEndpoint 组件虽然实现比较复杂，但基本原理就是上面两步，这个组件包含以下组件

* LimitLatch
  * 是连接控制器，它负责控制最大连接数，NIO 模式下默认是 10000，达到这个阈值后，连接请求被拒绝
* Acceptor
  * 跑在一个单独的线程里，它在一个死循环里调用 accept 方法来接收新连接，一旦有新的连接请求到来，accept 方法返回一个 Channel 对象，接着吧 Channel 对象交给 Poller 去处理
* Poller
  * 本质是一个 Selector， 也跑在单独线程里，Poller 在内部维护一个 Channel 数组，他在死循环里不断检测 Channel 的数据就绪状态，一旦有 Channel 可读，就生成一个 SocketProcessor 任务对象扔给 Executor 去处理
* SocketProcessor
* Executor
  * 本质是一个线程池，负责运行 SocketProcessor 任务类，SocketProcessor 的 run 方法会调用 Http11Processor 来读取和解析请求数据，Http11Processor 是应用层协议的封装，它会调用容器获得响应，再把相应通过 Channel 写出



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

Http11Processor 并不是直接读取 Channel 的，这是因为 Tomcat 支持同步非阻塞 I/O 模型和异步 I/O 模型，在 Java API 中，相应的 Channel 类也是不一样的，比如有 AsynchronusSocketChannel 和 SocketChannel，为了对 Http11Processor 屏蔽这些差异，Tomcat 设计了一个包装类叫做 SocketWrapper，Http11Processor 只调用 SocketWrapper 的方法去读写数据



#### Executor

Executor 是 Tomcat 定制的线程池，它负责创建真正干活的工作线程，来执行 SocketProcessor 的 run 方法，也就是解析请求并通过容器来处理请求，最后会调用我们的 servlet



### 高并发思路

高并发就是能快速的处理大量的请求，需要合理设计线程模型让 CPU 忙起来，尽量不要让线程阻塞，因为一阻塞，CPU 就闲下来了。另外就是有多少任务，就用相应规模的线程去处理。NioEndpoint 要完成三件事

* 接收连接
* 检测 I/O 事件
* 处理请求

最核心的是把这三件事情分开，用不同规格的线程数去处理，比如用专门的线程组跑 Acceptor，并且 Acceptor 的个数可配置，用专门的线程组去跑 Poller，Poller 的个数也可以配置，最后执行具体任务也由专门的线程池来处理，也可以配置线程池的大小



## 总结

*I/O 模型是为了解决内存和外部设备速度差异的问题，我们平时说的阻塞或非阻塞是指应用程序发起 I/O 操作时，是立即返回还是等待；异步和同步是指程序在与内存通信时，数据从内核空间到应用空间的拷贝，是由内核主动发起还是由应用程序来触发*

