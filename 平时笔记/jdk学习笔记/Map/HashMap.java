/*
package com.yczuoxin;

import jdk.internal.misc.SharedSecrets;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class HashMap<K,V> extends AbstractMap<K,V>
        implements Map<K,V>, Cloneable, Serializable {

    */
/**
     *  HashMap的默认初始容量为 1 左移 4 位（16），取余(%)操作中如果除数是2的幂次方，则等同于与其除数减一的与(&)操作
     *//*

    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;

    */
/**
     *  HashMap的最大容量为 1 左移 30 位（1073741824）
     *//*

    static final int MAXIMUM_CAPACITY = 1 << 30;

    */
/**
     *  HashMap 默认的加载因子 0.75
     *//*

    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    */
/**
     *  链转树的阈值（是 2 的倍数且最小值为 8）
     *//*

    static final int TREEIFY_THRESHOLD = 8;

    */
/**
     *  树转链的阈值
     *//*



    static final int UNTREEIFY_THRESHOLD = 6;

    */
/**
     *  最小树容量（最小值为 4 * TREEIFY_THRESHOLD）
     *//*

    static final int MIN_TREEIFY_CAPACITY = 64;

    */
/**
     *  链式列表的节点静态内部类
     *//*

    static class Node<K,V> implements Map.Entry<K,V> {

        final int hash; //hash值
        final K key;    //节点的键
        V value;        //节点的值
        Node<K,V> next; //下一个节点的地址

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public final K getKey(){
            return key;
        }

        @Override
        public final V getValue(){
            return value;
        }

        @Override
        public final String toString() {
            return key + "=" + value;
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        @Override
        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        public final boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                        Objects.equals(value, e.getValue())){
                    return true;
                }
            }
            return false;
        }
    }

    */
/**
     *  获取 key 值的 hash 值
     *  如果 key 值为 null，hash 值为0
     *  如果 key 值不为 null，hash 值为 key 的 hash 值在于其无符号右移 16 位取异，高位补 0 -> 将高位 16 位值置为 1
     *  该操作与其计算在 table 中的下标有关系
     *//*

    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    */
/**
     *  如果x的类型X直接实现了 Comparable<X> 接口（必须是其本身），返回x运行时类型，否则返回null
     *//*

    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c; Type[] ts, as; ParameterizedType p;
            if ((c = x.getClass()) == String.class){ // 如果x是字符串类型
                return c; // 返回String.class
            }

            // 返回的是该对象的运行时类型直接实现的接口,继承的不算
            if ((ts = c.getGenericInterfaces()) != null) {
                for (Type t : ts) {
                    // ParameterizedType 是 type 的子接口，表示参数化的类型，即实现了泛型参数的类型
                    if ((t instanceof ParameterizedType) &&
                            // getRawType() -> 返回声明了这个类型的类或接口，也就是去掉了泛型参数部分的类型对象
                            ((p = (ParameterizedType) t).getRawType() ==
                                    Comparable.class) &&
                            // getActualTypeArguments() -> 以数组的形式返回泛型参数列表
                            (as = p.getActualTypeArguments()) != null &&
                            as.length == 1 && as[0] == c){
                        return c; // type arg is c
                    }
                }
            }
        }
        return null;
    }

    */
/**
     *  如果 x 所属的类型是 kc，返回 k.compareTo(x) 的比较结果
     *  如果 x 是空或者所属类不是 kc，返回0
     *//*

    @SuppressWarnings({"rawtypes","unchecked"}) // for cast to Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable)k).compareTo(x));
    }

    */
/**
     *  找到大于等于initialCapacity的最小的2的幂
     *//*

    static final int tableSizeFor(int cap) {
        // Integer.numberOfLeadingZeros 获取传入值前面有多少个0占位
        // n为最高位与传入值最高位为1相同的位置为1的最大值
        // 如果 cap 二进制为 0000 0000 0000 0000 0000 0000 0010 0100
        // 此时 n 的二进制为 0000 0000 0000 0000 0000 0000 0011 1111
        int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
        // 如果 n 值小于 0，返回 1
        // 如果 n 值大于等于 int 最大值，返回 int 最大值
        // 如果 n 值介于 0 与 int 值之间，返回 n + 1，即大于等于传入值的第一个 2 的指数幂
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    */
/**
     *  当表第一次使用的时候初始化
     *//*

    transient Node<K,V>[] table;

    */
/**
     *  缓存，记录 map 中用过的 keySet 和 values
     *//*

    transient Set<Entry<K,V>> entrySet;

    */
/**
     *  key-value 映射的数量
     *//*

    transient int size;

    */
/**
     *  HashMap 结构修改的次数，用于快速失败的校验，当用于迭代器遍历的过程中会校验这个值
     *  如果该值在遍历的时候发生变化，就会抛出 ConcurrentModificationException 异常
     *//*

    transient int modCount;

    */
/**
     *  要调整大小的下一个大小值 (capacity * load factor)
     *//*

    int threshold;

    */
/**
     *  加载因子
     *//*

    final float loadFactor;

    */
/**
     *  HashMap的初始化方法
     * @param initialCapacity 初始化容量
     * @param loadFactor 加载因子
     *//*

    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " +
                    loadFactor);
        }
        // 初始化加载因子
        this.loadFactor = loadFactor;
        // 初始化 table 容量
        this.threshold = tableSizeFor(initialCapacity);
    }

    */
/**
     *  只传入容量的初始化方法
     * @param initialCapacity 初始化容量
     *//*

    public HashMap(int initialCapacity) {
        // 加载因子为默认值 0.75f
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    */
/**
     *  无参构造，加载因子为 0.75f
     *//*

    public HashMap() {
        // 所有值都是默认值
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }

    */
/**
     *  构造一个新的足够装下参数 m 的 HashMap，加载因子为 0.75f
     * @param m 初始化时放入 map 的映射
     *//*

    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        // 将映射放入 map
        putMapEntries(m, false);
    }

    */
/**
     *  将映射放入 map
     * @param m 放入 map 的映射
     * @param evict 如果为false，该 map 属于创建模式
     *//*

    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        // 映射的数量
        int s = m.size();
        if (s > 0) {
            // 如果映射表还没有初始化
            if (table == null) {
                //
                float ft = ((float)s / loadFactor) + 1.0F;
                // 判断考虑到加载因子后所需要的容量
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                        // 向下取整强转
                        (int)ft : MAXIMUM_CAPACITY);
                // 所需要的容量如果大于阈值
                if (t > threshold) {
                    // 重新找到合适的容量
                    threshold = tableSizeFor(t);
                }
            }
            // 映射表已经初始化并且映射的数量大于映射表的容量
            else if (s > threshold) {
                // 对映射表进行扩容
                resize();
            }
            // 遍历传入的 map
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                // 对映射表塞值
                putVal(hash(key), key, value, false, evict);
            }
        }
    }

    */
/**
     *  获取 key-value 映射中映射的数量
     *//*

    @Override
    public int size() {
        return size;
    }

    */
/**
     *  判断 key-value 映射中映射的数量是否为0
     *//*

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    */
/**
     *  根据 key 值查找对应的 value
     * @param key   需要查询的 key 值
     * @return  查询到 key 对应的 value，如果没有找到，则返回 null
     *//*

    @Override
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    */
/**
     *  实现 map 的 get()
     * @param hash  对于 key 值进行 hash() 后的值
     * @param key   需要取出的 value 对应的 key 值
     * @return  查询到的节点，如果没有则返回 null
     *//*

    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        // 如果映射表已经初始化并且映射的数量大于0
        if ((tab = table) != null && (n = tab.length) > 0 &&
                // 直接计算出对应的下标（n - 1）& hash，缩小查询范围，如果存在结果，则必定在 table 的这个位置上
                (first = tab[(n - 1) & hash]) != null) {
            // 判断第一个存在的节点的 key 是否和查询的 key 相等。如果相等，直接返回该节点
            // 判断相等的方式是其 hash 值相等并且 equals() 也相等
            if (first.hash == hash &&
                    ((k = first.key) == key || (key != null && key.equals(k)))) {
                return first;
            }
            // 遍历该链表/红黑树直到 next 为 null
            if ((e = first.next) != null) {
                if (first instanceof TreeNode) {
                    return ((TreeNode<K,V>) first).getTreeNode(hash, key);
                }
                do {
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        return e;
                    }
                } while ((e = e.next) != null);
            }
        }
        return null;
    }

    */
/**
     *  判断映射表中是否包含 key 值
     * @param key   需要判断的 key 值
     * @return  是否有 key 值
     *//*

    @Override
    public boolean containsKey(Object key) {
        return getNode(hash(key), key) != null;
    }

    */
