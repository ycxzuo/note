# Servlet 概念（基于 3.1）

## 什么是 Servlet

基于 Java 技术的 web 组件，容器托管的，用于生成动态内容。基于 Java 技术，所以与平台无关。容器，有时候也叫做 Serlvet 引擎，是 web server 支持 servlet 功能扩展的部分。客户端通过 Servlet 容器实现请求/应答模型与 Servlet 交互。



## 什么是 Servlet 容器

Serlvet 容器是 web server 或 application server 的一部分，提供基于请求/响应发送模型的网络服务，**解码基于 MIME 的请求，并且格式化基于 MIME 的响应**。Servlet 容器也包含了管理 Servlet 生命周期

所有 Servlet 容器**必须**支持基于 HTTP 协议的请求/响应模型



## 例子

典型的事件序列：

1. 客户端（如 web 浏览器）发送一个 HTTP 请求到 web 服务器
2. Web 服务器接收到请求并交给 servlet 容器处理，servlet 容器可以运行在与宿主 web 服务器同一个进程中，也可以是同一主机不同进程，或者位于不同主机的 web 服务器中，对请求进行处理
3. servlet 容器根据 servlet 配置选择相应的 servlet，并使用代表请求和响应对象的参数进行调用
4. servlet 通过请求对象得到远程用户，HTTP POST 参数和其他有关数据可能作为请求的一部分随请求一起发送过来。Servlet 执行我们编写的任意逻辑，然后动态产生响应内容发送回客户端。发送数据到客户端是通过响应对象完成的
5. 一旦 servlet 完成请求的处理，servlet 容器必须确保响应正确的刷出，并且将控制权还给宿主 Web 服务器



## Servlet 与其他技术的比较

处于 CGI 和私有 server 扩展两者之间

* 公共网关接口 CGI
  * 与平台无关
  * CGI 每个请求都要启动一个进程，而 servlet 每次调用时并不是新启用一个进程 ，而是在一个 Web 服务器的进程敏感词享和分离线程，而线程最大的好处在于可以共享一个数据源，使系统资源被有效利用
* 私有的 server 扩展
  * 采用标准的 API，从而支持更多的 web server