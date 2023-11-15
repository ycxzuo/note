# Docker 搭建 Zookeeper

首先创建 docker 给 zookeeper 日志目录和数据记录的挂载文件夹

```sh
mkdir /usr/volume/zk/data /usr/volume/zk/log
```

拉取 zookeeper 镜像

```sh
docker pull zookeeper
```

启动 zookeeper 镜像

```sh
docker run -p 2181:2181 -v /usr/volume/zk/data:/data -v /usr/volume/zk/log:/datalog --env TZ=Asia/Shanghai -d --name zk1 zookeeper
```

查看 zookeeper 节点

```sh
docker exec -it zk /bin/bash
cd bin
zkCli.sh
```

