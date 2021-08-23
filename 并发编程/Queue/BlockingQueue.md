# 阻塞队列 BlockingQueue

## 继承关系

* Iterable
  * Collection
    * Queue
      * BlockingQueue



## 概念

队列是一种 FIFO 的数据结构，阻塞则是根据生产和消费有不同的阻塞条件

* 生产方
  * 如果在队列满了的时候，继续生产会造成阻塞
* 消费方
  * 如果在队列空了的时候，继续消费会造成阻塞



## 核心方法

* 继承
  * boolean add(E e);
    * 在队列插入一个元素，如果成功则返回 true，如果由于队列满了导致失败会抛出异常（`IllegalStateException`）
  * boolean offer(E e);
    * 在队列插入一个元素，如果成功则返回 true，如果由于队列满了导致失败失败会返回 false
  * boolean remove(Object o);
    * 从队列移除一个指定的元素，如果队列数据发生改变则返回 true，否则返回 false
  * boolean contains(Object o);
    * 如果队列包含这个元素，则返回 true，否则返回 false
  * E poll();
    * 返回并移除队列的头元素，如果没有，则返回 null
  * E element();
    * 返回但是不移除队列的头元素，如果是空队列，则抛出异常（`NoSuchElementException`）
  * E peek();
    * 返回但是不移除队列的头元素，如果没有，则返回 null
* 自己的抽象方法
  * void put(E e) throws InterruptedException;
    * 在队列插入一个元素，如果队列满了，则阻塞等待队列有空间可以插入
  * boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException;
    * 在队列插入一个元素，如果队列满了，则阻塞等待指定时间，等待期间内如果还是没有空间，则返回 false
  * E take() throws InterruptedException;
    * 返回并移除队列的头元素，如果没有，则阻塞等待有元素插入队列
  * E poll(long timeout, TimeUnit unit) throws InterruptedException;
    * 返回并移除队列的头元素，如果没有，则阻塞等待指定时间，等待期间内如果还是没有元素插入，则返回 null
  * int remainingCapacity();
    * 返回剩下的可接受无需阻塞的元素的个数，如果是没有限制，则返回 `Integer.MAX_VALUE`
  * int drainTo(Collection<? super E> c);
    * 从队列中移除所有的元素，并将这些元素转移到给定的集合中
  * int drainTo(Collection<? super E> c, int maxElements);
    * 从队列中移除给定数量的元素，如果不够，则移除所有的元素，并将这些元素转移到给定的集合中



## 实现类

* ArrayBlockingQueue
  * 基于数组实现的**有界**的阻塞队列，默认情况下不保证阻塞后的元素操作公平性（先被阻塞的元素先进行操作）
* LinkedBlockingQueue
  * 基于链表实现的阻塞队列，可以根据构造函数的入参确定队列的大小，如果不设置，默认是 `Integer.MAX_VALUE`，其实现是采用两个锁（`ReentrantLock`）来保证数据同步的，队列头使用的是写锁（`takeLock`），而队列尾部使用的是读锁（`putLock`）
* PriorityBlockingQueue
  * 基于数组实现的**无界**的具有优先级的阻塞队列，利用 `ReentrantLock` 保证数据同步。如果元素没有实现 `Comparable` 接口，构造函数也没有传入 `Comparator`，则会抛出异常（`ClassCastException`），否则优先使用传入的 `Comparator`，如果优先级相同，则不保证顺序
* DelayQueue
  * 基于 PriorityQueue 实现（底层也是数组）的支持延时获取元素的**无界**阻塞队列。放入后会根据 `Comparable` 进行排序（`Delayed` 接口实现了 `Comparable`），队列的元素必须实现 Delay 接口，否则会抛出异常（`ClassCastException`），底层会有一个死循环不断地判断 getDelay() 方法返回的值是否小于 0，小于 0 就返回该对象
* SynchronousQueue
  * 一个不存储元素的阻塞队列，每个 put 操作都必须等待一个 take 操作（显然，这需要两个线程才能实现），每个 take 操作也必须等待一个 put 操作。适用于一个线程把数据传输给另一个线程，吞吐量高于 `ArrayBlockingQueue` 和 `LinkedBlockQueue`，其内部主要是静态内部类 `TransferQueue` 和 `TransferStack` 实现的各个功能，根据构造参数是否公平创建对应的内部实现对象，默认不公平，创建 `TransferStack` 
* LinkedTransferQueue
  * 基于链表实现的**无界**的阻塞队列，在 `BlockingQueue` 的基础能力上，添加了三个方法
    * void transfer(E e)
      * 如果有消费者正在等待接收元素，则直接把该对象投递给消费者并返回，否则把自己挂起
    * boolean tryTransfer(E e)
      * 如果有消费者正在等待接收元素，则直接把该对象投递给消费者并返回 true，否则返回 false
    * boolean tryTransfer(E e, long timeout, TimeUnit unit)
      * 如果有消费者正在等待接收元素，则直接把该对象投递给消费者并返回 true，否则等待时间内如果都没有接收到元素就返回 false

