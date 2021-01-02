# Spring 资源管理

## 引入动机

* Java 标准资源管理器强大，然而扩展复杂，资源存储方式并不统一
* Spring 要自立门户
* Spring “抄”、“超” 和 “潮”



## Java 标准资源管理

* Java 标准资源定位

| 职责         | 说明                                                         |
| ------------ | ------------------------------------------------------------ |
| 面向资源     | 文件系统、artifact（jar、war、ear 文件）以及远程资源（HTTP、FTP 等） |
| API 整合     | `java.lang.ClassLoader#getResource`、`java.io.File` 或 `java.net.URL` |
| 资源定位     | `java.net.URL` 或 `java.net.URI`                             |
| 面向流式存储 | `java.net.URLConnection`                                     |
| 协议扩展     | `java.net.URLStreamHandler` 或 `java.net.URLStreamHandlerFactory` |

* Java URL 协议扩展

  * 基于 `Java.net.URLStreamHandler`
  * 基于 `java.net.URLStreamHandlerFactory`

* 基于 `java.net.URLStreamHandler` 扩展协议

  * JDK 1.8 内建协议实现

  | 协议   | 实现类                                |
  | ------ | ------------------------------------- |
  | file   | `sun.net.www.protocol.file.Handler`   |
  | ftp    | `sun.net.www.protocol.ftp.Handler`    |
  | http   | `sun.net.www.protocol.http.Handler`   |
  | https  | `sun.net.www.protocol.https.Handler`  |
  | jar    | `sun.net.www.protocol.jar.Handler`    |
  | mailto | `sun.net.www.protocol.mailto.Handler` |
  | netdoc | `sun.net.www.protocol.netdoc.Handler` |

  * 实现类名必须为 `Handler`

    | 实现类命名规则 | 说明                                                         |
    | -------------- | ------------------------------------------------------------ |
    | 默认           | `sun.net.www.protocol.${protocol}.Handler`                   |
    | 自定义         | 通过 Java Properties java.protocol.handler.pkgs 指定实现类包名，实现类名必须为 `Hadnler`。如果存在多包名指定，通过分隔符 `|` |

  

## Spring 资源管理接口

| 类型       | 接口                                                         |
| ---------- | ------------------------------------------------------------ |
| 输入流     | org.springframework.core.io.InputStreamSource                |
| 只读资源   | org.springframework.core.io.Resource                         |
| 可写资源   | org.springframework.core.io.WritableResource                 |
| 编码资源   | org.springframework.core.io.support.EncodedResource          |
| 上下文资源 | org.springframework.core.io.ContextResource（context 是指的 Servlet 的上下文，主要提供给 Servlet 引擎使用） |



## Spring 内建 Resource 实现

| 资源来源       | 资源协议       | 实现类                                                       |
| -------------- | -------------- | ------------------------------------------------------------ |
| Bean 定义      | 无             | org.springframework.beans.factory.support.BeanDefinitionResource |
| 二进制字节数组 | 无             | org.springframework.core.io.ByteArrayResource                |
| 类路径         | classpath:/    | org.springframework.core.io.ClassPathResource                |
| 文件系统       | file:/         | org.springframework.core.io.FileSystemResource               |
| URL            | URL 支持的协议 | org.springframework.core.io.UrlResource                      |
| ServletContext | 无             | org.springframework.core.io.ContextResource                  |



## Spring Resource 扩展接口

* 可写资源接口
  * org.springframework.core.io.WritableResource
    * org.springframework.core.io.FileSystemResource
    * org.springframework.core.io.FileUrlResource (@since 5.0.2)
    * org.springframework.core.io.PathResource (@since 4.0 & @Deprecated)
* 编码资源接口
  * org.springframework.core.io.support.EncodedResource



## Spring 资源加载器

Resource 加载器

* org.springframework.core.io.ResourceLoader
  * org.springframework.core.io.DefaultResourceLoader
    * org.springframework.core.io.FileSystemResourceLoader
    * org.springframework.core.io.ClassRelativeResourceLoader
    * org.springframework.context.support.AbstractApplicationContext



## Spring 通配路径资源加载器

* 通配路径 ResourceLoader
  * org.springframework.core.io.support.ResourcePatternResolver
    * org.springframework.core.io.support.PathMatchingResourcePatternResolver
* 路径匹配器
  * org.springframework.util.PathMatcher
    * Ant 模式匹配实现：org.springframework.util.AntPathMatcher



## Spring 通配路径资源扩展

* 实现
  * org.springframework.util.PathMatcher
* 重置 PathMatcher
  * PathMatchingResourcePatternResolver#setPathMather



## 依赖注入 Spring  Resource

* 基于 @Value 实现

  * ```java
    @Value("classpath:/...")
    private Resource resource; 
    ```

### 注意

如果要用占位符的方式去匹配配置文件，要使用 `@Value("classpath*:/...")`



## 依赖注入 ResourceLoader

* 方法一：实现 ResourceLoaderAware 回调
  * org.springframework.context.support.ApplicationContextAwareProcessor#invokeAwareInterfaces
* 方法二：@Autowired 注入 ReourceLoader
  * org.springframework.context.support.AbstractApplicationContext#prepareBeanFactory
* 方法三：注入 ApplicationContext 作为 ResourceLoader
  * org.springframework.context.support.AbstractApplicationContext#prepareBeanFactory

其实这三者是相同对象，都是 AbstractApplicationContext