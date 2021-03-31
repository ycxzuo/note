# JDK 16 笔记

## 新特性

* JEP-338 **Vector API (Incubator)**

* JEP-376 **ZGC Concurrent Stack Processing**

  * ZGC 现在可以同时处理线程堆栈，允许 ZGC 在并发阶段处理 JVM 中的所有 root 节点，而不是 STW。ZGC 暂停中完成的工作量已经变得恒定，通常不会超过几百微秒

* JEP-380 **Unix domain sockets**

  * 提供 Unix 域套接字（AF_UNIX）的支持
    * java.nio.channels
    * SocketChannel
    * ServerSocketChannel

* JEP-387 **Elastic Metaspace**

* JEP-389 **Foreign Linker API (Incubator)**

  * 支持直接使用纯 Java 代码去调用本地代码，与 JEP 393 配合使用可以大程度的简化原本绑定到本机类库容易出错的过程

* JEP-390 **Warnings for Value-based Classes**

  * 增加了 `@BasedValue` 注解标记基于值的类，Java 9 已经废弃的基础类型包装类的构造器，在此版本会在编译的时候发出警告，如果在 synchronized 关键字中使用了这些类型或者其子类，会产生 synchronization 警告，相关的虚拟机参数 `-Xlint:synchronization`

* JEP-392 **Packaging Tool**

  * jpackage 可以用于生产环境，[JEP-392](https://openjdk.java.net/jeps/392)
    * `jpackage --name myapp --input lib --main-jar main.jar \--main-class myapp.Main`

* JEP-393 **Foreign-Memory Access API (Third Incubator)**

  * 允许 Java 程序安全有效的访问堆外内存

* JEP-394 **Pattern Matching for instanceof**

  * ```java
    private static void testPatternMatching(Object str) {
        if (str instanceof String) {
            String s = (String) str;
            System.out.println(s);
        }
    }
    ```

  * ```java
    private static void testPatternMatching(Object str) {
        if (str instanceof String s) {
            System.out.println(s);
        }
    }
    ```

* JEP-395 **Records**

  * ```java
    public record Point(int x, int y) {
    }
    ```

  * ```java
    public class Point {
        private final int x;
        private final int y;
        
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public int x() {
            return x;
        }
        
        public int y() {
            return y;
        }
        
        public boolean equals(Object o) {
            if (!(o instanceof Point)) return false;
            Point other = (Point) o;
            return other.x == x && other.y == y;
        }
        
        public int hashCode() {
            return Objects.hash(x, y);
        }
        
        public String toString() {
            return String.format("Point[x=%d, y=%d]")
        }
    }
    ```

* JEP-396 **Strongly Encapsulate JDK Internals by Default**

  * 之前 `--illegal-access=permit` 切换为了 `--illegal-access=deny`，在反射私有元素，并调用 setAccessible 时的影响
    * permit 允许对封装类型进行非法访问。当第一次尝试通过反射进行非法访问时会生成一个警告
    * warn 与 permit 一样，但每次非法访问尝试时都会产生错误
    * debug  同时显示非法访问尝试的堆栈跟踪
    * deny  不允许非法的访问尝试

* JEP-397 **Sealed Classes (Second Preview)**

  * 只允许接口被规定的类实现，并且该类必须为 final 修饰

  * ```java
    public sealed interface TestInterface permits Test1, Test2 {
    }
    ```

  * ```java
    public final class Test1 implements TestInterface {
    }
    ```

  * ```java
    public final class Test2 implements TestInterface {
    }
    ```

