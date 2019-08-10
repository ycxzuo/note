# JPA+SpringBoot学习笔记

*使用的 Spring Boot 版本是 5.1.6*

## 必要的依赖

```xml
<!-- JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<!-- MYSQL -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
```

## 配置

其使用的默认是 hibernate 框架，然后配置数据库连接信息（`DataSourceProperties`）和 JPA 的配置（`JpaProperties` + `HibernateProperties`）

```pro
# 数据源
spring.datasource.url=jdbc:mysql://47.106.80.100:3306/jpa
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 数据库类型
spring.jpa.database=mysql
# JPA 启动时初始化架构，检测功能
spring.jpa.generateDdl=true
# hibernate 会在启动时自动删除并创建表 none 关闭
spring.jpa.hibernate.ddl-auto=create-drop
```

## 启动类

### 注解 `@EnableJpaRepositories`

开启 SpringBoot 的 JPA 功能

## entity实体

### 注解  `@javax.persistence.Entity`

原来的注解 `org.hibernate.annotations.Entity` 已经被废弃掉，指定该类是一个持久化的实体aa

### 注解 `@javax.persistence.Table `

用来标名该实体对应的数据库表的表名

### 注解 `@javax.persistence.Acess`

用来表明该类的映射是基于字段（`AccessType.FIELD`）还是方法（`AccessType.PROPERTY`）

### 注解 `@javax.persistence.GeneratedValue`

指定实体类的主键的增长类型

* GenerationType.TABLE
  * 使用一个特定的数据库表格来保存主键
* GenerationType.SEQUENCE
  * 根据底层数据库的序列来生成主键，条件是数据库支持序列
* GenerationType.IDENTITY
  * 主键由数据库自动生成（主要是自动增长型）
* GenerationType.AUTO
  * 主键由程序控制

### 注解 `@javax.persistence.Id`

指定实体类的主键