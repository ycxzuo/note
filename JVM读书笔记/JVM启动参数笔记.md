# JVM启动参数笔记

## TieredStopAtLevel

分层编译

分层编译将 JVM 的执行状态

- 0：解释执行（也会 profiling）
- 1：执行不带 profiling 的C1代码，IDEA 默认设置的参数
- 2：执行仅带方法调用次数和循环回边执行次数 profiling 的 C1 代码
- 3：执行带所有 profiling 的 C1 代码
- 4：执行 C2 代码，JDK 默认的设置

在 JDK 1.7 之前，使用 `-client` 或 `-server` 来调整即时编译器

- 对于执行时间较短或对启动性能有要求的程序，采用编译效率较快的 C1，对应参数： `-client`
- 对于执行时间较长或 对峰值性能有要求的程序，采用生成代码执行效率较快的 C2，对应参数： `-server`

此参数是 JDK 1.7 引入的

