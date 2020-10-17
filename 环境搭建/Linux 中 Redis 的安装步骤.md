# Linux 中 Redis 的安装步骤

## 下载 Redis 安装包

下载方式有两种

* 使用命令下载`wget http://download.redis.io/releases/redis-6.0.6.tar.gz`
* 在官方网站上下载，并拖入 linux 服务器中



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