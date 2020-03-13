# MyBatis 的 environments 解析

## 配置环境

MyBatis 可以配置成适应多种环境，这种机制有助于将 SQL 映射应用于多种数据库之中， 现实情况下有多种理由需要这么做。例如，开发、测试和生产环境需要有不同的配置；或者想在具有相同 Schema 的多个生产数据库中 使用相同的 SQL 映射。有许多类似的使用场景

每个 SqlSessionFactory 只能创建一种持有特定的环境的 SqlSession，所以不同的环境要求创建不同的 SqlSessionFactory

配置方法如下

```xml
<environments default="development">
    <environment id="development">
        <transactionManager type="JDBC"/><!-- 单独使用时配置成 MANAGED 没有事务 -->
        <dataSource type="POOLED">
            <property name="driver" value="${jdbc.driver}"/>
            <property name="url" value="${jdbc.url}"/>
            <property name="username" value="${jdbc.username}"/>
            <property name="password" value="${jdbc.password}"/>
        </dataSource>
    </environment>
</environments>
```



### transactionManager

支持两种配置方式

* JDBC：这个配置就是直接使用了 JDBC 的提交和回滚设置，它依赖于从数据源得到的连接来管理事务作用域
* MANAGED：这个配置几乎没做什么。它从来不提交或回滚一个连接，而是让容器来管理事务的整个生命周期。 默认情况下它会关闭连接，然而一些容器并不希望这样，因此需要将 closeConnection 属性设置为 false 来阻止它默认的关闭行为

#### TransactionFactory 接口

MyBatis 的事务工厂，其继承关系如下

* `org.apache.ibatis.transaction.TransactionFactory`
  * `org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory`
  * `org.apache.ibatis.transaction.managed.ManagedTransactionFactory`

```java
public interface TransactionFactory {
	// 设置 properties 属性，扩展使用
    default void setProperties(Properties props) {
        // NOP
    }
	// 从现有的链接中创建一个事务
    Transaction newTransaction(Connection conn);
	// 从现有的数据源中创建一个事务
    Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit);

}
```



### dataSource

支持的配置方式有三种

* JNDI：这个数据源的实现是为了能在如 EJB 或应用服务器这类容器中使用，容器可以集中或在外部配置数据源，然后放置一个 JNDI 上下文的引用
  * initial_context：这个属性用来在 InitialContext 中寻找上下文（即，initialContext.lookup(initial_context)）。这是个可选属性，如果忽略，那么将会直接从 InitialContext 中寻找 data_source 属性
  * data_source：这是引用数据源实例位置的上下文的路径。提供了 initial_context 配置时会在其返回的上下文中进行查找，没有提供时则直接在 InitialContext 中查找
  * env.xxx：可以传递属性给初始上下文， 例如 env.encoding=UTF8，就会把值为 UTF8 的 encoding 属性传递给初始上下文
* UNPOOLED：这个数据源的实现只是每次被请求时打开和关闭连接
  * driver：这是 JDBC 驱动的 Java 类的完全限定名（并不是 JDBC 驱动中可能包含的数据源类）
  * url：这是数据库的 JDBC URL 地址
  * username：登录数据库的用户名
  * password：登录数据库的密码
  * defaultTransactionIsolationLevel：默认的连接事务隔离级别
  * defaultNetworkTimeout：等待数据库操作完成的网络超时时间，单位是毫秒
  * driver.xxx：可以传递属性给数据库驱动， 例如 driver.encoding=UTF8，就会把值为 UTF8 的 encoding 属性传递给驱动
* POOLED：这种数据源的实现利用“池”的概念将 JDBC 连接对象组织起来，避免了创建新的连接实例时所必需的初始化和认证时间
  * 继承 UNPOOLED 的配置
  * poolMaximumActiveConnections： 在任意时间可以存在的活动（也就是正在使用）连接数量，默认值：10
  * poolMaximumIdleConnections：任意时间可能存在的空闲连接数，默认值：5
  * poolMaximumCheckoutTime：在被强制返回之前，池中连接被检出（checked out）时间，默认值：20000 毫秒
  * poolTimeToWait：这是一个底层设置，如果获取连接花费了相当长的时间，连接池会打印状态日志并重新尝试获取一个连接（避免在误配置的情况下一直安静的失败），默认值：20000 毫秒
  * poolMaximumLocalBadConnectionTolerance：这是一个关于坏连接容忍度的底层设置， 作用于每一个尝试从缓存池获取连接的线程。 如果这个线程获取到的是一个坏的连接，那么这个数据源允许这个线程尝试重新获取一个新的连接，但是这个重新尝试的次数不应该超过 `poolMaximumIdleConnections` 与 `poolMaximumLocalBadConnectionTolerance` 之和。 默认值：3 （新增于 3.4.5）
  * poolPingQuery：发送到数据库的侦测查询，用来检验连接是否正常工作并准备接受请求。默认是“NO PING QUERY SET”，这会导致多数数据库驱动失败时带有一个恰当的错误消息
  * poolPingEnabled：是否启用侦测查询。若开启，需要设置 `poolPingQuery` 属性为一个可执行的 SQL 语句（最好是一个速度非常快的 SQL 语句），默认值：false
  * poolPingConnectionsNotUsedFor：配置 poolPingQuery 的频率。可以被设置为和数据库连接超时时间一样，来避免不必要的侦测，默认值：0（即所有连接每一时刻都被侦测 — 当然仅当 poolPingEnabled 为 true 时适用）