/**
     *  存放映射的方法
     * @param key   需要存放的 key 值
     * @param value 需要存放的 value 值
     * @return  存放前的 value 值
     *//*

    @Override
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    */
/**
     *  真正存放映射的方法
     * @param hash  对于 key 进行 hash() 后的值
     * @param key   需要存放的 key 值
     * @param value 需要存放的 value 值
     * @param onlyIfAbsent  如果是 true，不要改变现有的值
     * @param evict 如果为false，该 map 属于创建模式
     * @return  更改前的值
     *//*

    final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab; Node<K,V> p; int n, i;
        // 如果映射表还没有初始化或者映射表中映射的数量为0
        if ((tab = table) == null || (n = tab.length) == 0) {
            // 初始化映射表或者扩容
            n = (tab = resize()).length;
        }
        // 如果映射表中没有没有发生 hash 碰撞
        if ((p = tab[i = (n - 1) & hash]) == null) {
            // 直接放入在 (n - 1) & hash 的位置
            tab[i] = newNode(hash, key, value, null);
        }
        // 如果放生了 hash 碰撞
        else {
            Node<K,V> e; K k;
            // 如果该 key 值已经存在
            if (p.hash == hash &&
                    ((k = p.key) == key || (key != null && key.equals(k)))) {
                // 把节点置换为新的节点
                e = p;
            }
            // 如果此时表结构是红黑树
            else if (p instanceof TreeNode) {
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            }
            // 如果此时表结构是链表
            else {
                for (int binCount = 0; ; ++binCount) {
                    // 如果下个节点是空
                    if ((e = p.next) == null) {
                        // 则将值放入到下个节点
                        p.next = newNode(hash, key, value, null);
                        // 如果此时链表长度已经大于等于链表转红黑树的阈值
                        // -1是因为binCount初始值为0
                        if (binCount >= TREEIFY_THRESHOLD - 1) {
                            // 链表转红黑树
                            treeifyBin(tab, hash);
                        }
                        // 跳出循环
                        break;
                    }
                    // 如果下个节点的 key 值与要存的 key 值相同
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        // 跳出循环
                        break;
                    }
                    p = e;
                }
            }
            // 如果 key 值在之前的映射表中已经存在
            if (e != null) {
                // 提取出之前的 key 值
                V oldValue = e.value;
                // 如果可以改变之前的值或者之前的值为null
                if (!onlyIfAbsent || oldValue == null) {
                    // 将值替换掉
                    e.value = value;
                }
                // 什么都没做，这里是为linkHashMap留的，该方法是将节点放到链表的最后
                afterNodeAccess(e);
                return oldValue;
            }
        }
        // 修改次数 +1，用于快速失败
        ++modCount;
        // 映射表的大小 +1，如果超过了阈值
        if (++size > threshold) {
            // 重新调整映射表的大小
            resize();
        }
        // 为linkHashMap留的，该方法是删除列表中存活时间最长的节点
        afterNodeInsertion(evict);
        return null;
    }

    */
/**
     *  重新调整映射表大小
     * @return 新的映射表
     *//*

    final Node<K,V>[] resize() {
        // 旧的映射表
        Node<K,V>[] oldTab = table;
        // 记录旧表的映射数量
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        // 记录旧表的容量阈值
        int oldThr = threshold;
        int newCap, newThr = 0;
        // 如果旧表有数据
        if (oldCap > 0) {
            // 如果旧表映射数量已经大于或等于最大容量了
            if (oldCap >= MAXIMUM_CAPACITY) {
                // 阈值设为int最大值
                threshold = Integer.MAX_VALUE;
                // 返回旧表
                return oldTab;
            }
            // 将旧表的容量 * 2设置给 newCap
            // 如果 newCap 小于最大容量并且旧表容量大于默认初始容量
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    oldCap >= DEFAULT_INITIAL_CAPACITY) {
                // 设置新的阈值为旧阈值的两倍
                newThr = oldThr << 1;
            }
        }
        // 如果旧表没有数据，但是阈值已经设置了
        else if (oldThr > 0) {
            // 把旧表的阈值设置给 newCap
            newCap = oldThr;
        }
        // 映射表还未初始化，并且使用默认值给映射表
        else {
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }
        // 新的阈值还没有初始化，看起来应该是在 -> 如果旧表没有数据，但是阈值已经设置了
        // 情况下发生的
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                    (int)ft : Integer.MAX_VALUE);
        }
        // 设置新的阈值
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        // 新建一个数组
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            // 遍历旧的映射表
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                // 如果当前节点不为空，则把当前节点的元信息传递给 e
                if ((e = oldTab[j]) != null) {
                    // 把当前节点设置为 null 方便回收
                    oldTab[j] = null;
                    // 如果该节点为最后一个节点
                    if (e.next == null) {
                        //
                        newTab[e.hash & (newCap - 1)] = e;
                    }
                    // 如果该节点不是最后一个节点且节点是树节点
                    else if (e instanceof TreeNode) {
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    }
                    // 如果该节点不是最后一个节点且节点是链表
                    else { // preserve order
                        // 进行链表复制
                        // loHead用于存储低位（位置不变）key的链头，loTail用于指向链位位置
                        Node<K,V> loHead = null, loTail = null;
                        // hiHead用于存储高位（位置改变）key的链头，hiTail用于指向链位位置
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            // 将下个节点信息存在 next 局部变量中
                            next = e.next;
                            // 元素位置在扩容后数组中的位置是否需要发生改变
                            // 等于0，不需要改变
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null) {
                                    loHead = e;
                                }
                                else {
                                    loTail.next = e;
                                }
                                loTail = e;
                            }
                            // 位置需要改变的
                            else {
                                if (hiTail == null) {
                                    hiHead = e;
                                }
                                else {
                                    hiTail.next = e;
                                }
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        // 再加上原链表的长度即为新的位置
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }

    */
/**
     *  将链表换成红黑树
     * @param tab
     * @param hash
     *//*

    final void treeifyBin(Node<K,V>[] tab, int hash) {
        int n, index; Node<K,V> e;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY) {
            resize();
        }
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K,V> hd = null, tl = null;
            do {
                TreeNode<K,V> p = replacementTreeNode(e, null);
                if (tl == null) {
                    hd = p;
                }
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);
            if ((tab[index] = hd) != null) {
                hd.treeify(tab);
            }
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        putMapEntries(m, true);
    }

    */
/**
     *  移除节点的操作，如果有则返回 key 对应的 value，否则返回 null，也可能存入值就是 null，所以如果返回值为 null还需用 containsKey()
     * @param key   需要删掉的键
     * @return  删掉的值
     *//*

    @Override
    public V remove(Object key) {
        Node<K,V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
                null : e.value;
    }

    */
/**
     *  真正移除节点的方法
     * @param hash  节点的 hash 值
     * @param key   节点的 key 值
     * @param value 节点的 value 值
     * @param matchValue    如果为 true，则当 key 对应的 value 值 equals(value) 时删除；否则不关心 value 值
     * @param movable   删除后是否移动节点
     * @return  被移除的节点
     *//*

    final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {
        Node<K,V>[] tab; Node<K,V> p; int n, index;
        // 如果映射表不为空且映射表的数量不为0并且根据 hash 值定位的节点不为空
        if ((tab = table) != null && (n = tab.length) > 0 &&
                (p = tab[index = (n - 1) & hash]) != null) {
            Node<K,V> node = null, e; K k; V v;
            // 如果当前节点的键和 key 相等，那么当前节点就是要删除的节点，赋值给node
            if (p.hash == hash &&
                    ((k = p.key) == key || (key != null && key.equals(k)))) {
                node = p;
            }
            // 否则首节点的键没有与 key 值匹配，判断是否还有发生过hash碰撞，有下一个节点
            else if ((e = p.next) != null) {
                // 如果是红黑树
                if (p instanceof TreeNode) {
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                }
                // 如果是链表结构
                else {
                    do {
                        if (e.hash == hash &&
                                ((k = e.key) == key ||
                                        (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            // node 不为空即匹配上了节点并且
            // 没有设置节点值需要匹配或者节点值与需要匹配的节点值相等或者
            // value 不为空并且 value 值与需要匹配的值 equals
            // 那么就删除该节点
            if (node != null && (!matchValue || (v = node.value) == value ||
                    (value != null && value.equals(v)))) {
                // 如果是树节点
                if (node instanceof TreeNode) {
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                }
                // 如果node 就是首节点
                else if (node == p) {
                    tab[index] = node.next;
                }
                else {
                    p.next = node.next;
                }
                ++modCount;
                --size;
                // 空出来给LinkedHashMap使用的方法
                afterNodeRemoval(node);
                return node;
            }
        }
        // 没有匹配上，返回null
        return null;
    }

    */
