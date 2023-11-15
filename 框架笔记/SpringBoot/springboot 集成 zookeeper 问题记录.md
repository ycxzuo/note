# springboot 集成 zookeeper 问题记录

## 环境

springboot - 2.7.8

dubbo - 3.1.11

dubbo-dependencies-zookeeper-curator5 - 3.1.11

模拟真实环境，将 windows 上的 zookeeper 迁移到虚拟机 linux 的 docker 环境



## failed to connect to zookeeper server

迁移到 linux 环境，突然出现连不上 zookeeper 的问题，springboot 报错 

```log
Caused by: java.lang.IllegalStateException: failed to connect to zookeeper server
	at org.apache.dubbo.registry.zookeeper.util.CuratorFrameworkUtils.buildCuratorFramework(CuratorFrameworkUtils.java:100)
	at org.apache.dubbo.registry.zookeeper.ZookeeperServiceDiscovery.<init>(ZookeeperServiceDiscovery.java:82)
	... 74 more
```

### 猜测一

首先怀疑是 linux 上 docker 环境的 zookeeper 的问题，于是主机使用 zookeeper 的 zkCli.cmd 连接 docker 上的 zookeeper

```sh
zkCli.cmd -server 192.168.x.x
```

结果连接服务端成功，所以 zookeeper 端没有问题



### 猜测二

那么问题应该出现在配置上了，因为之前都在本机是没有问题的，并且在本机用客户端去连接 docker 上的 zookeeper 时，响应会有一点慢。于是增加了 dubbo 中的配置超时时间，这样应该就万事大吉了

```properties
dubbo:
  registry:
    address: zookeeper://${zookeeper.address:192.168.61.80}:2181
    timeout: 60000 # 增加这个超时时间
```

然而，并没有什么用



### 猜测三

经验法无法解决，只能老老实实的根据报错堆栈信息定位报错位置

```java
CuratorFramework curatorFramework = builder.build(); # 构造

curatorFramework.start(); # 启动
curatorFramework.blockUntilConnected(BLOCK_UNTIL_CONNECTED_WAIT.getParameterValue(connectionURL),
    BLOCK_UNTIL_CONNECTED_UNIT.getParameterValue(connectionURL)); # 阻塞直至连接

if (!curatorFramework.getState().equals(CuratorFrameworkState.STARTED)) {
    throw new IllegalStateException("zookeeper client initialization failed");
}    
if (!curatorFramework.getZookeeperClient().isConnected()) {
	throw new IllegalStateException("failed to connect to zookeeper server");
}
```

CuratorFramework 构造，启动，阻塞直至连接这三步里面肯定有一个有问题，最让人怀疑的是这个阻塞的步骤，于是往下 `BLOCK_UNTIL_CONNECTED_WAIT` 这个数据从哪里来的

```java
/**
 * The enumeration for the parameters  of {@link CuratorFramework}
 *
 * @see CuratorFramework
 * @since 2.7.5
 */
public enum CuratorFrameworkParams {
    ...
    /**
     * Wait time to block on connection to Zookeeper.
     */
    BLOCK_UNTIL_CONNECTED_WAIT("blockUntilConnectedWait", 10, Integer::valueOf),

    /**
     * The unit of time related to blocking on connection to Zookeeper.
     */
    BLOCK_UNTIL_CONNECTED_UNIT("blockUntilConnectedUnit", TimeUnit.SECONDS, TimeUnit::valueOf),

    ;
```

明显，这个阻塞时间是 10s，我超时时间是 60 秒，结果这里 10s 就报连不上。但是这个值可以配置还是写死的？于是全局搜索关键字 `blockUntilConnectedWait`，发现并没有，于是我想着看看 starter 里面会不会有，这个依赖情况如下

* dubbo-spring-boot-starter
  * dubbo-spring-boot-autoconfigure
    * dubbo-spring-boot-autoconfigure-compatible

根据 springboot 的 starter 的习惯，发现配置类 `org.apache.dubbo.spring.boot.autoconfigure.DubboConfigurationProperties`

