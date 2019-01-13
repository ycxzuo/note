# 阿里云安装 ElasticSearch 笔记

## 必要条件

环境需要JDK 8 及以上

## 下载解压安装包

[详细步骤](https://www.elastic.co/guide/en/elasticsearch/reference/6.5/zip-targz.html)

`wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.5.4.zip`

`wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.5.4.zip.sha512`

如果没有安装`shasum`命令需要执行下面的命令 -> 验证文件是否完整

`yum install perl-Digest-SHA`

`shasum -a 512 -c elasticsearch-6.5.4.zip.sha512`

`unzip elasticsearch-6.5.4.zip`

`cd elasticsearch-6.5.4/`

## 创建用户并授权

如果直接使用`root`去启动 ElasticSearch 会报错

```properties
org.elasticsearch.bootstrap.StartupException: java.lang.RuntimeException: can not run elasticsearch as root
```

因为官方不建议使用 root 启动，所以要新建用户并授权

`useradd elasticUser`

给 elasticUser 授权

`chown -R elasticUser:elasticUser /usr/local/tools/elasticsearch-6.5.4`

切换到 elasticUser 用户

`su elasticUser`

启动 ElasticSearch

`cd /usr/local/tools/elasticsearch-6.5.4 `

`./bin/elasticsearch`

此时可以启动 ElasticSearch，可能报出

```properties
JavaHotSpot(TM) 64-Bit Server VM warning: INFO: error='Cannotallocate memory' (errno=12)
```

这是说明 Java 虚拟机的内存不够，此时可以修改 jvm.options 文件

`vim config/jvm.options`

将

```pro
-Xms1g
-Xmx1g
```

修改为 500M或者更小，根据机器自定义配置

```properties
-Xms500m
-Xmx500m
```



但是外网是无法访问的，此时需要修改配置文件

`vim config/elasticsearch.yml`

在尾部添加

```properties
network.host: 0.0.0.0
discovery.zen.ping.unicast.hosts: ["0.0.0.0"]
```

此时再启动 ElasticSearch 发现会报错

```properties
ERROR: [2] bootstrap checks failed
# maxfile descriptors为最大文件描述符，设置其大于65536即可
[1]: max file descriptors [65535] for elasticsearch process is too low, increase to at least [65536]
# max_map_count文件包含限制一个进程可以拥有的VMA(虚拟内存区域)的数量，系统默认是65530，修改成262144
[2]: max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
```

此时需要切换 root 用户更改两个配置文件

`su root`

然后输入 root 用户的密码，开始更改文件

对于 [1] 修改 `/etc/security/limits.conf`文件，此文件是 linux 资源使用配置文件，可以用`ulimit -a`查看，soft 是一个警告值，而 hard 则是一个真正意义的阀值，`*`表示所有用户

`vim /etc/security/limits.conf`

在最底部增加

```properties
# 打开文件的最大数目
* - nofile 65536
# 最大锁定内存地址空间
* - memlock unlimited
```



对于 [2] 修改 `/etc/sysctl.conf`文件，此文件是 linux 系统控制文件，作用是用于在内核运行时动态地修改内核的运行参数，可用的内核参数在目录`/proc/sys`中。它包含一些 TCP/IP 堆栈和虚拟内存系统的高级选项。

`vim /etc/sysctl.conf`

在最底部增加

```properties
# 文件包含限制一个进程可以拥有的 VMA（虚拟内存区域）的数量，默认是 65530
vm.max_map_count=262144
```

*修改此配置需要重启机器才能生效*



## 重要配置文件 elaticsearch.yml

### cluster.name: elasticsearch

配置 elasticsearch 的集群名称，默认是 elasticsearch。elasticsearch 会自动发现在同一网段下的集群名为elasticsearch 的主机，如果在同一网段下有多个集群，就可以用这个属性来区分不同的集群。生成环境时建议更改

### node.name: node-1

节点名，默认随机指定一个 name 列表中名字，该列表在 elasticsearch 的 jar 包中`config`文件夹里`name.txt`文件中，其中有很多作者添加的有趣名字，大部分是漫威动漫里面的人物名字。生成环境中建议更改以能方便的指定集群中的节点对应的机器

### node.attr.rack: r1

指定节点的部落属性，这是一个比集群更大的范围

### node.master: true

指定该节点是否有资格被选举成为`node`，默认是`true`，elasticsearch 默认集群中的第一台启动的机器为`master`，如果这台机挂了就会重新选举`master`

### node.data: true

指定该节点是否存储索引数据，默认`true`。如果节点配置`node.master: false`且`node.data: false`，则该节点将起到负载均衡的作用

### index.number_of_shards: 5

设置默认索引分片个数，默认为`5`片。索引分片对ES的查询性能有很大的影响，在应用环境，应该选择适合的分片大小

### index.number_of_replicas: 1

设置默认索引副本个数，默认为`1`个副本。此处的1个副本是指`index.number_of_shards`的一个完全拷贝；默认 5 个分片 1 个拷贝；即总分片数为 10，是索引创建后一次生成的,后续不可更改设置 ，`number_of_replicas` 是可以通过`API`去实时修改设置的

### path.conf: /path/to/conf

设置配置文件的存储路径，默认是 ElasticSearch 根目录下的`config`文件夹

### path.data: /path/to/data

设置索引数据的存储路径，默认是 ElasticSearch 根目录下的 data 文件夹，可以设置多个存储路径，用逗号隔开

### path.logs: /path/to/logs

设置日志文件的存储路径，默认是 ElasticSearch 根目录下的logs文件夹

### path.plugins: /path/to/plugins

设置插件的存放路径，默认是 ElasticSearch 根目录下的 plugins 文件夹

### bootstrap.memory_lock: true

设置为`true`来锁住内存,防止 ElasticSearch 内存被交换出去。因为当 jvm 开始内存扩容或者收缩时 ElasticSearch 的效率会降低，所以要保证它不`swap`，可以把`ES_MIN_MEM`和`ES_MAX_MEM`两个环境变量设置成同一个值，并且保证机器有足够的内存分配给 ElasticSearch 。同时也要允许 ElasticSearch 的进程可以锁住内存，linux 下可以通过`ulimit -l unlimited`命令锁住内存

### network.host: 192.168.0.1

这个参数是用来同时设置`bind_host`和`publish_host`两个参数

### network.bind_host: 192.168.0.1

只有本机可以访问http接口

### network.publish_host: 192.168.0.1

设置其它节点和该节点交互的ip地址,如果不设置它会自动设置,值必须是个真实的ip地址

### http.port: 9200

设置对外服务的`http`端口，默认为`9200`

### transport.tcp.port

设置 TCP 传输端口，这个端口非常重要，节点之前传输数据也是走这个`TCP`端口，官方提供的`ES JAVA API`也是通过这个端口传输数据的

### transport.tcp.compress: true

设置是否压缩TCP传输时的数据，默认为`false`，不压缩

### http.max_content_length: 100mb

设置请求内容的最大容量,默认100mb

### http.enabled: false

使用`http`协议对外提供服务，默认为`true`，开启

### 使用 head 等插件监控集群信息，需要打开以下配置项

```properties
http.cors.enabled: true
http.cors.allow-origin: "*"
http.cors.allow-credentials: true
```

### gateway.recover_after_nodes: 3

设置集群中 N 个节点启动时进行数据恢复，默认为`1`

### discovery.zen.minimum_master_nodes: 1

设置这个参数来保证集群中的节点可以知道其它 N 个有`master`资格的节点,通过配置这个参数来防止集群脑裂现象 (集群总节点数量/2)+1。默认为`1`，对于大的集群来说，可以设置大一点的值（2~4）

### discovery.zen.ping.timeout: 3s

设置集群中自动发现其它节点时`ping`连接超时时间，默认为`3`秒，对于比较差的网络环境可以高点的值来防止自动发现时出错

### discovery.zen.ping.multicast.enabled: false

设置是否打开多播发现节点，默认是`true`

### discovery.zen.ping.unicast.hosts: [“host1”, “host2”]

设置集群中master节点的初始列表，可以通过这些节点来自动发现新加入集群的节点，可以在`host`后面加上端口`host:port`

### action.destructive_requires_name: true

设置是否可以通过正则或者_all删除或者关闭索引库，默认true表示必须需要显式指定索引库名称，生产环境建议设置为true，删除索引库的时候必须显式指定，否则可能会误删索引库中的索引库