/**
     *  清空映射表
     *//*

    @Override
    public void clear() {
        Node<K,V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i) {
                tab[i] = null;
            }
        }
    }

    */
/**
     *  判断映射表是否有与 value 相同的值
     * @param value 查询的 value 值
     * @return 是否有 value 值
     *//*

    @Override
    public boolean containsValue(Object value) {
        Node<K,V>[] tab; V v;
        if ((tab = table) != null && size > 0) {
            // 循环遍历所有的节点
            for (Node<K,V> e : tab) {
                // 如同 while(e = e.next != null)
                for (; e != null; e = e.next) {
                    if ((v = e.value) == value ||
                            (value != null && value.equals(v))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    */
/**
     *  返回映射表中所有的 key
     * @return  映射表中所有的 key
     *//*

    @Override
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    */
/**
     *  映射表所有的 keySet 的方法
     *//*

    final class KeySet extends AbstractSet<K> {
        @Override
        public final int size()                 { return size; }
        @Override
        public final void clear()               { HashMap.this.clear(); }
        @Override
        public final Iterator<K> iterator()     { return new KeyIterator(); }
        @Override
        public final boolean contains(Object o) { return containsKey(o); }
        @Override
        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }
        @Override
        public final Spliterator<K> spliterator() {
            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        @Override
        public final void forEach(Consumer<? super K> action) {
            Node<K,V>[] tab;
            if (action == null) {
                throw new NullPointerException();
            }
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (Node<K,V> e : tab) {
                    for (; e != null; e = e.next) {
                        action.accept(e.key);
                    }
                }
                if (modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    */
/**
     *  映射表所有的 value 的集合
     * @return  映射表所有的 value
     *//*

    @Override
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    */
/**
     *  映射表所有的 values 的方法
     *//*

    final class Values extends AbstractCollection<V> {
        @Override
        public final int size()                 { return size; }
        @Override
        public final void clear()               { HashMap.this.clear(); }
        @Override
        public final Iterator<V> iterator()     { return new ValueIterator(); }
        @Override
        public final boolean contains(Object o) { return containsValue(o); }
        @Override
        public final Spliterator<V> spliterator() {
            return new ValueSpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        @Override
        public final void forEach(Consumer<? super V> action) {
            // forEach 遍历方法
            Node<K,V>[] tab;
            if (action == null) {
                throw new NullPointerException();
            }
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (Node<K,V> e : tab) {
                    for (; e != null; e = e.next) {
                        action.accept(e.value);
                    }
                }
                if (modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    */
/**
     *  键值对的集合
     * @return 键值对
     *//*

    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
    }

    */
/**
     *  键值对集合的方法
     *//*

    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        @Override
        public final int size()                 { return size; }
        @Override
        public final void clear()               { HashMap.this.clear(); }
        @Override
        public final Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }
        @Override
        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
            Object key = e.getKey();
            Node<K,V> candidate = getNode(hash(key), key);
            return candidate != null && candidate.equals(e);
        }
        @Override
        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return removeNode(hash(key), key, value, true, true) != null;
            }
            return false;
        }
        @Override
        public final Spliterator<Map.Entry<K,V>> spliterator() {
            return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        @Override
        public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
            Node<K,V>[] tab;
            if (action == null) {
                throw new NullPointerException();
            }
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (Node<K,V> e : tab) {
                    for (; e != null; e = e.next) {
                        action.accept(e);
                    }
                }
                if (modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    */
/**
     *  根据 key 值获取 value 值，如果没有对应的 key 值就返回默认值
     * @param key   键
     * @param defaultValue  默认值
     * @return  value 值或者默认值
     *//*

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
    }

    */
/**
     *  如果没有改 key 值就插入，否则不改之前的值
     * @param key   键
     * @param value 值
     * @return  存入前的值
     *//*

    @Override
    public V putIfAbsent(K key, V value) {
        return putVal(hash(key), key, value, true, true);
    }

    */
/**
     *  删除匹配的节点，需要 key 和 value 都匹配才能删除
     * @param key   需要删除节点的 key
     * @param value 需要删除节点的 key
     * @return  是否删除成功
     *//*

    @Override
    public boolean remove(Object key, Object value) {
        return removeNode(hash(key), key, value, true, true) != null;
    }

    */
/**
     *  替换映射表中的键为 key 并且值为 oldValue 的值改为 newValue
     * @param key   需要更改的键
     * @param oldValue  需要更改的值
     * @param newValue  更改后的值
     * @return  是否发生变更
     *//*

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Node<K,V> e; V v;
        if ((e = getNode(hash(key), key)) != null &&
                ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
            e.value = newValue;
            afterNodeAccess(e);
            return true;
        }
        return false;
    }

    */
/**
     *  替换映射表中的键为 key 的键值对中的值为 value
     * @param key   需要更改的键
     * @param value 更改后的值
     * @return  变更前的值
     *//*

    @Override
    public V replace(K key, V value) {
        Node<K,V> e;
        if ((e = getNode(hash(key), key)) != null) {
            V oldValue = e.value;
            e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
        return null;
    }

    */
/**
     *  如果键值不存在 key，对 key 进行计算操作
     * @param key   需要查询的 key 值
     * @param mappingFunction   计算方法
     * @return  如果有值则返回 key 值对应的 value，否则返回经过计算后的 key 值，并存入map中
     *//*

    @Override
    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        if (mappingFunction == null) {
            throw new NullPointerException();
        }
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        // 如果键值对的数量大于阈值或者映射表还没有初始化或者键值对的数量为0
        if (size > threshold || (tab = table) == null ||
                (n = tab.length) == 0) {
            // 扩容
            n = (tab = resize()).length;
        }
        // 如果首节点不为空
        if ((first = tab[i = (n - 1) & hash]) != null) {
            // 如果是树节点
            if (first instanceof TreeNode) {
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            }
            // 如果是链表结构
            else {
                Node<K,V> e = first; K k;
                do {
                    // 如果 key 匹配上了
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    // 否则 binCount自增1
                    ++binCount;
                } while ((e = e.next) != null);
            }
            V oldValue;
            // 如果有对应传入 key 值的键并且对应的值不为空
            if (old != null && (oldValue = old.value) != null) {
                afterNodeAccess(old);
                // 直接返回保存的值
                return oldValue;
            }
        }
        // 此方法在对于 key 进行计算时存在快速失败的机制
        int mc = modCount;
        V v = mappingFunction.apply(key);
        if (mc != modCount) { throw new ConcurrentModificationException(); }
        if (v == null) {
            return null;
        }
        // 如果节点存在，但是其 value 为空
        else if (old != null) {
            old.value = v;
            afterNodeAccess(old);
            return v;
        }
        // 保存 key 和 value
        // 对于树结构
        else if (t != null) {
            t.putTreeVal(this, tab, hash, key, v);
        }
        // 对于链表结构
        else {
            tab[i] = newNode(hash, key, v, first);
            if (binCount >= TREEIFY_THRESHOLD - 1) {
                treeifyBin(tab, hash);
            }
        }
        // 为何不用 ++modCount
        modCount = mc + 1;
        ++size;
        afterNodeInsertion(true);
        return v;
    }

    */
/**
     *  键值对中存在对应的 key 值，则对 key 和 value 分别进行计算，并储存为 value
     * @param key   需要匹配的 key
     * @param remappingFunction 计算公式
     * @return  计算后得到的值
     *//*

    @Override
    public V computeIfPresent(K key,
                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null) {
            throw new NullPointerException();
        }
        Node<K,V> e; V oldValue;
        int hash = hash(key);
        // 需要键和值都不为空
        if ((e = getNode(hash, key)) != null &&
                (oldValue = e.value) != null) {
            int mc = modCount;
            V v = remappingFunction.apply(key, oldValue);
            if (mc != modCount) { throw new ConcurrentModificationException(); }
            // 如果计算后的值不为空
            if (v != null) {
                // 把值设置给节点
                e.value = v;
                afterNodeAccess(e);
                return v;
            }
            // 如果计算结果为空，则删除这个节点
            else {
                removeNode(hash, key, null, false, true);
            }
        }
        return null;
    }

    */
