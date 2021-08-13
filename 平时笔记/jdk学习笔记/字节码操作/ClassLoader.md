# ClassLoader

## 类加载顺序（loadClass）

* 尝试加载自己或 parent 已加载的类（findLoadedClass()）
* 尝试双亲委派加载类
* 执行 findClass() 找到 class（ClassLoader 是直接抛出 ClassNotFoundException，需要子类覆盖）
  * URLClassLoader#defineClass 定义类（可以利用这个方法生成不同的 Class 对象）

## SystemClassLoader 实现

jdk8 及之前

* sun.misc.Launcher.AppClassLoader
  * java.net.URLClassLoader
    * java.security.SecureClassLoader
      * java.lang.ClassLoader

jdk9 及之后

* jdk.internal.loader.ClassLoaders.AppClassLoader
  * jdk.internal.loader.BuiltinClassLoader
    * java.security.SecureClassLoader
      * java.lang.ClassLoader



## URL 意义

URLStreamHandler 是对 file、http、jar 等协议的抽象，各个协议有自己的 handler 去处理

由于 jdk9 引入了模块化，所以各个模块有自己的作用域，于是加入了 BuiltinClassLoader 作为中间层，让之前的 URLClassLoader 切换成了一个内部属性 URLClassPath，用于兼容之前的方式



## allocation.hpp 以及 allcation.cpp 文件

* ResourceObj
  * For objects allocated in the resource area (see resourceArea.hpp)
* CHeapObj
  * For objects allocated in the C-heap (managed by: free & malloc)
* StackObj
  * For objects allocated on the stack
* ValueObj
  * For embedded objects
* AllStatic
  * For classes used as name spaces
* MetaspaceObj
  * For classes in Metaspace (class data)