#### DataSourceFactory 接口

MyBatis 的数据源工厂，其继承关系如下

* `org.apache.ibatis.datasource.DataSourceFactory`
  * `org.apache.ibatis.datasource.jndi.JndiDataSourceFactory`
  * `org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory`
    * `org.apache.ibatis.datasource.pooled.PooledDataSourceFactory`

#### UnpooledDataSourceFactory 类

UnpooledDataSourceFactory 获取

```java
public class UnpooledDataSource implements DataSource {

    private ClassLoader driverClassLoader;
    private Properties driverProperties;
    private static Map<String, Driver> registeredDrivers = new ConcurrentHashMap<>();

    private String driver;
    private String url;
    private String username;
    private String password;

    private Boolean autoCommit;
    private Integer defaultTransactionIsolationLevel;
    private Integer defaultNetworkTimeout;

    static {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            registeredDrivers.put(driver.getClass().getName(), driver);
        }
    }

    public UnpooledDataSource() {
    }

    public UnpooledDataSource(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public UnpooledDataSource(String driver, String url, Properties driverProperties) {
        this.driver = driver;
        this.url = url;
        this.driverProperties = driverProperties;
    }

    public UnpooledDataSource(ClassLoader driverClassLoader, String driver, String url, String username, String password) {
        this.driverClassLoader = driverClassLoader;
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public UnpooledDataSource(ClassLoader driverClassLoader, String driver, String url, Properties driverProperties) {
        this.driverClassLoader = driverClassLoader;
        this.driver = driver;
        this.url = url;
        this.driverProperties = driverProperties;
    }
    
      private Connection doGetConnection(String username, String password) throws SQLException {
    Properties props = new Properties();
    if (driverProperties != null) {
      props.putAll(driverProperties);
    }
    if (username != null) {
      props.setProperty("user", username);
    }
    if (password != null) {
      props.setProperty("password", password);
    }
    return doGetConnection(props);
  }

    private Connection doGetConnection(Properties properties) throws SQLException {
        initializeDriver();
        // 获取到连接
        Connection connection = DriverManager.getConnection(url, properties);
        configureConnection(connection);
        return connection;
    }
	...
}
```



## 读取配置

```java
private void environmentsElement(XNode context) throws Exception {
    if (context != null) {
        if (environment == null) {
            environment = context.getStringAttribute("default");
        }
        for (XNode child : context.getChildren()) {
            String id = child.getStringAttribute("id");
            if (isSpecifiedEnvironment(id)) {
                TransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
                DataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
                DataSource dataSource = dsFactory.getDataSource();
                Environment.Builder environmentBuilder = new Environment.Builder(id)
                    .transactionFactory(txFactory)
                    .dataSource(dataSource);
                configuration.setEnvironment(environmentBuilder.build());
            }
        }
    }
}

private TransactionFactory transactionManagerElement(XNode context) throws Exception {
    if (context != null) {
        String type = context.getStringAttribute("type");
        Properties props = context.getChildrenAsProperties();
        TransactionFactory factory = (TransactionFactory) resolveClass(type).getDeclaredConstructor().newInstance();
        factory.setProperties(props);
        return factory;
    }
    throw new BuilderException("Environment declaration requires a TransactionFactory.");
}

private DataSourceFactory dataSourceElement(XNode context) throws Exception {
    if (context != null) {
        String type = context.getStringAttribute("type");
        Properties props = context.getChildrenAsProperties();
        DataSourceFactory factory = (DataSourceFactory) resolveClass(type).getDeclaredConstructor().newInstance();
        factory.setProperties(props);
        return factory;
    }
    throw new BuilderException("Environment declaration requires a DataSourceFactory.");
}
```



## 自定义

### datasource

首先要在 POM 中引入 Druid 的依赖

配置文件修改

```xml
<environments default="development">
    <environment id="development">
        <transactionManager type="JDBC"/><!-- 单独使用时配置成 MANAGED 没有事务 -->
        <dataSource type="com.yczuoxin.datasource.DruidDataSourceFactory">
            <property name="driver" value="${jdbc.driver}"/>
            <property name="url" value="${jdbc.url}"/>
            <property name="username" value="${jdbc.username}"/>
            <property name="password" value="${jdbc.password}"/>
        </dataSource>
    </environment>
</environments>
```

自定义 Druid 数据源

```java
public class DruidDataSourceFactory implements DataSourceFactory {
    private Properties props;

    @Override
    public void setProperties(Properties props) {
        this.props = props;
    }

    @Override
    public DataSource getDataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(this.props.getProperty("url"));
        druidDataSource.setPassword(this.props.getProperty("password"));
        druidDataSource.setUsername(this.props.getProperty("username"));
        druidDataSource.setDriverClassName(this.props.getProperty("driver"));
        try {
            druidDataSource.init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return druidDataSource;
    }
}
```