/**
     *  当找到与 key 对应的键时
     *      如果计算出的 value 为空，则删除节点
     *      不为空则置换节点
     *  没有对应的键时
     *      如果 value 为空，直接返回 null
     *      不为空则增加节点
     * @param key   匹配键值
     * @param remappingFunction 对 key 和 value 的计算方法
     * @return  返回计算后的 value
     *//*

    @Override
    public V compute(K key,
                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null) {
            throw new NullPointerException();
        }
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        // 是否需要扩容
        if (size > threshold || (tab = table) == null ||
                (n = tab.length) == 0) {
            n = (tab = resize()).length;
        }
        // 寻找对应键值为 key 的节点
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode) {
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            }
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        // 如果没有找到键值为 key 的节点就设置 null 值
        // 如果找到则拿到对应的 value 值
        V oldValue = (old == null) ? null : old.value;
        int mc = modCount;
        V v = remappingFunction.apply(key, oldValue);
        if (mc != modCount) { throw new ConcurrentModificationException(); }
        // 找到键值为 key 的节点
        if (old != null) {
            // 计算的结果不为空
            if (v != null) {
                // 置换结果值
                old.value = v;
                afterNodeAccess(old);
            }
            // 计算的结果为空
            else {
                // 删除节点
                removeNode(hash, key, null, false, true);
            }
        }
        // 没有找到键值为 key 的节点
        // 计算的结果不为空
        else if (v != null) {
            // 如果是树节点
            if (t != null) {
                t.putTreeVal(this, tab, hash, key, v);
            }
            // 如果是链表节点
            else {
                // 插入节点
                tab[i] = newNode(hash, key, v, first);
                if (binCount >= TREEIFY_THRESHOLD - 1) {
                    treeifyBin(tab, hash);
                }
            }
            modCount = mc + 1;
            ++size;
            afterNodeInsertion(true);
        }
        return v;
    }

    */
/**
     *  当找到与 key 对应的键时
     *      如果 value 为空
     *          直接替换为 value
     *      如果 value 不为空
     *          将原有值和传入值进行计算并设为 value
     *      如果传入的 value 或者 计算出来的 value 为空
     *          删除该节点
     *  当没有找到与 key 对应的键时
     *      如果传入的 value 不为空
     *          插入节点
     *      如果传入的 value 也为空
     *          就不作任何处理
     * @param key   需要匹配的键
     * @param value 带入计算的 value
     * @param remappingFunction 计算的方法
     * @return  计算后的结果
     *//*

    @Override
    public V merge(K key, V value,
                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null) {
            throw new NullPointerException();
        }
        if (remappingFunction == null) {
            throw new NullPointerException();
        }
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        // 是否需要扩容
        if (size > threshold || (tab = table) == null ||
                (n = tab.length) == 0) {
            n = (tab = resize()).length;
        }
        // 寻找对应键值为 key 的节点
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode) {
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            }
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        // 找到匹配的节点
        if (old != null) {
            V v;
            // 原来的值不是空
            if (old.value != null) {
                int mc = modCount;
                // 将原有值和传入值进行计算
                v = remappingFunction.apply(old.value, value);
                if (mc != modCount) {
                    throw new ConcurrentModificationException();
                }
            }
            // 原来的值是空
            else {
                // 直接替换为 value
                v = value;
            }
            // 计算的结果或者传入的 value 不为空
            if (v != null) {
                // 置换节点
                old.value = v;
                afterNodeAccess(old);
            }
            // 计算的结果或者传入的 value 为空
            else {
                // 删除节点
                removeNode(hash, key, null, false, true);
            }
            return v;
        }
        // 如果传入的 value 不是空
        if (value != null) {
            // 如果是树节点
            if (t != null) {
                t.putTreeVal(this, tab, hash, key, value);
            }
            // 如果是链表节点
            else {
                // 插入节点
                tab[i] = newNode(hash, key, value, first);
                if (binCount >= TREEIFY_THRESHOLD - 1) {
                    treeifyBin(tab, hash);
                }
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return value;
    }

    */
/**
     *  遍历 HashMap
     * @param action    需要遍历的处理方式
     *//*

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Node<K,V>[] tab;
        if (action == null) {
            throw new NullPointerException();
        }
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (Node<K,V> e : tab) {
                for (; e != null; e = e.next) {
                    action.accept(e.key, e.value);
                }
            }
            if (modCount != mc) {
                throw new ConcurrentModificationException();
            }
        }
    }

    */
/**
     *  置换所有满足条件的节点的 value 值
     * @param function
     *//*

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Node<K,V>[] tab;
        if (function == null) {
            throw new NullPointerException();
        }
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (Node<K,V> e : tab) {
                for (; e != null; e = e.next) {
                    // 将满足条件的节点的 key 和 value 值进行计算，赋予 value
                    e.value = function.apply(e.key, e.value);
                }
            }
            if (modCount != mc) {
                throw new ConcurrentModificationException();
            }
        }
    }

    */
/**
     *  克隆一个浅复制的副本，不会对其本体有任何的干扰
     * @return  一个浅复制的副本
     *//*

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        HashMap<K,V> result;
        try {
            result = (HashMap<K,V>)super.clone();
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们是克隆的
            throw new InternalError(e);
        }
        // 初始化一个新的映射表
        result.reinitialize();
        // 将此映射表进行复制给新的映射表
        result.putMapEntries(this, false);
        return result;
    }

    */
/**
     *  获取加载因子
     * @return  返回加载因子
     *//*

    final float loadFactor() { return loadFactor; }

    */
/**
     *  获取映射表容量
     * @return  返回映射表容量
     *//*

    final int capacity() {
        return (table != null) ? table.length :
                (threshold > 0) ? threshold :
                        DEFAULT_INITIAL_CAPACITY;
    }

    */
/**
     *  写出阈值，loadFactor和任何隐藏的东西
     * @param s
     * @throws IOException
     *//*

    private void writeObject(java.io.ObjectOutputStream s)
            throws IOException {
        int buckets = capacity();
        s.defaultWriteObject();
        s.writeInt(buckets);
        s.writeInt(size);
        internalWriteEntries(s);
    }

    */
/**
     *  读入阈值（忽略），loadFactor和任何隐藏的东西
     * @param s
     * @throws IOException
     * @throws ClassNotFoundException
     *//*

    private void readObject(java.io.ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        reinitialize();
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new InvalidObjectException("Illegal load factor: " +
                    loadFactor);
        }
        // 读取并忽略桶数
        s.readInt();
        // 读取映射数（大小）
        int mappings = s.readInt();
        if (mappings < 0) {
            throw new InvalidObjectException("Illegal mappings count: " +
                    mappings);
        }
        // 如果是0，就用默认值
        else if (mappings > 0) {
            // Size the table using given load factor only if within
            // range of 0.25...4.0
            float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
            float fc = (float)mappings / lf + 1.0f;
            int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                    DEFAULT_INITIAL_CAPACITY :
                    (fc >= MAXIMUM_CAPACITY) ?
                            MAXIMUM_CAPACITY :
                            tableSizeFor((int)fc));
            float ft = (float)cap * lf;
            threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                    (int)ft : Integer.MAX_VALUE);

            // Check Map.Entry[].class since it's the nearest public type to
            // what we're actually creating.
            SharedSecrets.getJavaObjectInputStreamAccess().checkArray(s, Map.Entry[].class, cap);
            @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] tab = (Node<K,V>[])new Node[cap];
            table = tab;

            // Read the keys and values, and put the mappings in the HashMap
            for (int i = 0; i < mappings; i++) {
                @SuppressWarnings("unchecked")
                K key = (K) s.readObject();
                @SuppressWarnings("unchecked")
                V value = (V) s.readObject();
                putVal(hash(key), key, value, false, false);
            }
        }
    }

    */
/**
     *  HashMap 的迭代器
     *//*

    abstract class HashIterator {
        // 要遍历的下一个元素
        Node<K,V> next;
        // 当前元素
        Node<K,V> current;
        // 快速失败验证值
        int expectedModCount;
        // 当前遍历的元素的桶索引
        int index;

        // 迭代器的构造初始化
        HashIterator() {
            expectedModCount = modCount;
            Node<K,V>[] t = table;
            current = next = null;
            index = 0;
            // 拿到第一个键值对
            if (t != null && size > 0) { // advance to first entry
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        // 是否有下一个键值对
        public final boolean hasNext() {
            return next != null;
        }

        // 获取下一个键值对
        final Node<K,V> nextNode() {
            Node<K,V>[] t;
            Node<K,V> e = next;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (e == null) {
                throw new NoSuchElementException();
            }
            // 预拿到下一个键值对放在 next
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        // 移除当前的元素
        public final void remove() {
            Node<K,V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            removeNode(p.hash, p.key, null, false, false);
            // 防止快速失败
            expectedModCount = modCount;
        }
    }

    */
/**
     *  键的迭代器
     *//*

    final class KeyIterator extends HashIterator
            implements Iterator<K> {
        // 针对性重写 next 方法
        @Override
        public final K next() { return nextNode().key; }
    }

    */
/**
     *  值的迭代器
     *//*

    final class ValueIterator extends HashIterator
            implements Iterator<V> {
        // 针对性重写 next 方法
        @Override
        public final V next() { return nextNode().value; }
    }

    */
