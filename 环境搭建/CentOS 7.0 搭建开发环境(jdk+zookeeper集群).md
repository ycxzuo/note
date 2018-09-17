# CentOS 7.0 搭建开发环境(jdk+zookeeper集群)

## 安装mini版CentOS 7.0

没啥好说的,下载镜像,使用net连接网络,安装时记得打开虚拟机的网络.用到的文件夹都自己创建一下.就不写出来了

## 使用静态IP

* 铭记自己的网关,可以在VMware的虚拟网络编辑器中看见

* 重启网络 ->  `service network restart`

* 查看分配的IP -> `ip addr`

* 修改网络参数 -> `vi /etc/sysconfig/network-scripts/ifcfg-ens33`

  * 重要参数如下

    ```properties
    ...
    BOOTPROTO="static"
    ...
    ONBOOT="yes"
    IPADDR0=192.168.32.134
    GATEWAY0=192.168.32.2
    PREFIXO0=24
    DNS1=8.8.8.8
    DNS2=114.114.114.114
    ```

* 重启网络 -> `service network restart`

* 尝试ping百度 -> `ping www.baidu.com`

## 安装常用命令

### 安装vim命令

* `yum -y install vim* `

### 安装telnet命令

* `yum -y install telnet-server`
* `yum -y install telnet`

### 防火墙常用命令(zookeeper需要用到)

* 启动 -> `systemctl start firewalld `
* 关闭 -> `systemctl stop firewalld`
* 查看状态 -> `systemctl status firewalld `
* 开机禁用 -> `systemctl disable firewalld `
* 开机启用 -> `systemctl enable firewalld `
* 查看所有打开的端口 -> `firewall-cmd --zone=public --list-ports `
* 打开一个端口
  *  `firewall-cmd --zone=public --add-port=80/tcp --permanent `
  * `firewall-cmd --reload `
* 关闭一个端口 -> `firewall-cmd --zone= public --remove-port=80/tcp --permanent `

## 安装JDK

* 下载jdk然后解压到目标目录`tar -zxvf jdk....`,例如`/usr/local/env/`

* 配置环境变量(带着zookeeper一起配置了,方便启动)

  * `vim /etc/profile`
  * 在末尾加入以下内容

  ```properties
  #jdk1.8
  JAVA_HOME=/usr/local/env/jdk1.8.0_181
  CLASSPATH=$JAVA_HOME/lib/
  PATH=$PATH:$JAVA_HOME/bin
  export PATH JAVA_HOME CLASSPATH
  
  # zookeeper
  export ZK_HOME=/usr/local/tools/zookeeper-3.4.13
  export PATH=$ZK_HOME/bin:$PATH
  ```

* 重启环境变量 -> `source /etc/profile`

* 查看jdk是否配置成功 -> `java -version`

## 安装zookeeper

* 下载zookeeper然后解压到目标目录`tar -zxvf zookpeer....`,例如`/usr/local/tools`
* 进入zookeeper目录,复制一份zookeeper配置文件(默认读取zoo.cfg) -> `copy conf/zoo_simple.cfg zoo.cfg`
* 直接使用命令启动zookeeper -> `sh zkServer.sh start`

## 搭建zookeeper集群

* 修改zoo.cfg文件 -> `vim conf/zoo.cfg`

* 修改内容如下

  ```properties
  ...
  dataDir=/usr/local/logs/zookeeper
  ...
  # IP写入自己三台虚拟机的IP地址
  server.1=192.168.32.128:2888:3888
  server.2=192.168.32.129:2888:3888
  server.3=192.168.32.130:2888:3888
  ```

* 在dataDir的路径下创建myid文件,并只写入上面配置中server后面的数字

  * `vim /usr/local/logs/zookeeper/myid`
  * 输入id然后保存退出

* 先关闭之前启动的zookeeper -> `sh zkServer.sh stop`

* 开启三台zookeeper即可

* 查看状态 -> `sh zkServer.sh status`看zookeeper的状态,是leader还是follower

* 连接zookeeper -> `zkCli.sh`