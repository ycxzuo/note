# boolean 类型

如果是成员变量，会使用 int 来保存。

```java
boolean flag = true;
if (flag) -> if (flag != 0);
if (flag == true) -> if (flag == 1);
```

如果是静态变量，也会使用 int 来存储，但是存储方式为

```java
static Field flag:Z;
---------------------------------------------
static Method "<clinit>":"()V"
	stack 1 locals 0
{
		iconst_1;// 将 boolean 压入栈，改为 3 效果一致
		putstatic	Field flag:"Z";
		return;
}
```

在使用的时候会使用最低位去判断，即存储 0 与 2 的结果是一致的

可以使用 asmtools 工具进行修改

