# Zookeepper 3.4.13 windows 安装及服务注册

## 下载

[官方网站](http://zookeeper.apache.org/releases.html)

下载后解压安装包即可

## 修改默认配置

将解压目录下的conf文件夹下的zoo_simple.cfg改成zookeeper默认读取的配置文件zoo.cfg,其内容如下:

```properties
# The number of milliseconds of each tick
tickTime=2000
# The number of ticks that the initial 
# synchronization phase can take
initLimit=10
# The number of ticks that can pass between 
# sending a request and getting an acknowledgement
syncLimit=5
# the directory where the snapshot is stored.
# do not use /tmp for storage, /tmp here is just 
# example sakes.
dataDir=/tmp/zookeeper
# the port at which the clients will connect
clientPort=2181
# the maximum number of client connections.
# increase this if you need to handle more clients
#maxClientCnxns=60
#
# Be sure to read the maintenance section of the 
# administrator guide before turning on autopurge.
#
# http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_maintenance
#
# The number of snapshots to retain in dataDir
#autopurge.snapRetainCount=3
# Purge task interval in hours
# Set to "0" to disable auto purge feature
#autopurge.purgeInterval=1
```

### 普通配置

* `tickTime` -> 服务器之间或客户端与服务器之间发送心跳检测的间隔时间
* `initLimit` -> 此配置表示，允许follower连接并同步到leader的初始化连接时间,以tickTime为单位.当初始化连接时间超过该值,则表示连接失败.
* `syncLimit` -> leader与follower之间发送消息时,请求和应答时间长度,以tickTime为单位.如果follower在设置时间内不能与leader通信,那么该follower将会被丢弃.
* `dataDir` -> 存储内存中数据的位置
* `dataLogdDir` -> 把事务日志写在指定的目录中而不是`dataDir`中
* `maxClientCnxns` -> 限制连接到zookeeper客户端的数量(根据IP判断)
* `minSessionTimeout` -> 最小的会话超时(defualt = 2 * tickTime)
* `maxSessionTimeout` -> 最大的会话超时(defualt = 20 * tickTime)

### 集群配置

增加配置

```properties
server.1=192.168.211.1:2888:3888  
server.2=192.168.211.2:2888:3888
```

* server配置
  * 在配置文件后面增加`server.n=${IP}:${leader选举端口}:${服务器通讯端口}`如上示例
* myid文件
  * 在dataDir目录下保存这个文件,里面就一个数据就是n值