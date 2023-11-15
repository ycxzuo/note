# Synchronized 概述

## 概述

synchronized 关键字是 Sun 公司封装的一个锁工具，其在 JDK 1.6 之前就是一把重量级锁，但是在 JDK 1.6 及之后， synchronized 关键字进行了升级，锁会不断地升级

* 偏向锁
* 轻量级锁
* 重量级锁



## 文档

[官方参考](https://wiki.openjdk.java.net/display/HotSpot/Synchronization)



## 对象实例

JVM 中，一个对象实例对应的 instanceOopDesc

其中包括对象头（markOop）、元数据（kclass / instanceKlass）和实例数据（oopDesc）



## Mark Word

要讲 synchronized 关键字，就不得不说到 Mark Word 这个东西

```pro
  32 bits:
  --------
  hash:25 ------------>| age:4    biased_lock:1 lock:2 (normal object)
  JavaThread*:23 epoch:2 age:4    biased_lock:1 lock:2 (biased object)
  size:32 ------------------------------------------>| (CMS free block)
  PromotedObject*:29 ---------->| promo_bits:3 ----->| (CMS promoted object)
 
  64 bits:
  --------
  unused:25 hash:31 -->| unused:1   age:4    biased_lock:1 lock:2 (normal object)
  JavaThread*:54 epoch:2 unused:1   age:4    biased_lock:1 lock:2 (biased object)
  PromotedObject*:61 --------------------->| promo_bits:3 ----->| (CMS promoted object)
  size:64 ----------------------------------------------------->| (CMS free block)
 
  unused:25 hash:31 -->| cms_free:1 age:4    biased_lock:1 lock:2 (COOPs && normal object)
  JavaThread*:54 epoch:2 cms_free:1 age:4    biased_lock:1 lock:2 (COOPs && biased object)
  narrowOop:32 unused:24 cms_free:1 unused:4 promo_bits:3 ----->| (COOPs && CMS promoted object)
  unused:21 size:35 -->| cms_free:1 unused:7 ------------------>| (COOPs && CMS free block)
 [ptr             | 00]  locked             ptr points to real header on stack
 [header      | 0 | 01]  unlocked           regular object header
 [ptr             | 10]  monitor            inflated lock (header is wapped out)
 [ptr             | 11]  marked             used by markSweep to mark an object
                                              
```



## 指针压缩

指针压缩是指的对象头的指针压缩，而 Mark Word 是对象头的一部分

* 32 位 JDK
  * 存放 Klass Pointer 大小是 4 bytes，Mark Word 是 4 bytes，对象头为 8 bytes，如果是数组，数组也会占用 4 bytes
* 64 位 JDK
  * 未开启指针压缩
    * 存放 Klass Pointer 大小是 8 bytes，Mark Word 是 8 bytes，但 java 内存地址按照 8 bytes 对齐，因此会从 12 bytes 补齐到 16 bytes，但是此时用来对齐的 bytes 已经不能再使用，如果有数组，数组又会占用 4 bytes
  * 开启指针压缩（-Xmx 小于 32G 的时候默认开启`-Xmx32G`）
    * 存放 Klass Pointer 大小是 4 bytes，Mark Word 是 8 bytes，如果有数组，数组又会占用 4 bytes



## 大小端

* 大端模式：是指数据的高字节保存在内存的低地址中，而数据的低字节保存在内存的高地址中，这样的存储模式有点儿类似于把数据当作字符串顺序处理：地址由小向大增加，而数据从高位往低位放；这和我们的阅读习惯一致
* 小端模式：是指数据的高字节保存在内存的高地址中，而数据的低字节保存在内存的低地址中，这种存储模式将地址的高低和数据位权有效地结合起来，高地址部分权值高，低地址部分权值低

> 摘自百度百科--[大小端模式](https://baike.baidu.com/item/%E5%A4%A7%E5%B0%8F%E7%AB%AF%E6%A8%A1%E5%BC%8F/6750542?fr=aladdin)

*PS: 大小端的单位字节是 byte，一个 byte 为 8 个 bit*



## 字节码

```java
public class SyncDemo {
    public void syncBlock(){
        synchronized (SyncDemo.class){
            System.out.println("hello block");
        }
    }
    public synchronized void syncMethod(){
        System.out.println("hello method");
    }
}

-------------------------------------------------------------------------------------
{
  public com.yczuoxin.demo.sync.SyncDemo();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 3: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   Lcom/yczuoxin/demo/sync/SyncDemo;

  public void syncBlock();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=3, args_size=1
         0: ldc           #2                  // class com/yczuoxin/demo/sync/SyncDemo
         2: dup
         3: astore_1
         4: monitorenter
         5: getstatic     #3                  // Field java/lang/System.out:Ljava/io/PrintStream;
         8: ldc           #4                  // String hello block
        10: invokevirtual #5                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        13: aload_1
        14: monitorexit
        15: goto          23
        18: astore_2
        19: aload_1
        20: monitorexit
        21: aload_2
        22: athrow
        23: return
      Exception table:
         from    to  target type
             5    15    18   any
            18    21    18   any
      LineNumberTable:
        line 5: 0
        line 6: 5
        line 7: 13
        line 8: 23
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      24     0  this   Lcom/yczuoxin/demo/sync/SyncDemo;
      StackMapTable: number_of_entries = 2
        frame_type = 255 /* full_frame */
          offset_delta = 18
          locals = [ class com/yczuoxin/demo/sync/SyncDemo, class java/lang/Object ]
          stack = [ class java/lang/Throwable ]
        frame_type = 250 /* chop */
          offset_delta = 4

  public synchronized void syncMethod();
    descriptor: ()V
    flags: ACC_PUBLIC, ACC_SYNCHRONIZED
    Code:
      stack=2, locals=1, args_size=1
         0: getstatic     #3                  // Field java/lang/System.out:Ljava/io/PrintStream;
         3: ldc           #6                  // String hello method
         5: invokevirtual #5                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
         8: return
      LineNumberTable:
        line 10: 0
        line 11: 8
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       9     0  this   Lcom/yczuoxin/demo/sync/SyncDemo;
}
```

可以看见 Synchronized 关键字对应的有一对匹配的 monitorenter 和 monitorexit 操作指令，这个是虚拟机协助生成的，功能是 monitor 的上锁和解锁操作，并且在抛出异常的时候也会帮助解锁