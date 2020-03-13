# MyBatis 的 properties 配置

## 属性

这些属性都是可外部配置且可动态替换的，既可以在典型的 Java 属性文件中配置，亦可通过 properties 元素的子元素来传递。可以以 key - value 的形式存储这些配置，使用方法如下

1. 在构造函数中传入

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(in, environment, props);
```

2. 直接使用子标签

```xml
<properties>
  <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
  <property name="url" value="jdbc:mysql://192.168.137.100:3306/yczuoxin?serverTimezone=Asia/Shanghai&amp;useUnicode=true&amp;characterEncoding=utf8&amp;useSSL=true"/>
  <property name="username" value="root"/>
  <property name="password" value="root"/>
</properties>
```

就可以在其他标签中，如 dataSource 标签中使用，冒号后面是如果没有设置时读取的默认值（ 3.4.2 开始支持）

```xml
<environments default="development">
    <environment id="development">
        <transactionManager type="JDBC"/>
        <dataSource type="POOLED">
            <property name="driver" value="${driver}"/>
            <property name="url" value="${url}"/>
            <property name="username" value="${username:root}"/>
            <property name="password" value="${password:root}"/>
        </dataSource>
    </environment>
</environments>
```

3. 配置 resource 地址

```xml
<properties resource="jdbc.properties"></properties>
```

并在 resource 文件夹下创建对应的文件 jdbc.properties

```properties
driver=com.mysql.cj.jdbc.Driver
url=jdbc:mysql://192.168.137.100:3306/yczuoxin?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&useSSL=true
username=root
password=root
```

在上述 dataSource 标签中使用

4. 配置 url 地址

```xml
<properties resource="xxx.cn"></properties>
```

会使用 url 去读取流作为配置



*如果同时出现以上几种配置的情况会按照以下优先级，resource 和 url 不能同时配置*

- 在 properties 元素体内指定的属性首先被读取。
- 然后根据 properties 元素中的 resource 属性读取类路径下属性文件或根据 url 属性指定的路径读取属性文件，并覆盖已读取的同名属性。
- 最后读取作为方法参数传递的属性，并覆盖已读取的同名属性。



## 读取配置

读取配置的源码如下

```java
private void propertiesElement(XNode context) throws Exception {
    // 判断配置文件又没有 properties 标签
    if (context != null) {
        Properties defaults = context.getChildrenAsProperties();
        String resource = context.getStringAttribute("resource");
        String url = context.getStringAttribute("url");
        // resource 和 url 同时配置会报错
        if (resource != null && url != null) {
            throw new BuilderException("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
        }
        if (resource != null) {
            defaults.putAll(Resources.getResourceAsProperties(resource));
        } else if (url != null) {
            defaults.putAll(Resources.getUrlAsProperties(url));
        }
        Properties vars = configuration.getVariables();
        // 传入的配置中有相同的配置会覆盖 resource 或者 url 配置的数据
        if (vars != null) {
            defaults.putAll(vars);
        }
        // 放入 XPathParser 和 Configuration 中
        parser.setVariables(defaults);
        configuration.setVariables(defaults);
    }
}
```

