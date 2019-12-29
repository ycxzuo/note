# 高并发笔记

## 概述

为了提高服务器性能，高并发的使用显得尤为重要。经常会出现线程不安全的问题或者锁问题，这里重点讲线程安全问题和一些必备知识

### 必备知识

* 进程和线程
  * 进程是计算机中的程序关于某数据集合啥功能的一次调用活动，是系统资源调用和调度的基本单位
  * 线程是程序执行流的最小单元，一般一个进程会有多个线程
* 并发和并行
  * 并发是表示多任务**交替运行**
  * 并行是表示多任务**同时执行**
* 异步和同步
  * 异步在方法调用时就返回了
  * 同步必须等到调用的方法结束后才能执行后续操作
* 阻塞和非阻塞
  * 阻塞是表示在访问同一个资源时多个线程之间会相互影响
  * 非阻塞是表示多个线程可以同时访问这一个资源

## 线程

### 线程的状态

* `NEW`
  * 初始状态，线程被构建，但还没有调用`start()`方法
* `RUNNABLE`
  * 运行状态， Java 线程把操作系统中的就绪和运行状态统一称为`运行中`
* `BLOCKED`
  * 阻塞状态，表示线程进入等待状态，也就是线程因为某种原因放弃了`CPU`使用权，阻塞分为
    * 等待阻塞 -> 运行的线程执行`wait()`方法， JVM 会把当前线程放入到等待队列
    * 同步阻塞 -> 运行的线程在获取对象的同步锁时，该同步锁已经被占用，JVM 会把当前线程放入锁池
    * 其他阻塞 -> 运行的线程执行`Thread.sleep()`或`join()`方法，或发出了`I/O`请求，JVM 会把当前线程设置为阻塞状态，当`sleep()`结束、`join()`线程终止、`I/0`处理完毕后线程恢复
* `WAITING`
  * 等待状态，与其他阻塞一致
* `TIMED_WAITING`
  * 超时等待状态，超时后自动返回
* `TERMINATED`
  * 终止状态，表示当前线程执行完毕

[详细代码](https://github.com/ycxzuo/DemoCode/blob/master/Concurrent/src/com/yczuoxin/concurrent/demo/thread/state/ThreadStatusDemo.java)

> 运行后找到对应的 .class 文件，打开命令行输入 jps 查看ThreadStatusDemo 的 pid，使用 jstack pid 查看各个线程状态

### 线程的状态转换

![状态转换](http://wx3.sinaimg.cn/mw690/0060lm7Tly1fyeebv7diej30nx0ik3zq.jpg)

### 线程的优先级

每个线程都有自己的优先级，优先级的数据类型是整型，范围是 1（Thread.MIN_PRIORITY） ~ 10（Thread.MAX_PRIORITY）。默认情况，每一个线程都会分配一个优先级 5（NORM_PRIORITY）

线程优先级不能保证线程执行的顺序

### sleep()方法

使调用该方法的线程暂停执行一段时间，让其他线程有机会继续执行，使得线程优先级低的线程有机会运行，它不会释放对象锁

### join()方法

使调用该方法的线程在此之前执行完毕后，再继续往下执行

### yield()方法

该方法与`sleep()`方法类似，只是用户不能执行暂停多长时间，且**只能让同优先级的线程有执行机会**

### 线程的停止

`Thread.stop`和`Thread.suspend`已经不推荐使用，因为该方法在结束一个线程时，并不能保证线程资源正常释放，所以要中断一个线程，可以使用`interrupt()`方法

#### interrupt()方法

当其他线程通过调用当前线程的`interrupt()`方法，表示告诉该线程要中断了，至于什么时候中断，取决于当前线程自己，线程可以通过`isInterrupted()`方法来判断是否中断。这种方法可以在使线程终止时有机会去清理资源，而不是武断的将线程停止

```c++
void os::interrupt(Thread* thread) {
assert(Thread::current() == thread ||
Threads_lock->owned_by_self(),
"possibility of dangling Thread pointer");
OSThread* osthread = thread->osthread();
if (!osthread->interrupted()) {
osthread->set_interrupted(true);
// More than one thread can get here with the same value of
osthread,
// resulting in multiple notifications. We do, however, want the
store
// to interrupted() to be visible to other threads before we
execute unpark().
OrderAccess::fence();
ParkEvent * const slp = thread->_SleepEvent ;
if (slp != NULL) slp->unpark() ;
}
// For JSR166. Unpark even if interrupt status already was set
if (thread->is_Java_thread())
((JavaThread*)thread)->parker()->unpark();
ParkEvent * ev = thread->_ParkEvent ;
if (ev != NULL) ev->unpark() ;
}
```



#### Thread.interrupted()方法

线程中还提供了静态方法`Thread.interrupted()`方法对设置中断标志的**线程复位**（将标志位设置为`false`），还有一种被动复位的场景，就是对抛出了`interruptedException`异常的方法，在抛异常之前，JVM 会先把线程的中断标志位清除，这样线程就可以快速退出阻塞状态。也可以用`Valotile`关键字设置一个共享变量，达到这个效果



## 线程安全

说到线程安全问题就会想到几个经常在提的例子

* `HashMap`和`ConcurrentHashMap`
  * `ConcurrentHashMap`将`HashMap`的`putValue()`方法、`replaceNode()`方法等加上了`synchronized`关键字
* `StringBuilder`和`StringBuffer`
  * `StringBuffer`将`append()`方法、`replace()`方法等加上了`synchronized`关键字，而`StringBuilder`没有

......

高并发情况下可能会导致多个线程同时去修改某一个共享资源，导致该资源的数据出现了脏数据。线程安全就是保证该资源在一个线程调用时，其他线程都必须等待其执行完毕，才能去竞争

### Java 内存模型方面

内存模型定义了共享内存系统中多线程程序读写操作行为的规范，来屏蔽各种硬件和操作系统的内存访问差异，来实现 Java 程序在各个平台下都能达到一致的内存访问效果。Java 内存模型的**主要目标是定义程序中各个变量的访问规则，也就是在虚拟机中将变量存储到内存以及从内存中取出变量（这里的变量，指的是共享变量，也就是实例对象、静态字段、数组对象等存储在堆内存中的变量。而对于局部变量这类的，属于线程私有，不会被共享）这类的底层细节。通过这些规则来规范对内存的读写操作，从而保证指令执行的正确性。**它与处理器有关、与编译器有关、与缓存有关、与并发有关。他解决了 CPU多级缓存、处理器优化、指令重排等导致的内存访问问题，保证了并发场景下的可见性、原子性和有序性。**内存模型解决并发问题主要采用两种方式 -> 限制处理器优化**和**使用内存屏障**

- 原子性
  - 指一个操作是不可分的，例如常用的反例`i++`操作，实际上会执行三步 -> 读取，增加，赋值
- 可见性
  - 可见性是指一个线程对共享变量的修改，对于另一个线程来说是否是可以看到的。因为每个线程操作的是自己的工作内存
- 有序性
  - 有序性是指程序执行的时候，代码执行顺序和语句的顺序一致。编译器在不改变语义的前提下，可以对执行顺序进行重排。如果在高并发情况下，可能会导致结果不一致

**Java 内存模型定义了线程和内存的交互方式**。在 JMM 抽象模型中，分为

* 主内存
* 工作内存

主内存是所有线程共享的，工作内存是每个线程独有的。**线程对变量的所有操作（读取、赋值）都必须在工作内存中进行，不能直接读写主内存中的变量。并且不同的线程之间无法访问对方工作内存中的变量，线程间的变量值的传递都需要通过主内存来完成**