/**
     *  键值对的迭代器
     *//*

    final class EntryIterator extends HashIterator
            implements Iterator<Map.Entry<K,V>> {
        // 针对性重写 next 方法
        @Override
        public final Map.Entry<K,V> next() { return nextNode(); }
    }

    */
/**
     *  HashMap 的并行迭代器
     * @param <K>
     * @param <V>
     *//*

    static class HashMapSpliterator<K,V> {
        // 需要遍历的 HashMap
        final HashMap<K,V> map;
        // 当前遍历的节点
        Node<K,V> current;
        // 当前迭代器开始遍历节点的桶索引（拆分前）
        int index;
        // 当前迭代器遍历节点的上限的桶索引（拆分前）
        int fence;
        // 需要遍历的元素个数（size）
        int est;
        // 快速失败的验证
        int expectedModCount;

        // 初始化构造函数
        HashMapSpliterator(HashMap<K,V> m, int origin,
                           int fence, int est,
                           int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        // 获取映射表中键值对的数量，是迭代器的迭代范围
        final int getFence() {
            int hi;
            if ((hi = fence) < 0) {
                HashMap<K,V> m = map;
                // 映射表的
                est = m.size;
                expectedModCount = m.modCount;
                Node<K,V>[] tab = m.table;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        // 获取当前迭代器要遍历的元素个数
        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }
    }

    //
    static final class KeySpliterator<K,V> extends HashMapSpliterator<K,V> implements Spliterator<K> {
        KeySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        // 对当前迭代器进行分割
        @Override
        public KeySpliterator<K,V> trySplit() {
            // 将当前迭代的索引除以二，获得中间索引
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    // 需要遍历的元素个数 est 也需要除以二
                    new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        */
/**
         *  在当前迭代器遍历范围遍历一遍
         * @param action    遍历的元素执行的操作
         *//*

        @Override
        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null) {
                throw new NullPointerException();
            }
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            // 需要遍历的桶上限小于0
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                // 重新初始化一次需要遍历的桶上限
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else {
                mc = expectedModCount;
            }
            // 如果 hashmap 桶不为空，且桶数大于等于要遍历的数量
            if (tab != null && tab.length >= hi &&
                    // 开始节点桶索引大于等于 0 并且遍历通索引上限大于开始节点桶索引或者当前节点不为空
                    // 说这么多就是可以进行遍历
                    (i = index) >= 0 && (i < (index = hi) || current != null)) {
                // 当前节点赋予 p
                Node<K,V> p = current;
                // 把当前节点值为 null
                current = null;
                do {
                    // 该桶内的节点都已经遍历完了
                    if (p == null) {
                        // 开始遍历下一个桶
                        p = tab[i++];
                    }
                    // 桶内节点还未遍历完全
                    else {
                        // 进行处理该节点
                        action.accept(p.key);
                        // 置换成下一个节点准备遍历
                        p = p.next;
                    }
                    // 如果下一个节点不为空或者遍历的索引还为达到要遍历桶索引的上限
                } while (p != null || i < hi);
                if (m.modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        */
/**
         *  会遍历迭代器遍历的范围之内的元素，当找到第一个非空元素的时并操作成功后返回 true，没找到节点返回 false
         * @param action    遍历的元素执行的操作
         * @return 当找到第一个非空元素的时并操作成功后返回 true，没找到节点返回 false
         *//*

        @Override
        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null) {
                throw new NullPointerException();
            }
            Node<K,V>[] tab = map.table;
            // 如果桶不为空并且桶的大小大于需要遍历的桶上限并且当前桶索引大于等于0
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                // 如果当前节点不为空且桶索引小于要遍历的桶上限
                while (current != null || index < hi) {
                    // 如果当前节点为空
                    if (current == null) {
                        // 开始遍历下一个桶
                        current = tab[index++];
                    }
                    // 如果当前节点不为空
                    else {
                        // 拿到当前的节点的键并设置为 k
                        K k = current.key;
                        // 将下一个节点设置为当前节点备用
                        current = current.next;
                        // 对当前节点进行处理
                        action.accept(k);
                        if (map.modCount != expectedModCount) {
                            throw new ConcurrentModificationException();
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        */
/**
         *
         * @return
         *//*

        @Override
        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                    Spliterator.DISTINCT;
        }
    }

    static final class ValueSpliterator<K,V>
            extends HashMapSpliterator<K,V>
            implements Spliterator<V> {
        ValueSpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new ValueSpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                    (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.value);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        V v = current.value;
                        current = current.next;
                        action.accept(v);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0);
        }
    }

    static final class EntrySpliterator<K,V>
            extends HashMapSpliterator<K,V>
            implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                    new EntrySpliterator<>(map, lo, index = mid, est >>>= 1,
                            expectedModCount);
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K,V>> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                    (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Node<K,V> e = current;
                        current = current.next;
                        action.accept(e);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                    Spliterator.DISTINCT;
        }
    }

    */
/**
     *  常规创建非树节点
     * @param hash  hash 值
     * @param key   键
     * @param value 值
     * @param next  下一个节点
     * @return  新的节点
     *//*

    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
        return new Node<>(hash, key, value, next);
    }

    */
/**
     *  用于从 TreeNodes 转换为链表节点
     * @param p 当前节点
     * @param next  下一个节点
     * @return  链表节点
     *//*

    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    */
/**
     *  常规创建一个树节点
     * @param hash  hash 值
     * @param key   键
     * @param value 值
     * @param next  下一个节点
     * @return  创建的新节点
     *//*

    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    */
/**
     *  将链表节点转换为树节点
     * @param p 树节点
     * @param next  下一个节点
     * @return  树节点
     *//*

    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    */
/**
     *  初始化值
     *//*

    void reinitialize() {
        table = null;
        entrySet = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }

    void afterNodeAccess(Node<K,V> p) { }
    void afterNodeInsertion(boolean evict) { }
    void afterNodeRemoval(Node<K,V> p) { }

    */
/**
     *
     * @param s
     * @throws IOException
     *//*

    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        Node<K,V>[] tab;
        if (size > 0 && (tab = table) != null) {
            for (Node<K,V> e : tab) {
                for (; e != null; e = e.next) {
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }
    }

    */
/**
     *  红黑树静态内部类,包内继承
     *//*

    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
        // 父节点
        TreeNode<K,V> parent;
        // 左节点
        TreeNode<K,V> left;
        // 右节点
        TreeNode<K,V> right;
        // 需要再删除后取消连接
        TreeNode<K,V> prev;
        // 红黑标志
        boolean red;
        TreeNode(int hash, K key, V val, Node<K,V> next) {
            // 调用 LinkedHashMap.Entry 的构造方法，由于 LinkedHashMap.Entry 没有被 final 修饰，得以被继承
            super(hash, key, val, next);
        }

        */
/**
         * 返回红黑树的根节点
         *//*

        final TreeNode<K,V> root() {
            // 循环遍历树
            for (TreeNode<K,V> r = this, p;;) {
                // 如果节点的父节点为空，则为根节点
                if ((p = r.parent) == null) {
                    return r;
                }
                // 如果父节点不为空，继续向上遍历
                r = p;
            }
        }

        */
/**
         *  把红黑树的根节点设为其所在的数组槽的第一个元素
         *  TreeNode既是一个红黑树结构，也是一个双链表结构
         *  这个方法里做的事情，就是保证树的根节点一定也要成为链表的首节点
         *//*

        static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
            int n;
            // 如果有根节点并且 Entry 数组不为空并且数组长度大于0
            if (root != null && tab != null && (n = tab.length) > 0) {
                // 根据根节点的 hash 值，找到元素所在数组的桶索引
                int index = (n - 1) & root.hash;
                // 找到桶里第一个节点（相对于链表）
                TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
                // 如果传入的节点与首节点不同
                if (root != first) {
                    Node<K,V> rn;
                    // 将首节点换为传入的节点
                    tab[index] = root;
                    // 获取根节点前一个节点
                    TreeNode<K,V> rp = root.prev;
                    // 如果根节点的下一个节点不为空
                    if ((rn = root.next) != null) {
                        // 将根节点下一个节点的前一个节点设置为根节点的前一个节点
                        ((TreeNode<K,V>)rn).prev = rp;
                    }
                    // 如果根节点的前一个节点不为空
                    if (rp != null) {
                        // 根节点的前一个节点的下一个节点设置为根节点下一个节点
                        rp.next = rn;
                    }
                    // 如果该数组原来的根节点不为空
                    if (first != null) {
                        // 这个原有根节点的前节点指向到 root，此时 root 称为新的根节点
                        first.prev = root;
                    }
                    // root 的下一个节点为原有的根节点
                    root.next = first;
                    // root 的前节点设置为空
                    root.prev = null;
                }
                // 校验TreeNode对象是否满足红黑树和双链表的特性，如果为 false 就抛出异常
                assert checkInvariants(root);
            }
        }

        */
