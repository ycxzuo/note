# Hash 的使用指令

## 描述

Hash 是 field 和 value 的映射集合，特别适合存储对象，相比 String 而言，占用更少的内存空间，并可以对整个对象进行存取



## Hash 的指令

```properties
赋值语句
HSET KEY_NAME FIELD VALUE : 为指定的 KEY，设定 FIELD/VALUE 映射

HSETNX KEY_NAME FIELD VALUE : 只有在 FIELD 不存在时，设置 FIELD 的值，并返回 1，如果存在，则不设置值，并返回 0

HMSET KEY_NAME FIELD VALUE FIELD1 VALUE1 ... : 设置多个 FIELD/VALUE 映射

HINCRBY KEY_NAME FIELD VALUE : 为存储在 key 中的整数值增加 VALUE 的值，如果加一，可以使用 HINCR 命令

HINCRBYFLOAT KEY_NAME FIELD VALUE : 为存储在 key 中的浮点数数值增加 VALUE 的值

HDECRYBY/HDECRBYFLOAT 和上面两个指令相似

HEXISTS KEY_NAME FIELD : 查看存储在 key 的映射中是否有 FIELD 字段

取值语句
HGET KEY_NAME FIELD : 获取存储在 key 中的值，根据 FIELD 获取 VALUE

HMGET KEY_NAME FIELD1 FIELD2 ... : 获取 存储在 key 中的值，根据 FIELD 获取所有的 VALUE

HGETALL KEY_NAME : 返回存储在 key 中所有的 FIELD 和 VALUE

HKEYS KEY_NAME : 获取存在 key 中所有的 FILED 值

HLEN KEY_NAME : 获取 key 中 FIELD 的数量

删除语句
HDEL KEY FIELD FIELD1 : 删除一个或者多个 FIELD
```



## 应用场景

用于存储一个对象

### 原因

如果用Stirng 存储 JSON 代替，可能会有线程安全问题

如果用多个 String 来存储，会增加内存占用，并且取出时不方便