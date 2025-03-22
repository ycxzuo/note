# WLS ubuntu 安装 docker

## 安装 docker 的命令

`sudo apt install docker-ce docker-ce-cli containerd.io`



## 如果安装过程出以下错误

## 错误1

```
Reading package lists... Done
Building dependency tree
Reading state information... Done
E: Unable to locate package docker-ce
E: Unable to locate package docker-ce-cli
E: Unable to locate package containerd.io
E: Couldn't find any package by glob 'containerd.io'
```

原因是镜像包中没有新版的docker仓库，此时需要去更新镜像地址



### 添加阿里云证书

`curl -fsSL https://mirrors.aliyun.com/docker-ce/linux/ubuntu/gpg | sudo apt-key add`



### 添加阿里云镜像地址

可以用命令添加或者使用配置增加

#### 命令添加

`sudo add-apt-repository "deb [arch=amd64] https://mirrors.aliyun.com/docker-ce/linux/ubuntu $(lsb_release -cs) stable"`

#### 配置添加

```
cd /etc/apt/sources.list.d        
sudo touch docker.list            
sudo chmod 666 docker.list
sudo echo "deb [arch=amd64] https://mirrors.aliyun.com/docker-ce/linux/ubuntu $(lsb_release -cs) stable" > docker.list
```



### 更新软件列表并保存本地

`sudo apt-get update`



## 错误2

```
$ docker -v

The command 'docker' could not be found in this WSL 2 distro.
We recommend to activate the WSL integration in Docker Desktop settings.

For details about using Docker Desktop with WSL 2, visit:

https://docs.docker.com/go/wsl2/
```

这是 Docker Desktop settings 没有开启对 ubuntu 的设置



### 解决方案

打开 Docker Desktop，选择设置 Settings -> Resources -> WSL integration，打开对应的 ubuntu 选项即可



## 错误3

```
$ docker image ls
permission denied while trying to connect to the Docker daemon socket at unix:///var/run/docker.sock: Head "http://%2Fvar%2Frun%2Fdocker.sock/_ping": dial unix /var/run/docker.sock: connect: permission denied
```

用户权限设置问题，一般是刚刚安装docker还没有刷新用户组权限



### 解决方案

给自己配置权限并刷新用户组

#### 查看用户组

`cat /etc/group`

看是否存在 docker 用户组，不存在的话需要添加 docker 用户组，如果发现存在用户组并且当前用户在 docker 用户组，则直接执行更新用户组操作

`sudo groupadd docker`



#### 将用户添加到 docker 用户组中

`sudo gpasswd -a $USER docker`



#### 更新用户组

`newgrp docker`