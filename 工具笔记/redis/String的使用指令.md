# String 的使用指令

## 描述

* string 类型是 Redis 最基本的数据类型，一个键最大能存储 512 MB
* string 数据结构是简单的 key-value 类型，也可以是数字等类型
* string 是二进制安全的，可以用来存储图片等数据



## String 的命令

```properties
赋值语句
SET KEY_NAME VALUE : 简单的赋值语句，用于设置给定 key 的值，如果 key 已经存在，这条语句会覆盖之前的值

SETNX KEY_NAME VALUE : 如果 key 值不存在，则设置给定 key 的值，并返回 1，如果存在，则不设置，并返回 0。（分布式锁的解决方案）

SETEX KEY_NAME EXPIRE_TIME VALUE : 设置 key 的值并将其过期时间设置为 expire 的过期时间，随后过期

MSET KAY_NAME1 VALUE1 KAY_NAME2 VALUE2 ... : 一次性写入多个值

INCR KEY_NAME : 将 key 中存储的数值加 1，如果 key 不存在，则初始化值为 0，再执行 INCR 操作

INCRBY KEY_NAME VALUE : 将 key 中存储的数值加 VALUE，如果 key 不存在，则初始化值为 0，再执行 INCR 操作

DECR KEY_NAME : 将 key 中存储的数值减 1

DECRBY KEY_NAME VALUE : 将 key 中存储的数值减 VALUE

APPEND KEY_NAME VALUE : 对于指定的 key 将字符串追加至末尾，如果不存在 key 值，则直接为其赋值

取值语句
GET KEY_NAME : 简单的取值语句，如果 key 值存在，并且存储的是 string 类型，则直接返回值，否则会报错；如果 key 值不存在，返回 nil

GETRANGE KEY_NAME START END : 用于获取存储在指定 key 中字符串的截取值，字符串的截取范围有 START 和 END 两个偏移量决定 （包头包尾）

GETBIT KEY_NAME OFFSET : 对 key所存储的字符串值，获取指定偏移量上的位 (bit)

GETSET KEY_NAME VALUE : 用于设置指定 key 的值，并返回之前的值，若之前不存在对应的 key，则返回 nil

STRLEN KEY_NAME : 返回 key 所存储的字符串值的长度

MSET KAY_NAME1 KAY_NAME2 ... : 一次性获取多个值

删值语句
DEL KEY_NAME : 删除指定的 key，如果存在，则返回值数字类型
```



## 应用场景

* String 类型经常用来保存单个字符串或者 JSON 类型的值
* 可以用来保存图片等二进制的数据
* 作为计数器，例如访问次数，投票数等

