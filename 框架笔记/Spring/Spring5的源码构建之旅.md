# Spring5的源码构建之旅

最近在看 `spring源码深度解析第二版`，需要先构建 Spring 的源码，于是开始自己的构建之旅，在公司构建是一帆风顺，可是不知道为什么回家后，构建起来就是各种辛酸，查了很多资料，很多都不是很全面，所以自己写一篇笔记方便大家和自己以后遇到相同问题有地可寻，二话不说，先来说说构建的事吧



## 环境准备

### Gradle

反正我用的最新版应该是 3.0 之后的都没问题，网上教程很多，也很简单，就不赘述了

### Jdk

1.8版本及以上，应该是标配了吧



## 下载源码并导入IDEA

我是先到 [Spring官网](http://spring.io) 页面，然后点击 SpringFramework 进入Spring 的主页，然后点击 github 的图标进入源码的地址，避免找错，然后 fork 或者 clone 随自己心意，然后使用 IDEA 导入 spring 项目，此时会，然后设置好 gradle 为本地的版本，下载版本是 5.1.x



## spring-core 模块的报错

```properties
Error:(20, 50) java: 程序包org.springframework.objenesis.instantiator不存在
Error:(21, 46) java: 程序包org.springframework.objenesis.strategy不存在
Error:(22, 46) java: 程序包org.springframework.objenesis.strategy不存在
...
```

这样的报错，原因貌似是因为源码打包的问题

### 解决办法

用命令行到达 spring-core 文件夹下，然后执行下面两个命令

```java
gradle objenesisRepackJar
gradle cglibRepackJar
```



## spring-aspect 模块报错

因为其中有些类使用了 `aspect` 关键字，但是 javac 编译器不能识别

### 解决办法

#### 安装 AspectJ 工具

相关链接是 eclipse 的链接，[下载网址](<https://www.eclipse.org/aspectj/downloads.php>)，下载完成后需要安装，双击 jar 或者使用 `java -jar aspectj-1.9.4.jar` 安装，傻瓜式安装就不解释了

#### 配置 IDEA

首先在 Project Structure 中找到 Facets，然后添加 AspectJ 其中加入 spring-aop.main 和 spring-aspect.main，最后在 Setting 中设置 Java complier 中 use complier 选择 Ajc，Path 要选择 `AspectJ\lib\aspectjtools.jar` 并且勾选上 Delegate to Javac，这个选项的作用是让只编译 AspectJ 的 Facets 项目，而其他则使用 JDK 代理



## spring-oxm 模块报错

应该是 spring-core 一样的问题，看起来是很多

```properties
Error:(20, 50) java: 程序包org.springframework.oxm.jaxb.test.FlightType不存在
Error:(21, 46) java: 程序包org.springframework.oxm.jaxb.test.Flights不存在
Error:(22, 46) java: 程序包org.springframework.oxm.jaxb.test.ObjectFactory不存在
...
```

### 解决办法

用命令行到达 spring-oxm 文件夹下，然后执行下面两个命令

```properties
gradle genCastor
gradle genJaxb
```



## 总结

以上是我遇到的一些问题，可能是 IDEA 版本高，没有碰到所说的 Kotlin 版本问题，如果遇到，可以更新 IDEA 的内嵌 Kotlin 的版本解决，希望对大家有帮助