/**
         *  调用该方法的也为一个 TreeNode 的对象，该对象是树上的某个节点。以该节点为根节点，查找所有的子孙节点
         *  查看那个节点可以匹配上给定的键对象
         * @param h k 的 hash 值
         * @param k 要查找的对象
         * @param kc    k 的 Class 对象，该 Class 对象应该是实现了 Comparable<K> 的，否则为 null
         * @return  找到
         *//*

        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            // 把当前对象赋值给 p，同时验证了上面所说 -> 调用该方法的也为一个 TreeNode 的对象
            TreeNode<K,V> p = this;
            do {
                // ph -> 当前节点的 hash 值
                // dir -> 当前节点的方向
                // pk -> 当前节点的键值
                int ph, dir; K pk;
                // pl -> 当前节点的左孩子
                // pr -> 当前节点的右孩子
                // q -> 用来存储并返回找到的对象
                TreeNode<K,V> pl = p.left, pr = p.right, q;
                // 如果该节点的 hash 值大于需要查找的节点的 hash 值
                if ((ph = p.hash) > h) {
                    // 往左边找，准备进入下一次循环
                    p = pl;
                }
                // 如果该节点的 hash 值小于需要查找的节点的 hash 值
                else if (ph < h) {
                    // 往右边找，准备进入下一次循环
                    p = pr;
                }
                // 如果该节点的键等于需要查找的键或者当前节点的键值对对象 pk 和 k 相等（地址相同或者equals相同）
                else if ((pk = p.key) == k || (k != null && k.equals(pk))) {
                    // 返回当前节点
                    return p;
                }
                // 此时说明 hash 桶找到了，但是没有找到对应的节点

                // 如果左孩子为空
                else if (pl == null) {
                    // 往右边去寻找
                    p = pr;
                }
                // 如果右孩子为空
                else if (pr == null) {
                    // 往左边寻找
                    p = pl;
                }
                // 如果左右孩子都不为空，就要进行比较，是往左还是往右遍历
                // 如果传入的字节码不为空
                else if ((kc != null ||
                        // 如果 k 对象实现了 comparable<K> 方法
                        (kc = comparableClassFor(k)) != null) &&
                        // 比较之后不相等
                        (dir = compareComparables(kc, k, pk)) != 0) {
                    // 利用 comparable 方法判断往左还是往右遍历
                    p = (dir < 0) ? pl : pr;
                }
                // 无法通过 comparable 方法比较或者比较之后还是相等
                // 从右孩子递归查找，找到就返回
                else if ((q = pr.find(h, k, kc)) != null) {
                    return q;
                }
                // 从右孩子递归未找到，就从左孩子节点进行下一次循环
                else {
                    p = pl;
                }
            } while (p != null);
            // 到最后都没有找到就返回空
            return null;
        }

        */
/**
         *  从根结点寻找 h 和 k 符合的节点
         * @param h 节点的 hash 值
         * @param k 需要寻找的对象
         * @return
         *//*

        final TreeNode<K,V> getTreeNode(int h, Object k) {
            // 从这里可以看出调用这个方法的一定是根节点
            return ((parent != null) ? root() : this).find(h, k, null);
        }

        */
/**
         *  这个方法在 hashCode（）值相等和不可比较的时候对插入的节点进行排序，返回值要不是 -1，要不是 1，不会为 0
         *  这样就能保证插入的节点要么就是左孩子，要不是右孩子，不然就无法满足二叉树的数据结构
         * @param a 插入对象 a
         * @param b 插入对象 b
         * @return  返回顺序
         *//*

        static int tieBreakOrder(Object a, Object b) {
            int d;
            // 如果对象 a 为空或者对象 b 为空
            if (a == null || b == null ||
                    // 或者对象 a 的字节码名称与对象 b 的字节码名称对比值也相同
                    (d = a.getClass().getName().
                            compareTo(b.getClass().getName())) == 0) {
                // 调用本地方法为两个对象生成 hashCode 值，在进行比较，如果还是相等就返回 -1，并赋值于 d
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                        -1 : 1);
            }
            return d;
        }

        */
/**
         *  将该对象为首节点的链表转换为红黑树
         * @param tab   HashMap 元素数组
         *//*

        final void treeify(Node<K,V>[] tab) {
            // 定义树的根节点
            TreeNode<K,V> root = null;
            // 向下遍历链表
            for (TreeNode<K,V> x = this, next; x != null; x = next) {
                // 讲下一个节点强转为树节点
                next = (TreeNode<K,V>)x.next;
                // 设置该节点的左右孩子都为空
                x.left = x.right = null;
                // 如果还没有根节点
                if (root == null) {
                    // 将该节点的父节点设置为空
                    x.parent = null;
                    // 该节点为黑色
                    x.red = false;
                    // 根节点指向到该节点
                    root = x;
                }
                // 如果有根节点了
                else {
                    // 取得当前节点的键
                    K k = x.key;
                    // 取得当前节点的 hash 值
                    int h = x.hash;
                    // 定义当前键的字节码
                    Class<?> kc = null;
                    // 从根节点进行遍历，死循环写法，只能从内部跳出
                    for (TreeNode<K,V> p = root;;) {
                        // 定义表示方向
                        // 定义当前节点的 hash 值
                        int dir, ph;
                        // 当前节点的键
                        K pk = p.key;
                        // 如果当前节点的 hash 值大于当前链表节点的 hash 值
                        if ((ph = p.hash) > h) {
                            // 当前链表节点放在当前树节点的左侧
                            dir = -1;
                        }
                        // 如果当前节点的 hash 值小于当前链表节点的 hash 值
                        else if (ph < h) {
                            // 当前链表节点放在当前树节点的右侧
                            dir = 1;
                        }
                        // 此时两个节点的 hash 值相等
                        // 如果节点的键没有实现 comparable<K> 接口
                        else if ((kc == null &&
                                (kc = comparableClassFor(k)) == null) ||
                                // 或者实现 comparable<K> 接口对比值为 0，即相等
                                (dir = compareComparables(kc, k, pk)) == 0) {
                            // 强行进行排序
                            dir = tieBreakOrder(k, pk);
                        }
                        // 保存当前节点
                        TreeNode<K,V> xp = p;
                        // dir <= 0 -> 如果当前节点会放在根节点或者根节点的子节点的左边
                        // dir >  0 -> 如果当前节点会放在根节点或者根节点的子节点的右边

                        // 如果当前节点不是叶子节点（if 条件为 false），就以当前节点为起点循环遍历
                        // 如果当前节点是叶子节点（if 条件为 true），就根据 dir 的值，将该节点挂载在当前节点的左边或者右边
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            // 当前链表节点作为当前节点的子节点
                            x.parent = xp;
                            if (dir <= 0) {
                                // 左孩子
                                xp.left = x;
                            }
                            else {
                                // 右孩子
                                xp.right = x;
                            }
                            // 重新平衡
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            // 经过了多次平衡后，根节点是链表结构的哪个位置还不确定
            // 调用该方法将根节点设置为链表的首节点
            moveRootToFront(tab, root);
        }

        */
/**
         *  将红黑树节点转为链表节点
         * @param map   需要转换的 map
         * @return  转换后的链表的首节点
         *//*

        final Node<K,V> untreeify(HashMap<K,V> map) {
            // 定义首节点
            // 定义尾节点
            Node<K,V> hd = null, tl = null;
            // 从调用该方法的节点, 即链表的首结点开始遍历, 将所有节点全转为链表节点
            for (Node<K,V> q = this; q != null; q = q.next) {
                // 构建链表节点
                Node<K,V> p = map.replacementNode(q, null);
                // 如果尾节点为空（第一次进来）
                if (tl == null) {
                    // 当前节点为第一个节点，赋值给首节点
                    hd = p;
                }
                // 否则将尾节点的下一个节点设置为该节点
                else {
                    tl.next = p;
                }
                // 每次都将尾节点指向当前节点
                tl = p;
            }
            // 返回转换后的首节点
            return hd;
        }

        */
