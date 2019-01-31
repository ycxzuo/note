# Tomcat正统的类加载器架构

## Web 服务器概述

主流的 java Web 服务器都实现有自己定义的类加载器，一个很健全的 Web 服务器，要解决如下几个问题

* 部署在同一个服务器上的两个 Web 应用程序所使用的 Java 类库可以实现互相隔离
  * 解决相同的依赖不同版本问题
* 部署在同一个服务器上的两个 Web 应用程序所使用的 Java 类库可以互相共享
  * 解决相同依赖相同版本的类库存 n 份造成的资源浪费
* 服务器需要尽可能地保证自身的安全不受部署的 Web 应用程序影响。
  * 许多服务器本身也是 Java 语言实现的，服务器本身也有类库依赖的问题
* 支持 JSP 应用的 Web 服务器，大多数都需要支持`HotSwap`功能。
  * JSP 文件最终都要编译成 Java Class 才能有虚拟机执行，主流的 Web 服务器都会支持 JSP 生成类的热替换

由于存在上述问题，在部署 Web 应用时，单独的一个 ClassPath 就无法满足需求了，所以各种 Web 服务器都提供了好几个 ClassPath 路径供用户存放第三方类库，这些路径一般都以`lib`或`classes`命名。**被放置到不同路径中的类库，具备不同的访问范围和服务对象，通常，每一个目录都会有一个相应的自定义类加载器去加载放置在里面的 Java 类库**。



## Tomcat 服务器

在 Tomcat 目录结构中，有 4 组目录

* /common/*
  * 类库可被 Tomcat 和所有的 Web 应用程序使用
* /server/*
  * 类库可被 Tomcat 使用，对所有 Web 应用程序都不可见
* /shared/*
  * 类库可被所有的 Web 应用程序共同使用，但对Tomcat 自己不可见
* /WEB-INF/*
  * 类库仅仅可以被此 Web 应用程序使用，对 Tomcat 和其他 Web 应用程序都不可见

为了支持这套目录结构，并对目录里面的类库进行加载和隔离，Tomcat 自定义了多个类加载器，这些类加载器按照经典的双亲委派模型来实现的

![Tomcat类加载器](http://wx2.sinaimg.cn/mw690/0060lm7Tly1fzoraoqy5xj30d70mjwev.jpg)

* `Common ClassLoader` -> /common/*
* `Catalina ClassLoader` -> /server/*
* `Shared ClassLoader` -> /shared/*
* `WebApp ClassLoader` -> /WebApp/WEB-INF/*
* 每一个 JSP 文件对应一个 Jsp 类加载器

`JasperLoader`的家在范围仅仅是这个 JSP 文件所编译出来的那一个 Class，它出现的目的就是为了被丢弃。当服务器检测到 JSP 文件被修改时，会替换掉目前的`JasperLoader`实例，并通过在建立一个新的 Jsp 类加载器来实现 JSP 文件的`HotSwap`功能

对于 Tomcat 6.x 及以后的版本，只有指定了`tomcat/conf/catalina.properties`配置文件的`server.loader`和`share.loader`项才会真正建立`CatalinaClassLoader`和`SharedClassLoader`的实例。否则会用到这两个类加载器的地方都会用`CommonClassLoader`的实例代替，而默认的配置文件中没有设置这两个 loader 项，所以 Tomcat 6.x 顺理成章的把 /common、/server、/shared 三个目录默认合并到一起成一个 /lib 目录，相当于之前的 /common 目录中类库的作用。

