# MyBatis 源码

源码版本 3.5.4

[源码地址](https://github.com/mybatis/mybatis-3)

[中文官方文档](https://mybatis.org/mybatis-3/zh/index.html)

## 核心类

* SqlSessionFactory
* SqlSession
* Configuration
* Mapper（非必须）
* Executor



## SqlSessionFactory 的创建

### 首先读取 MyBatis 配置文件并解析

解析 MyBatis 主要配置的类是 `org.apache.ibatis.builder.xml.XMLConfigBuilder`

调用 `org.apache.ibatis.session.SqlSessionFactoryBuilder` 的 build() 方法生成 SqlSessionFactory。入参是配置文件的字节流或者字符流，可以手动加入部分 environment 配置和 properties 配置。`org.apache.ibatis.builder.xml.XMLConfigBuilder#parse` 方法解析配置文件，并将其中的配置内容保存在 `org.apache.ibatis.session.Configuration` 中

### 配置内容

configuration（配置）

- properties（属性）
- settings（设置）
- typeAliases（类型别名）
- typeHandlers（类型处理器）
- objectFactory（对象工厂）
- objectWrapperFactory（对象包装工厂）
- reflectorFactory（反射器工厂）
- plugins（插件）
- environments（环境配置）

  - environment（环境变量）
    - transactionManager（事务管理器）
    - dataSource（数据源）
- databaseIdProvider（数据库厂商标识）
- mappers（映射器）

加载源码为

```java
private void parseConfiguration(XNode root) {
    try {
        //issue #117 read properties first
        propertiesElement(root.evalNode("properties"));
        Properties settings = settingsAsProperties(root.evalNode("settings"));
        loadCustomVfs(settings);
        loadCustomLogImpl(settings);
        typeAliasesElement(root.evalNode("typeAliases"));
        pluginElement(root.evalNode("plugins"));
        objectFactoryElement(root.evalNode("objectFactory"));
        objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
        reflectorFactoryElement(root.evalNode("reflectorFactory"));
        settingsElement(settings);
        // read it after objectFactory and objectWrapperFactory issue #631
        environmentsElement(root.evalNode("environments"));
        databaseIdProviderElement(root.evalNode("databaseIdProvider"));
        typeHandlerElement(root.evalNode("typeHandlers"));
        mapperElement(root.evalNode("mappers"));
    } catch (Exception e) {
        throw new BuilderException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
    }
}
```



