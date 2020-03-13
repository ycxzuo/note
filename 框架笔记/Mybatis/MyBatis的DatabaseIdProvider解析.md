

# MyBatis 的 DatabaseIdProvider 解析

## 数据库厂商标识

MyBatis 可以根据不同的数据库厂商执行不同的语句，这种多厂商的支持是基于映射语句中的 `databaseId` 属性。 MyBatis 会加载不带 `databaseId` 属性和带有匹配当前数据库 `databaseId` 属性的所有语句。 如果同时找到带有 `databaseId` 和不带 `databaseId` 的相同语句，则后者会被舍弃

配置方法

```xml
<environments default="development">
    <environment id="development">
        <transactionManager type="JDBC"/>
        <dataSource type="POOLED">
            <property name="driver" value="${jdbc.driver}"/>
            <property name="url" value="${jdbc.url}"/>
            <property name="username" value="${jdbc.username}"/>
            <property name="password" value="${jdbc.password}"/>
        </dataSource>
    </environment>
    <environment id="developmentOracle">
        <transactionManager type="JDBC"/>
        <dataSource type="POOLED">
            <property name="driver" value="${oracle.driver}"/>
            <property name="url" value="${oracle.url}"/>
            <property name="username" value="${oracle.username}"/>
            <property name="password" value="${oracle.password}"/>
        </dataSource>
    </environment>
</environments>

<databaseIdProvider type="com.yczuoxin.databaseidorovider.MyDatabaseIdProvider">
    <property name="Oracle" value="oracle"/>
    <property name="MySQL" value="mysql"/>
</databaseIdProvider>
```



### DatabaseIdProvider 接口

```java
public interface DatabaseIdProvider {

    default void setProperties(Properties p) {
        // NOP
    }
	// 返回数据源的产品名称
    String getDatabaseId(DataSource dataSource) throws SQLException;
}
```

其继承关系如下

* `org.apache.ibatis.mapping.DatabaseIdProvider`
  * `org.apache.ibatis.mapping.VendorDatabaseIdProvider`



### VendorDatabaseIdProvider 实现类

```java
public class VendorDatabaseIdProvider implements DatabaseIdProvider {

    private Properties properties;

    @Override
    public String getDatabaseId(DataSource dataSource) {
        // 空值判定
        if (dataSource == null) {
            throw new NullPointerException("dataSource cannot be null");
        }
        try {
            // 获取数据源的产品名称
            return getDatabaseName(dataSource);
        } catch (Exception e) {
            LogHolder.log.error("Could not get a databaseId from dataSource", e);
        }
        return null;
    }

    @Override
    public void setProperties(Properties p) {
        this.properties = p;
    }

    private String getDatabaseName(DataSource dataSource) throws SQLException {
        String productName = getDatabaseProductName(dataSource);
        if (this.properties != null) {
            for (Map.Entry<Object, Object> property : properties.entrySet()) {
                if (productName.contains((String) property.getKey())) {
                    return (String) property.getValue();
                }
            }
            // no match, return null
            return null;
        }
        return productName;
    }

    private String getDatabaseProductName(DataSource dataSource) throws SQLException {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            DatabaseMetaData metaData = con.getMetaData();
            return metaData.getDatabaseProductName();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    // ignored
                }
            }
        }
    }

    private static class LogHolder {
        private static final Log log = LogFactory.getLog(VendorDatabaseIdProvider.class);
    }

}
```

查询数据源产品名称底层调用的是 `java.sql.DatabaseMetaData#getDatabaseProductName` 方法，在 mapper.xml 中选择对应的 `databaseId` 就会将信息保存在`org.apache.ibatis.mapping.MappedStatement` 对象中并保存在 Configuration 的 `Map<String, MappedStatement> mappedStatements` 中保存



## 配置读取

```java
private void databaseIdProviderElement(XNode context) throws Exception {
    DatabaseIdProvider databaseIdProvider = null;
    if (context != null) {
        String type = context.getStringAttribute("type");
        if ("VENDOR".equals(type)) {
            type = "DB_VENDOR";
        }
        Properties properties = context.getChildrenAsProperties();
        // DB_VENDOR 类型创建 VendorDatabaseIdProvider 对象
        databaseIdProvider = (DatabaseIdProvider) resolveClass(type).getDeclaredConstructor().newInstance();
        databaseIdProvider.setProperties(properties);
    }
    Environment environment = configuration.getEnvironment();
    if (environment != null && databaseIdProvider != null) {
        String databaseId = databaseIdProvider.getDatabaseId(environment.getDataSource());
        configuration.setDatabaseId(databaseId);
    }
}
```



## 自定义

```java
public class MyDatabaseIdProvider implements DatabaseIdProvider {
    private static final Log log = LogFactory.getLog(MyDatabaseIdProvider.class);
    private Properties properties;

    @Override
    public void setProperties(Properties p) {
        this.properties = p;
    }

    @Override
    public String getDatabaseId(DataSource dataSource) throws SQLException {
        Connection connection = dataSource.getConnection();
        DatabaseMetaData metaData = connection.getMetaData();
        String databaseProductName = metaData.getDatabaseProductName();
        log.debug("Current DataBase Product Name is: " + databaseProductName);
        for (Object key : properties.keySet()) {
            if (key.equals(databaseProductName)) {
                log.debug("Find a matched property value: " + properties.get(key));
                return (String) properties.get(key);
            }
        }
        return null;
    }
}
```

在配置中引入

```xml
<databaseIdProvider type="com.gupaoedu.databaseprovider.MyDatabaseIdProvider">
    <property name="DB2" value="db2"/>
    <property name="Oracle" value="oracle" />
</databaseIdProvider>
```

