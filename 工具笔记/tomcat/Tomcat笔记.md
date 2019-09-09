# Tomcat笔记

## 配置文件

### server.xml

用于存储 Host 和 Engine 的相关信息，可以切换线程池

```xml
<!-- 切换线程池的名称，最大线程数和闲置时线程数量 -->
<Executor name="tomcatThreadPool" namePrefix="yczuoxin-exec-" maxThreads="150" 
		minSpareThreads="4"/>
<Connector executor="tomcatThreadPool" connectionTimeout="20000" port="8080"
		protocol="HTTP/1.1" redirectPort="8443" />
```



### web.xml

#### 静态资源访问

静态资源访问默认走 `org.apache.catalina.servlets.DefaultServlet`，并通过 doGet 方法找到资源

```xml
<!-- 自带的 web.xml 内容 -->
<servlet>
    <servlet-name>default</servlet-name>
    <servlet-class>org.apache.catalina.servlets.DefaultServlet</servlet-class>
    <init-param>
        <param-name>debug</param-name><!-- 是否开启 debug 级别 -->
        <param-value>0</param-value>
    </init-param>
    <init-param>
        <param-name>listings</param-name><!-- 是否展示静态文本目录 -->
        <param-value>false</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>default</servlet-name>
    <url-pattern>/</url-pattern><!-- 匹配映射为 / -->
</servlet-mapping>
```



```xml
<!-- WEB-INF 下自己的 web.xml -->
<!-- 配置启动监听器 -->
<listener>
		<listener-class>com.yczuoxin.tomcat.servlet.ServletContextListenerImpl</listener-class>
</listener>

<!-- 配置启动的 servlet -->
<servlet>
    <servlet-name>JDBCTestServlet</servlet-name>
    <servlet-class>com.yczuoxin.tomcat.servlet.JDBCTestServlet</servlet-class>
</servlet>
<!-- 启动的 servlet 监听的映射 -->
<servlet-mapping>
    <servlet-name>JDBCTestServlet</servlet-name>
    <url-pattern>/jdbc/test</url-pattern>
</servlet-mapping>

<!-- 与 context.xml 中的 Resource 对应 -->
<resource-ref>
    <res-ref-name>jdbc/testDB</res-ref-name>
    <res-type>javax.sql.DataSource</res-type>
    <res-auth>Container</res-auth>
</resource-ref>

<!-- 注入值，取值方式
	Context context = new InitialContext();
	Context envContext = (Context) context.lookup("java:comp/env");
	Datasource dataSource = (DataSource) envContext.lookup("jdbc/TestDB") 
	String bean = (DataSource) envContext.lookup("Bean") 
-->
<env-entry>
    <env-entry-name>Bean</env-entry-name>
    <env-entry-type>java.lang.String</env-entry-type>
    <env-entry-value>Hello, World</env-entry-value>
</env-entry>
```



#### JSP处理

```xml
<servlet>
    <servlet-name>jsp</servlet-name>
    <servlet-class>org.apache.jasper.servlet.JspServlet</servlet-class>
    <init-param>
        <param-name>fork</param-name>
        <param-value>false</param-value>
    </init-param>
    <init-param>
        <param-name>xpoweredBy</param-name>
        <param-value>false</param-value>
    </init-param>
    <init-param>
        <param-name>development</param-name><!-- 修改 JSP 后是否可以立即生效 -->
        <param-value>false</param-value>
    </init-param>
    <load-on-startup>3</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>jsp</servlet-name>
    <url-pattern>*.jsp</url-pattern>
    <url-pattern>*.jspx</url-pattern>
</servlet-mapping>
```



### context.xml

```xml
<!-- tomcat 用 Resource 的方式配置数据源，useSSL=false 是高版本数据库需要的，其中有系统自带的全局的资源 GlobalNamingResources 标签 -->
<Resource name="jdbc/TestDB" auth="Container" type="javax.sql.DataSource"
		username="root" password="root" driverClassName="com.mysql.jdbc.Driver"
		url="jdbc:mysql://47.106.80.100:3306/?useSSL=false" />
```