/**
         *  红黑树插入节点，同时维护原来的链表属性
         * @param map   原来的 HashMap
         * @param tab   节点数组 table
         * @param h     节点的 hash 值
         * @param k     节点的键
         * @param v     节点要写入的值
         * @return  指定键所匹配到的节点对象，针对这个对象去修改值（返回空说明创建了一个新节点）
         *//*

        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab,
                                       int h, K k, V v) {
            // 定义键的字节码
            Class<?> kc = null;
            // 标识是否已经遍历过，未必是从根节点遍历的，但是遍历路径上一定已经包含了后续需要比对的所有节点
            boolean searched = false;
            // 父节点不为空则不是根节点，为空，则是根节点
            TreeNode<K,V> root = (parent != null) ? root() : this;
            // 从根节点循环遍历，死循环写法，只能从内部跳出
            for (TreeNode<K,V> p = root;;) {
                // 定义表示方向
                // 定义当前节点的 hash 值
                // 定义当前节点的键
                int dir, ph; K pk;
                // 如果当前节点的 hash 值大于当前链表节点的 hash 值
                if ((ph = p.hash) > h) {
                    // 当前链表节点放在当前树节点的左侧
                    dir = -1;
                }
                // 如果当前节点的 hash 值小于当前链表节点的 hash 值
                else if (ph < h) {
                    // 当前链表节点放在当前树节点的右侧
                    dir = 1;
                }
                // 如果当前节点的键与要放入的 k 相同或者字符串值相同
                else if ((pk = p.key) == k || (k != null && k.equals(pk))) {
                    // 说明目标节点就是当前节点，直接返回，在外层方法写入 value
                    return p;
                }
                // 如果键的字节码为空
                else if ((kc == null &&
                        // 并且键没有实现 Comparable<K> 接口
                        (kc = comparableClassFor(k)) == null) ||
                        // 或者对比值为 0，即相等
                        (dir = compareComparables(kc, k, pk)) == 0) {
                    // 如果没有搜索过
                    if (!searched) {
                        // 定义要返回的节点
                        // 定义孩子节点
                        TreeNode<K,V> q, ch;
                        // 将搜索过标志位设为 true
                        searched = true;
                        // 红黑树的遍历，使用的短路原酸，如果左边找到了就不用跑右边遍历了
                        // 如果左孩子不为空
                        if (((ch = p.left) != null &&
                                // 并且在左边找到了要寻找的节点
                                (q = ch.find(h, k, kc)) != null) ||
                                // 或者右孩子不为空
                                ((ch = p.right) != null &&
                                        // 并且在右边找到了要寻找的节点
                                        (q = ch.find(h, k, kc)) != null)) {
                            // 返回找到的节点
                            return q;
                        }
                    }
                    // 走到这里就说明，遍历了所有子节点也没有找到和当前键 equals 相等的节点
                    // 比较一下当前节点键和指定key键的大小
                    dir = tieBreakOrder(k, pk);
                }
                // 走到这里就说明
                // 将该节点信息赋值给 xp
                TreeNode<K,V> xp = p;
                // 如果该节点的已经是该方向最后一个节点
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    // 获取当前节点的下一个节点
                    Node<K,V> xpn = xp.next;
                    // 创建新的节点 x，其中 x 的 next 值为 xpn
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                    // 父节点为 xp
                    if (dir <= 0) {
                        // xp 的左孩子为 x
                        xp.left = x;
                    }
                    else {
                        // xp 的右孩子为 x
                        xp.right = x;
                    }
                    // xp 的下一个节点为 x
                    xp.next = x;
                    // x 的父节点和前一个节点都为 xp
                    x.parent = x.prev = xp;
                    // 如果 xpn 不为空
                    if (xpn != null) {
                        // 将 xpn 的 prev 节点设置为 x，与上文的 x 节点的 next 节点对应
                        ((TreeNode<K,V>)xpn).prev = x;
                    }
                    // 重新平衡，并重置根节点置顶
                    moveRootToFront(tab, balanceInsertion(root, x));
                    // 返回空，表示插入了一个新的节点
                    return null;
                }
            }
        }

        */
/**
         *  移除调用该方法的节点
         * @param map   HashMap
         * @param tab   节点数组
         * @param movable   是否重置根节点置顶
         *//*

        final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,
                                  boolean movable) {
            // 开始处理链表
            // 定义数组长度
            int n;
            // 如果节点数组为空或者数组长度为 0
            if (tab == null || (n = tab.length) == 0) {
                // 返回空
                return;
            }
            // 计算节点的桶索引
            int index = (n - 1) & hash;
            // 索引位置的首节点设置给 first 和 root
            // 定义右孩子
            TreeNode<K,V> first = (TreeNode<K,V>)tab[index], root = first, rl;
            // succ 为该节点为下一个节点
            // pred 为该节点为红黑树时的前一个节点
            TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;
            // 如果为该节点为红黑树时的前一个节点为空
            if (pred == null) {
                // 将桶索引位置的值和 first 节点的值赋值为 succ 节点
                // 此时 succ 节点的父节点还是要删除的节点
                tab[index] = first = succ;
            }
            // 如果为该节点红黑树时的前一个节点不为空
            else {
                // 该节点红黑树时的前一个节点的下一个节点设置为该节点的下一个节点
                pred.next = succ;
            }
            // 如果该节点的下一个节点不为空
            if (succ != null) {
                // 将该节点为红黑树的前一个节点设置为该节点下一个节点为红黑树时的前一个节点，与上面对应
                succ.prev = pred;
            }
            // 如果此处 first 为空, 则代表该索引位置已经没有节点，直接返回
            if (first == null) {
                return;
            }
            // 如果 root 的父节点不为空, 则将root赋值为根节点
            // root 在上面被赋值为索引位置的首节点, 索引位置的头节点并不一定为红黑树的根节点
            if (root.parent != null) {
                // 将红黑树的根节点赋值给 root
                root = root.root();
            }
            // 通过 root 节点来判断此红黑树是否太小, 如果是则调用 untreeify 方法转为链表节点并返回
            // 如果根节点为空
            if (root == null
                    // 或者重置根节点置顶
                    || (movable
                    // 并且根节点的右孩子为空
                    && (root.right == null
                    // 或者根节点的左孩子为空
                    || (rl = root.left) == null
                    // 或者左孩子的左孩子为空
                    || rl.left == null))) {
                // 转换为链表节点并返回
                tab[index] = first.untreeify(map);
                return;
            }
            // 处理链表结束
            // 红黑树处理开始，链表已经完成部分
            // 将 p 赋值为当前节点
            // 定义左孩子 pl
            // 定义右孩子 pr
            // 定义 replacement
            TreeNode<K,V> p = this, pl = left, pr = right, replacement;
            // 如果左孩子不为空切右孩子不为空
            if (pl != null && pr != null) {
                // 定义 s 为右孩子
                // 定义 s 的左孩子
                TreeNode<K,V> s = pr, sl;
                // 向左一直查找,直到叶子节点,跳出循环时,s 为叶子节点
                while ((sl = s.left) != null) {
                    s = sl;
                }
                // 交换 p 节点和 s 节点（叶子节点）的颜色
                boolean c = s.red; s.red = p.red; p.red = c;
                // s 节点的右孩子
                TreeNode<K,V> sr = s.right;
                // p 节点的父节点
                TreeNode<K,V> pp = p.parent;
                // 如果 p 的右孩子是叶子节点 s
                // 第一次调整树结构
                if (s == pr) {
                    // 将 p 节点父节点设置为 叶子节点 s
                    p.parent = s;
                    // 将叶子节点 s 的右孩子设置为 p
                    s.right = p;
                }
                // 如果 p 的右孩子不是叶子节点 s
                else {
                    // 将 s 节点父节点赋值与 sp
                    TreeNode<K,V> sp = s.parent;
                    // 将 sp 节点的赋值给 p 节点的父节点，如果 sp 节点不为空
                    if ((p.parent = sp) != null) {
                        // 如果 s 节点为左孩子
                        if (s == sp.left) {
                            // 将 p 节点赋值给 s 的父节点的左孩子
                            sp.left = p;
                        }
                        // 如果 s 节点为右孩子（何时会走进来不知道）
                        else {
                            // 将 p 节点赋值给 s 的父节点的右孩子
                            sp.right = p;
                        }
                    }
                    // 将 p 的右节点赋值给 s 的右孩子，如果 p 的右孩子不为空
                    if ((s.right = pr) != null) {
                        // 将 s 节点赋值给 p 的右孩子的父节点
                        pr.parent = s;
                    }
                }
                // 第二次调整树结构
                p.left = null;
                // 如果 sr 不为空，将 sr 赋值给 p 的右孩子
                if ((p.right = sr) != null) {
                    // p 设置为 sr 的父节点
                    sr.parent = p;
                }
                // 如果 pl 不为空，将 pl 赋值给 s 的左孩子
                if ((s.left = pl) != null) {
                    // s 设置为 pl 的父节点
                    pl.parent = s;
                }
                // pp 为空
                if ((s.parent = pp) == null) {
                    // 此时 s 节点是根节点
                    root = s;
                }
                // 否则，如果之前 p 是 pp 的左孩子
                else if (p == pp.left) {
                    // s 设置为 pp 的左孩子
                    pp.left = s;
                }
                // 如果之前 p 是 pp 的右孩子
                else {
                    // s 设置为 pp 右孩子
                    pp.right = s;
                }
                // 如果 sr 节点不是空，把 sr 赋值给替换节点
                if (sr != null) {
                    replacement = sr;
                }
                // 如果 sr 节点是空，把 p 赋值给替换节点
                else {
                    replacement = p;
                }
            }
            // 如果 p 的左孩子不为空，即右孩子为空
            else if (pl != null) {
                // 把 p 的左孩子赋值给替换节点
                replacement = pl;
            }
            // 如果 p 的右孩子不为空，即左孩子为空
            else if (pr != null) {
                // 把 p 的右孩子赋值给替换节点
                replacement = pr;
            }
            // 如果 p 的左右孩子都为空，即 p 为叶子节点
            else {
                // 把 p 赋值给替换节点
                replacement = p;
            }
            // 如果 p 不是叶子节点
            if (replacement != p) {
                // 将替换节点的父节点赋值为 p 节点的父节点, 同时赋值给 pp 节点
                TreeNode<K,V> pp = replacement.parent = p.parent;
                // 如果 p 没有父节点，p 就是根节点
                if (pp == null) {
                    // 将替换节点赋值给 root
                    root = replacement;
                }
                // 如果 P 不是根节点，并且 p 是 pp 的左孩子
                else if (p == pp.left) {
                    // 将替换节点赋值给 pp 的左孩子
                    pp.left = replacement;
                }
                // 如果 P 不是根节点，并且 p 是 pp 的右孩子
                else {
                    // 将替换节点赋值给 pp 的右孩子
                    pp.right = replacement;
                }
                // 把 p 节点置空，以便垃圾收集器回收
                p.left = p.right = p.parent = null;
            }
            //  如果 p 节点不为红色则进行红黑树删除平衡调整
            TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);
            // 如果 p 节点是叶子节点，则简单的将 p 节点去除即可
            if (replacement == p) {
                // 将 p 的父节点赋值给 pp
                TreeNode<K,V> pp = p.parent;
                // 将 p 的父引用置空
                p.parent = null;
                // 如果 p 存在父节点
                if (pp != null) {
                    // 如果 p 是左孩子
                    if (p == pp.left) {
                        // 把 p 的左孩子置空
                        pp.left = null;
                    }
                    // 如果 p 是右孩子
                    else if (p == pp.right) {
                        // 把 p 的右孩子置空
                        pp.right = null;
                    }
                }
            }
            // 如果需要重置根节点
            if (movable) {
                // 将 root 节点移到索引位置的头节点
                moveRootToFront(tab, r);
            }
        }

        */
