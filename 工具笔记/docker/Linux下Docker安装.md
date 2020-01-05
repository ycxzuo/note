# Linux 下 Docker 安装

## 准备

拥有一台 Linux 系统的机器，我这里使用的是 VMware 搭建的一个 CentOS 7 的操作系统，搭建过程可以参考[官方文档](https://docs.docker.com/install/linux/docker-ce/centos/)



## 卸载历史版本

```properties
sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine
```



## 安装 Docker

先安装需要的包

```properties
sudo yum install -y yum-utils \
  device-mapper-persistent-data \
  lvm2
```

设置稳定的镜像仓库，国内推荐阿里云的

```properties
sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo
    
```

```properties
sudo yum-config-manager \
    --add-repo \
	http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
```

或者也可以配置[云加速器](https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors)

```properties
vi /etc/docker/daemon.json
```

```properties
{
  "registry-mirrors": ["https://gi5ikucc.mirror.aliyuncs.com"]
}
```

可以先查看仓库中所有的 docker 版本，版本从高到低排列

```properties
yum list docker-ce --showduplicates | sort -r
```

安装最新版本的 docker 引擎

```properties
sudo yum install docker-ce
```

当然可以选择版本进行安装

```properties
sudo yum install docker-ce-<VERSION_STRING>
```



## 测试安装

启动 docker

```properties
sudo systemctl start docker
```

设置开机启动

```properties
sudo systemctl enable docker
```

运行镜像 hello-world

```properties
sudo docker run hello-world
```

重启 docker

```properties
sudo systemctl restart docker
```

停止 docker

```properties
systemctl stop docker
```





## 卸载 dorker

删除软件

```properties
sudo yum remove docker-ce
```

删掉 docker 文档

```properties
sudo rm -rf /var/lib/docker
```