```java
@ConfigurationProperties("dubbo")
public class DubboConfigurationProperties {
    @NestedConfigurationProperty
    private Config config = new Config();
    @NestedConfigurationProperty
    private Scan scan = new Scan();
    @NestedConfigurationProperty
    private ApplicationConfig application = new ApplicationConfig();
    @NestedConfigurationProperty
    private ModuleConfig module = new ModuleConfig();
    @NestedConfigurationProperty
    private RegistryConfig registry = new RegistryConfig();
    ...
}
```

zookeeper 属于注册中心部分，所以继续查看 `org.apache.dubbo.config.RegistryConfig`

```java
/**
 * RegistryConfig
 *
 * @export
 */
public class RegistryConfig extends AbstractConfig {

    public static final String NO_AVAILABLE = "N/A";
    private static final long serialVersionUID = 5508512956753757169L;

    /**
     * Register center address
     */
    private String address;

    /**
     * Username to login register center
     */
    private String username;

    /**
     * Password to login register center
     */
    private String password;

    /**
     * Default port for register center
     */
    private Integer port;

    /**
     * Protocol for register center
     */
    private String protocol;

    /**
     * Network transmission type
     */
    private String transporter;

    private String server;

    private String client;

    /**
     * Affects how traffic distributes among registries, useful when subscribing multiple registries, available options:
     * 1. zone-aware, a certain type of traffic always goes to one Registry according to where the traffic is originated.
     */
    private String cluster;

    /**
     * The region where the registry belongs, usually used to isolate traffics
     */
    private String zone;

    /**
     * The group that services registry in
     */
    private String group;

    private String version;

    /**
     * Connect timeout in milliseconds for register center
     */
    private Integer timeout;

    /**
     * Session timeout in milliseconds for register center
     */
    private Integer session;

    /**
     * File for saving register center dynamic list
     */
    private String file;

    /**
     * Wait time before stop
     */
    private Integer wait;

    /**
     * Whether to check if register center is available when boot up
     */
    private Boolean check;

    /**
     * Whether to allow dynamic service to register on the register center
     */
    private Boolean dynamic;

    /**
     * Whether to allow exporting service on the register center
     */
    private Boolean register;

    /**
     * Whether to allow subscribing service on the register center
     */
    private Boolean subscribe;

    /**
     * The customized parameters
     */
    private Map<String, String> parameters;

    /**
     * Simple the registry. both useful for provider and consumer
     *
     * @since 2.7.0
     */
    private Boolean simplified;
    /**
     * After simplify the registry, should add some parameter individually. just for provider.
     * <p>
     * such as: extra-keys = A,b,c,d
     *
     * @since 2.7.0
     */
    private String extraKeys;

    /**
     * the address work as config center or not
     */
    private Boolean useAsConfigCenter;

    /**
     * the address work as remote metadata center or not
     */
    private Boolean useAsMetadataCenter;

    /**
     * list of rpc protocols accepted by this registry, for example, "dubbo,rest"
     */
    private String accepts;

    /**
     * Always use this registry first if set to true, useful when subscribe to multiple registries
     */
    private Boolean preferred;

    /**
     * Affects traffic distribution among registries, useful when subscribe to multiple registries
     * Take effect only when no preferred registry is specified.
     */
    private Integer weight;

    private String registerMode;

    private Boolean enableEmptyProtection;
    ...
}
```

还是没有我们需要找的目标 `blockUntilConnectedWait`，但是

```java
    /**
     * The customized parameters
     */
    private Map<String, String> parameters;
```

这个字段看注释是自定义参数，于是试着修改一下配置

```properties
dubbo:
  registry:
    address: zookeeper://${zookeeper.address:192.168.61.80}:2181
    timeout: 60000 # 增加这个超时时间
    parameters:
  		blockUntilConnectedWait: 60 # 单位为秒，也是可以设置的
```

终于项目启动成功



## 总结

遇到这样的问题起初还是很沮丧的，然后百度也给的那种版本不一致什么的答案，但是明显我这个是跑起来过的，于是只能自己摸索，花了大概 6 个多小时，终于成功解决这个问题，所以想着记录一下自己的解决过程

curator 是 zookeeper 的一层封装，可以理解就是 mybatis 和 mysql 的关系