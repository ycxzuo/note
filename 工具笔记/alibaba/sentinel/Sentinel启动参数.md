# Sentinel启动参数

## Sentinel控制台启动参数

### 基本启动参数

```properties
java -Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard.jar
```

* Dserver.port
  * 控制台启动端口参数
* Dcsp.sentinel.dashboard.server
  * 控制台访问url
* Dproject.name
  * 控制台自己注册自己的项目名称
* Dsentinel.dashboard.auth.username
  * 指定控制台的登录用户名
* Dsentinel.dashboard.auth.password
  * 指定控制台的登录密码
* Dserver.servlet.session.timeout
  * 指定服务端 session 过期时间，单位为秒



