# Docker+Jenkins 环境搭建

## 使用 docker 命令装 jenkins

```sh
docker run -d --restart=unless-stopped --name docker_jenkins -u root \
-v $(which docker):/usr/bin/docker \
-v /var/run/docker.sock:/var/run/docker.sock \
-v /usr/local/dockerinfo/jenkins:/var/jenkins_home \
-p 28080:8080 -p 50000:50000 jenkinszh/jenkins-zh
```

命令解析

```sh
-u root
# 使用 root 身份运行，避免无法执行宿主机的 docker 命令

-v $(which docker):/usr/bin/docker
# 将宿主机的 docker 命令目录挂载到 jenkins 容器内部
# $(which docker)：动态获取当前 docker 命令目录

-v /var/run/docker.sock:/var/run/docker.sock
# 挂载 docker 容器进程通信文件到容器内
# docker.sock 文件官方解释：It’s the unix socket the Docker daemon listens on by default and it can be used to communicate with the daemon from within a container.

-v /usr/local/dockerinfo/jenkins:/var/jenkins_home
# 挂载 jenkins 的数据目录

jenkinszh/jenkins-zh
# jenkins 的插件默认下载地址是：https://www.google.com，这个在国内无法访问的
```



## 登录 Jenkins

进入 docker 容器查找登录密码

```sh
docker exec -it docker_jenkins bash
cat /var/jenkins_home/secrets/initialAdminPassword
```

然后登录 jenkins 网址：http://localhost:28080

复制密码然后安装推荐模式



## 配置 Maven 插件

Manage Jenkins -> Manage Plugins -> 搜索 Maven Integration 安装



## 配置 Git 用户凭据

系统管理 -> Manage Credentials -> jenkins -> 全局凭证 -> 添加凭据



## 配置 Maven 和 JDK

Manage Jenkins -> Global Tool Configuration

* Maven 自动安装
* JDK docker 环境中有，可以使用 `echo $JAVA_HOME` 找到路径