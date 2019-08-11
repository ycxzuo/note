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

```properties
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
# 解决 hibernate 将驼峰命名不会转化为带下划线
#spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
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

### 注解 `@javax.persistence.Column`

指定该字段对于数据库字段的属性

* name
  * 对应的字段名
* unique
  * 是否是唯一
* nullable
  * 是否可为空
* insertable
  * 是否可以被持久化时插入
* length
  * 字段长度设置

### 关系注解

* 注解 `@javax.persistence.OneToOne`
  * 指定和其他实体之间的关联关系是一对一
* 注解 `@javax.persistence.OneToMany`
  * 指定和其他实体之间的关联关系是一对多
* 注解 `@javax.persistence.ManyToMany`
  - 指定和其他实体之间的关联关系是多对多
* 注解 `@javax.persistence.ManyToOne`
  - 指定和其他实体之间的关联关系是多对一

* mapperBy
  * 被关联的实体的字段名称，ManyToOne 不需要配置 ManyToMany 需要配置被关联的一边即可
* cascade
  * 级联操作
  * CascadeType.ALL
    * 所有操作
  * CascadeType.MERGE
    * 查询时会级联查出相对应的主表的数据，并进入持久化状态，更新会改变数据库的值，对应 EntityManager 的 merge 方法
  * CascadeType.PERSIST
    * 保存该实体类时会级联保存该实体类关联的实体，对应 EntityManager 的 presist 方法
  * CascadeType.REMOVE
    * 删除主表数据时，也会同时删除关联表中对应的数据，对应 EntityManager 的 remove 方法
  * CascadeType.REFRESH
    * 重新查询数据库里的最新数据，对应 EntityManager 的 refresh(object) 方法有效
  * CascadeType.DETACH
    * 分离所有相关联的实体

**在实体类中注意 Jackson 在序列化的时候循环引用，所以在一边的实体类加上注解 `@JsonIgnore*`**



## 实体类监听器和回调方法

### 实体类监听器

`@javax.persistence.EntityListeners`，该注解加载 Entity 上

### 实体类回调方法

`@javax.persistence.PrePersist`

`@javax.persistence.PreRemove`

`@javax.persistence.PreUpdate`

`@javax.persistence.PostPersist`

`@javax.persistence.PostRemove`

`@javax.persistence.PostUpdate`

`@javax.persistence.PostLoad`

*被这些注解标记的方法必须传入一个入参*



## 服务层

一般使用 EntityManager 对实体进行操作

```java
@PersistenceContext
private EntityManager manager;
```



## 持久层

持久层可以进行操作可以使用`org.springframework.data.jpa.repository.support.SimpleJpaRepository` ，例如

```java
@Repository
@Transactional
public class CustomerRepository extends SimpleJpaRepository<Customer, Long> {

    @Autowired
    public CustomerRepository(EntityManager em) {
        super(Customer.class, em);
    }
}
```

