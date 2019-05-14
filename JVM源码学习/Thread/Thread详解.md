# Thread详解

## 基础概念

### 进程、线程和协程

#### 进程

进程是系统进行资源分配和调度的一个独立单位



#### 线程

CPU调度和分派的基本单位



#### 协程

由用户控制一个线程的运作



#### 并行和并发

* 并行 -> CPU 同时执行多个任务
* 并发 -> CPU 交替执行多个任务



## Thread类

### 字段

```java
/* 是否单步执行线程 */
private boolean     single_step;

/* 是否为守护线程 */
private boolean     daemon = false;

/* JVM 状态 */
private boolean     stillborn = false;

/* run 方法执行的目标代码 */
private Runnable target;

/* 线程所属的线程组 */
private ThreadGroup group;

/* 线程的类加载器 */
private ClassLoader contextClassLoader;

/* 线程继承的AccessControlContext */
private AccessControlContext inheritedAccessControlContext;

/* 用于生成默认线程名称的值 */
private static int threadInitNumber;

/* 该线程存储 */
ThreadLocal.ThreadLocalMap threadLocals = null;

/* 该线程父类及以上的存储 */
ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;

/* 栈深度 */
private long stackSize;

/* JVM 虚拟机用的参数 */
private long nativeParkEventPointer;

/* 线程 ID */
private long tid;

/* 用于生成线程 ID */
private static long threadSeqNumber;

/* 线程的状态 */
private volatile int threadStatus = 0;

/* 用于调用java.util.concurrent.locks.LockSupport.park方法 */
volatile Object parkBlocker;

/* 用于 NIO  */
private volatile Interruptible blocker;
private final Object blockerLock = new Object();

/* 线程优先级最低 */
public final static int MIN_PRIORITY = 1;

/* 线程优先级默认 */
public final static int NORM_PRIORITY = 5;

/* 线程优先级最高 */
public final static int MAX_PRIORITY = 10;
```



### 方法

*JVM 源码中 `JavaThread` 继承自 `Thread`*

#### init(ThreadGroup, Runnable, String, long, AccessControlContext, boolean)

`Thread` 初始化方法，这个方法是初始化一个 Java 中的 Thread 对象，并不会 OS 上创建一个对应的线程

##### ThreadGroup g

创建线程所属的用户组，其数据结构是一个以 `system` 为名的 `ThreadGroup` 为根节点的树形结构，其默认值是 null，就会取创建该线程的线程组作为自己线程组，

##### Runnable target

需要运行的目标代码，默认是 null，及空转一次

##### String name

线程的名称，默认是 `Thread-n` ，n 从 0 开始

##### long stackSize

新线程所需的栈深度，默认值为 0，即 JVM 参数

##### AccessControlContext acc

线程的上下文资源

##### boolean inheritThreadLocals

是否需要持有父类的 `threadlocals`，默认值是 true



#### start()

该方法用 synchronized 关键字修饰，为了保证线程在创建的源头上保证线程安全，同一个线程不能启动两次，否则会抛出异常 `IllegalThreadStateException` ，然后线程组将启动线程放入启动线程数组中，并将未启动线程数量 -1，然后调用本地方法 `start0` ，对应 JVM 源码中 thread.cpp 451行。如果执行失败，该线程会被从线程组移除并被垃圾收集器回收



#### interrupt()

该方法在使用 NIO 时，调用 `blockedOn(Interruptible b)` 方法会设置 blocker，如果 NIO 通道实现了 `InterruptibleChannel` 接口，就可以响应 `interrupt()` 中断，在 `InterruptibleChannel` 接口的抽象实现类 `AbstractInterruptibleChannel` 的方法 `begin()` 中，在 `Channel.ReadableByteChannelImpl.read()` 和 `Channel.ReadableByteChannelImpl.write()` 中都先使用 `begin()` 方法保证线程不处于中断状态。然后调用本地方法 `interrupt0()` ，对应 JVM 源码在 thread.cpp 795行。



#### exit()

该方法是私有方法，并且只能由系统调用，用来清除线程中的信息，并确保能完全退出线程，Java 是无法销毁一个线程的，销毁线程是 JVM 操作的



#### static interrupted()

这个方法是判断**执行此操作的线程**的中断状态，并且重置中断标志。注意是执行此操作的线程而不是调用此操作的线程



#### isInterrupted()

这个方法是判断调用线程的中断状态



#### native isInterrupted(boolean ClearInterrupted)

判断线程是否有中断标志，并根据 `ClearInterrupted` 的值决定是否重置中断状态。对应 JVM 源码在 thread.cpp 801行。



#### native isAlive()

判断线程（OS 线程）是否还存活，对应 JVM 源码在 javaClasses.cpp 942行。



