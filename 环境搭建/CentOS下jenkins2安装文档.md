# CentOS 下 jenkins2 安装文档

## 下载

首先在 [jenkins官网](https://jenkins.io/zh/download/) 下载合适版本的 jenkins 2.x，我这里下载的是 jenkins-2.190.1-1.1.noarch.rpm



## 安装

下载的是一个 rpm 格式的文件，需要使用命令 `rpm -ivh jenkins-2.190.1-1.1.noarch.rpm`



## 配置

### 配置端口

`vim /etc/sysconfig/jenkins` 修改 `JENKINS_PORT="8080“` 为自己想设置的监听端口

### 配置 JDK 路径

`vim /etc/init.d/jenkins` 在 JDK 配置后面加入本机 JDK 的路径，例如我的

`/usr/local/tools/jdk1.8.0_162/bin/java`



## 启动

用命令 `service jenkins start` 启动 jenkins

用命令 `service jenkins stop` 停止 jenkins

用命令 `service jenkins restart` 重启 jenkins

启动时可能会弹出一句提示 

`Starting jenkins (via systemctl):  Warning: jenkins.service changed on disk. Run 'systemctl daemon-reload' to reload units.`

使用命令解决警告

`systemctl daemon-reload`

