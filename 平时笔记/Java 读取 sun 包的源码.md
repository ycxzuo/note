# Java 读取 sun 包的源码

研究 Jdk 的时候，在查看 sun 包下面的代码时，总是没有 source，于是下载了在 github 上下载了 openjdk 的源码，然后关联上 IDE，就可以看到其源码，下面分享出来

**以 jdk8 的版本进行举例**



## 下载 OpenJDK 的源码到本地

```sh
git clone https://gitclone.com/github.com/openjdk/jdk.git
cd jdk
git checkout jdk8-b120
```

或者之前 openjdk 维护的镜像其中有 jdk10 以及之前的 分支

```shell
git clone https://gitclone.com/github.com/openjdk-mirror/jdk.git
cd jdk
git checkout jdk8u/jdk8u/master
```

在 git 地址的前面加上 gitclone.com/ 可以加速



## 配置 IDE 环境

以 IDEA 进行举例，其源码放在文件的 `/jdk/src/share/classes` 下

![环境配置](https://tva1.sinaimg.cn/large/0080xEK2ly1gm9u89awppj30rq0jrjs2.jpg)