/**
         *  将映射表扩大或者缩小
         * @param map   这个 HashMap
         * @param tab   映射表
         * @param index 要修剪的表索引
         * @param bit   要修剪的位数（Hash 值）
         *//*

        final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
            TreeNode<K,V> b = this;
            // loHead用于存储低位（位置不变）key的链头，loTail用于指向链位位置
            TreeNode<K,V> loHead = null, loTail = null;
            // hiHead用于存储高位（位置改变）key的链头，hiTail用于指向链位位置
            TreeNode<K,V> hiHead = null, hiTail = null;
            //
            int lc = 0, hc = 0;
            // 遍历树
            for (TreeNode<K,V> e = b, next; e != null; e = next) {
                // 该节点的下一个节点
                next = (TreeNode<K,V>)e.next;
                // 将该节点的下一个节点置空
                e.next = null;
                // 如果其 hash 值没有变，说明没有被修剪到扩充栏或缩减栏（扩容或者缩小时，可能要将节点放到之前的 index + 之前的表长度）
                if ((e.hash & bit) == 0) {
                    // 把该节点赋值给 loTail, 如果 e 前面没有节点，说明是头节点
                    if ((e.prev = loTail) == null) {
                        // 赋值给 loHead
                        loHead = e;
                    }
                    // 如果 e 前面有节点
                    else {
                        // 将 e 赋值给 loTail 的下一个节点
                        loTail.next = e;
                    }
                    // 将 e 赋值给 loTail 节点
                    loTail = e;
                    // 低位的节点数量
                    ++lc;
                }
                else {
                    // 如果其 hash 值没有变，说明被修剪到扩充栏或缩减栏（扩容或者缩小时，可能要将节点放到之前的 index + 之前的表长度）
                    if ((e.prev = hiTail) == null) {
                        // 赋值给 hiHead
                        hiHead = e;
                    }
                    // 如果 e 前面有节点
                    else {
                        // 将 e 赋值给 hiTail 的下一个节点
                        hiTail.next = e;
                    }
                    // 将 e 赋值给 hiTail 节点
                    hiTail = e;
                    // 高位的节点数量
                    ++hc;
                }
            }

            if (loHead != null) {
                //如果低位上的树节点小于等于6个那就去树化变回单向链表
                if (lc <= UNTREEIFY_THRESHOLD)
                    // 将树转换为链表
                    tab[index] = loHead.untreeify(map);
                else {
                    // 否则让索引位置的节点指向 loHead 树
                    tab[index] = loHead;
                    // hiHead 不为空说明原来索引位置上的树少了，修剪过，否则没少，没修剪过就不用动
                    if (hiHead != null)
                        // 重新初始化树
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                //如果高位上的树节点小于等于6个那就去树化变回单向链表
                if (hc <= UNTREEIFY_THRESHOLD){
                    // 将树转换为链表
                    tab[index + bit] = hiHead.untreeify(map);
                }
                else {
                    // 否则让索引位置的节点指向 hiHead 树
                    tab[index + bit] = hiHead;
                    // loHead 不为空说明原来索引位置上的树少了，修剪过，否则没少，没修剪过就不用动
                    if (loHead != null)
                        // 重新初始化树
                        hiHead.treeify(tab);
                }
            }
        }

        // 左旋
        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
                                              TreeNode<K,V> p) {
            TreeNode<K,V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        // 右旋
        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                               TreeNode<K,V> p) {
            TreeNode<K,V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        */
/**
         *  插入之后平衡树
         * @param root  根节点
         * @param x     插入的节点
         * @param <K>   键类型
         * @param <V>   值类型
         * @return
         *//*

        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
                                                    TreeNode<K,V> x) {
            // 插入的节点一定是红色节点
            x.red = true;
            // 进入死循环
            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
                // 插入节点的父节点赋值给 xp 并且为空
                if ((xp = x.parent) == null) {
                    // 则 x 是根节点，根节点为黑色节点
                    x.red = false;
                    return x;
                }
                // 如果不是根节点
                // 如果插入的节点的父节点不是红色的或者插入节点的爷爷节点为空
                else if (!xp.red || (xpp = xp.parent) == null) {
                    // 直接返回根节点
                    return root;
                }
                // 插入节点的爷爷节点左孩子 赋值给 xppl
                // 如果插入节点的父节点就是左孩子
                if (xp == (xppl = xpp.left)) {
                    // 插入节点的爷爷节点右孩子 赋值给 xppr， 并且是红色节点
                    if ((xppr = xpp.right) != null && xppr.red) {
                        // 将爷爷节点的右节点设置为黑色节点
                        xppr.red = false;
                        // 将父节点设置为黑色节点
                        xp.red = false;
                        // 将爷爷节点设置为红色节点
                        xpp.red = true;
                        // 将爷爷节点赋值给 x 节点
                        x = xpp;
                    }
                    //
                    else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                }
                else {
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        */
/**
         *  删除节点后平衡树
         * @param root
         * @param x
         * @param <K>
         * @param <V>
         * @return
         *//*

        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
                                                   TreeNode<K,V> x) {
            for (TreeNode<K,V> xp, xpl, xpr;;) {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (x.red) {
                    x.red = false;
                    return root;
                }
                else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                                (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        }
                        else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                        null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                }
                else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                                (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        }
                        else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                        null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

        */
/**
         *  递归检查是否满足红黑树的性质
         *//*

        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {
            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,
                    tb = t.prev, tn = (TreeNode<K,V>)t.next;
            if (tb != null && tb.next != t) {
                return false;
            }
            if (tn != null && tn.prev != t) {
                return false;
            }
            if (tp != null && t != tp.left && t != tp.right) {
                return false;
            }
            if (tl != null && (tl.parent != t || tl.hash > t.hash)) {
                return false;
            }
            if (tr != null && (tr.parent != t || tr.hash < t.hash)) {
                return false;
            }
            if (t.red && tl != null && tl.red && tr != null && tr.red) {
                return false;
            }
            if (tl != null && !checkInvariants(tl)) {
                return false;
            }
            if (tr != null && !checkInvariants(tr)) {
                return false;
            }
            return true;
        }
    }
}
*/
