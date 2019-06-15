# mybatis 笔记

## [配置](http://www.mybatis.org/mybatis-3/zh/configuration.html#properties)

* 全局 XML 配置文件

  * 全局 XML 配置文件包含影响 MyBatis 行为的设置和属性

    * properties 属性，占位符的作用

    * setting 设置，修改 Mybatis 运行时行为

    * typeAliases 类型别名，一般用更短的名称代替

    * typeHanders 类型处理器，用于将预编译语句或结果集转化为 Java 类型，如果需要使用Java8新增的Date API，需要引入依赖 

      ```xml
      <dependency>
          <groupId>org.mybatis</groupId>
          <artifactId>mybatis-typehandlers-jsr310</artifactId>
          <version>1.0.2</version>
      </dependency>
      ```

    * objectFactory 对象工厂，提供构造器创建对象

    * plugins 插件，用拦截器来拦截映射语句

    * environments 环境，允许配置多个环境，通过环境信息切换关联的 `SqlSessionFactory`，类似 MAVEN 或者 Spring 中的profile

    * databaseIdProvider 数据库标识提供商，根据 SQL 厂商，提供不同的实现

    * mappers SQL 映射文件

* SQL Mapper XML 配置文件

  * 用于映射 SQL 模板语句与 Java 类型的配置

* SQL Mapper Annotation

  * SQL Mapper Annotation 是 Java Annotation 的方式替代 XML 配置文件



## mybatis配置

### XML 定义

#### 文档类型约束方式

* DTD：Document Type Definition
* http://mybatis.org/dtd/mybatis-3-config.dtd

### API 接口

`org.apache.ibatis.session.Configuration`

#### properties 属性

组装 API 接口

`org.apache.ibatis.session.Configuration#variables`

填充配置

`org.apache.ibatis.builder.xml.XMLConfigBuilder(XPathParser,String,Properties)`

#### settings 设置

组装 API 接口

`org.apache.ibatis.session.Configuration#setXXX(*)`

填充配置

`org.apache.ibatis.builder.xml.XMLConfigBuilder#settingsElement(Properties)`

#### typeAliases 类型别名

组装 API 接口

`org.apache.ibatis.session.Configuration#typeAliasRegistry`

API 定义

`org.apache.ibatis.type.TypeAliasRegistry`

填充配置

`org.apache.ibatis.builder.xml.XMLConfigBuilder#typeAliasElement(XNode)`

#### typeHandlers 类型处理器

组装 API 接口

`org.apache.ibatis.session.Configuration#typeHandlerRegistry`

API 定义

`org.apache.ibatis.type.TypeHandlerRegistry`

填充配置

`org.apache.ibatis.builder.xml.XMLConfigBuilder#typeHandlerElement(XNode)`

#### objectFactory 对象工厂

组装 API 接口

`org.apache.ibatis.session.Configuration#objectFactory`

API 定义

`org.apache.ibatis.reflection.factory.ObjectFactory`

填充配置

`org.apache.ibatis.builder.xml.XMLConfigBuilder#objectFactoryElement(XNode)`

#### plugins 插件

组装 API 接口

`org.apache.ibatis.session.Configuration#interceptorChain`

API 定义

`org.apache.ibatis.plugin.Interceptor`

填充配置

`org.apache.ibatis.builder.xml.XMLConfigBuilder#pluginElement(XNode)`

#### environments 环境

组装 API 接口

`org.apache.ibatis.session.Configuration#environment`

API 定义

`org.apache.ibatis.mapping.Environment`

填充配置

`org.apache.ibatis.builder.xml.XMLConfigBuilder#environmentsElement(XNode)`

#### databaseIdProvider 数据库标识提供商

组装 API 接口

`org.apache.ibatis.session.Configuration#databaseId`

填充配置

`org.apache.ibatis.builder.xml.XMLConfigBuilder#databaseIdProviderElement(XNode)`

#### mappers SQL 映射文件

组装 API 接口

`org.apache.ibatis.session.Configuration#mapperRegistry`

API 定义

`org.apache.ibatis.binding.MapperRegistry`

填充配置

`org.apache.ibatis.builder.xml.XMLConfigBuilder#mapperElement(XNode)`



### 核心 API

#### SqlSessionFactoryBuilder

`org.apache.ibatis.session.SqlSessionFactoryBuilder` ，`SqlSessionFactory` 构造器，通过重载 build 方法，控制实例行为，其中方法参数如下

`SqlSessionFactoryBuilder#build(Reader, String, Properties)` 

* Reader：MyBatis 全局配置流（`java.io.InputStream`、`java.io.Reader`）
* String：MyBatis 环境名称（environment）
* Properties：MyBatis 属性（`java.util.Properties`）

相关 API

* 配置构造器：`org.apache.ibatis.builder.xml.XMLConfigBuilder`
* Mybatis 配置：`org.apache.ibatis.session.Configuration`
* Mybatis 环境：`org.apache.ibatis.mapping.Environment`

#### SqlSessionFactory

`org.apache.ibatis.session.SqlSessionFactory`，`SqlSession` 工厂，通过重载 openSession 方法，控制实例特性，其中方法参数如下

* boolean autoCommit：是否需要自动提交
* Connection connection：JDBC 数据库连接
* ExecutorType execType：Mybatis SQL 语句执行器类型
* TransactionIsolationLevel level：Mybatis 事务隔离级别

#### SqlSession

`org.apache.ibatis.session.SqlSession`，Mybatis SQL 会话对象，类似于 JDBC Connection，职责如下

* 封装 `java.sql.Connection`
* 屏蔽 `java.sql.Statement` 及派生接口的细节
* 映射 `java.sql.ResultSet` 到 Java 类型
* 事务控制
* 缓存
* 代理映射（Mappper）

