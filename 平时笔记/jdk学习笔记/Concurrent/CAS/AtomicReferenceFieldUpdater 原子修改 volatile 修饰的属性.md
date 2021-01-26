# AtomicReferenceFieldUpdater 原子修改 volatile 修饰的字段

## doc 描述概述

* 基于反射
* 指定类
* 指定 volatile 修饰的字段（必须对于 AtomicReferenceFieldUpdater 可访问）



AtomicReferenceFieldUpdater 有内部的实现类 AtomicReferenceFieldUpdaterImpl，利用静态方法创建 AtomicReferenceFieldUpdaterImpl 实例 `java.util.concurrent.atomic.AtomicReferenceFieldUpdater#newUpdater` 



## API

```java
// 指定对象如果指定字与期望值相同时，原子更新指定字段的值
public final boolean compareAndSet(T obj, V expect, V update);
// 将指定对象的指定字段设置为指定值，如果被 volatile 修饰，则会使主内存的数据失效
public final void set(T obj, V newValue);
// 指定对象如果指定字与期望值相同时，原子更新指定字段的值（在 jdk 8 中与 compareAndSet 效果相同）
public abstract boolean weakCompareAndSet(T obj, V expect, V update);
// 将指定对象的指定字段更新为指定值，但是不保证 volatile 的性质（立即使主内存内的数据失效）
public final void lazySet(T obj, V newValue);
// 获取对象字段的当前值
public final V get(T obj);
// 获取对象字段的当前值，再原子更新指定对象字段的值为 newValue
public V getAndSet(T obj, V newValue);
// 原子更新指定对象的值，调用 UnaryOperator 的 apply 方法更新值，并返回更新后的值
public final V updateAndGet(T obj, UnaryOperator<V> updateFunction);
// 获取对象字段的当前值，通过指定值和当前值计算获取新值，并原子更新对象的字段
public final V getAndAccumulate(T obj, V x,BinaryOperator<V> accumulatorFunction);
// 通过指定值和当前值计算获取新值，并原子更新对象的字段，返回更新后的值
public final V accumulateAndGet(T obj, V x,BinaryOperator<V> accumulatorFunction);
```

