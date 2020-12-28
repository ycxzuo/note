# Linux 中 Redis 的安装步骤

## 下载 Redis 安装包

下载方式有两种

* 使用命令下载`wget http://download.redis.io/releases/redis-6.0.8.tar.gz`
* 在[官方网站](https://redis.io/download)上下载，并拖入 linux 服务器中



## 先安装 make 插件

这个 Redis 安装的必要条件

`yum install -y gcc make`



### 如果需要的话，要升级 gcc 版本，默认是 4.8.5，

```sh
yum -y install centos-release-scl
yum -y install devtoolset-9-gcc devtoolset-9-gcc-c++ devtoolset-9-binutils
scl enable devtoolset-9 bash
echo "source /opt/rh/devtoolset-9/enable" >> /etc/profile
```



## 编译 Redis

```shell
cd redis-6.0.6
make
```



## 修改配置文件

```shell
vim redis.conf
```

* 修改 Redis 是可以被外网访问的
  * 注释掉 `bind 172.0.0.1`
* 修改 Redis 是守护线程启动
  * 修改 `daemonize` 由 no 改为 yes
* 修改 Redis 是需要密码验证的
  * 去掉 `requirepass` 的注释，并修改为自己需要的密码



## 启动 Redis server

```cmd
cd src
src/redis-server ./redis.conf
```





# Docker 环境中安装启动 Redis

## 拉取镜像

```sh
docker pull redis:6.0.8
```



## 创建 Redis 配置文件和数据存储文件作为挂载文件

```sh
mkdir -p mkdir -p /usr/local/tools/dockers/redis
mkdir -p mkdir -p /usr/local/tools/dockers/data
```



## 在网上下载 Redis 的 redis.conf 配置，然后移到 /usr/local/tools/dockers/redis 路径下

需要注释掉 bind 网关，并且设置密码，但是不能设置 Redis 是守护线程启动，不然会启动不了 Redis 并且没有报错日志



## 用 Docker 启动 Redis

```sh
docker run -p 6379:6379 --name redis6 -v /usr/local/tools/docker/redis/redis.conf:/usr/local/etc/redis/redis.conf -v /usr/local/tools/docker/redis/data:/data -d redis redis-server /usr/local/etc/redis/redis.conf
```



## 以 client 方式进入 Docker 与 Redis 互动

```sh
docker exec -it redis6379 redis-cli -a 123456
```

其中 `123456` 换成自己 Redis 设置的密码即可

