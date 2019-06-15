# jdbc笔记

## 数据源（DataSource）

### 定义

数据源是数据库连接的来源，通过 `javax.sql.DataSource` 接口获取

### 类型

* 通用型数据源（`javax.sql.DataSource`）
  * 应用场景：通用型数据库，本地事务，一般通过 Socket 连接
* 分布式数据源（`javax.sql.XADataSource`）
  * 应用场景：通用分布式数据库，分布式事务，一般通过 Socket 连接
* 嵌入式数据源
  * 应用场景：本地文件系统数据库，如：HSQL、H2、Derby等
  * 枚举：`org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType`



## 事务（Transaction）

### 定义

是无用于提供数据完整性，并在并发访问下确保数据视图的一致性

### 概念

#### 自动提交模式（Auto-commit mode）

##### 定义

默认情况下，当独立 SQL 语句执行后，当前事务将会自动提交

##### 触发机制

DML 执行后

DDL 执行后

SELECT 查询后结果集关闭后

存储过程执行后（如果执行返回结果记得话，待其关闭后）

#### 事务隔离级别（Transaction isolation levels）

##### 定义

事务隔离级别决定事务中执行语句中的数据可见性

##### 影响

* 脏读（dirty reads）
* 不可重复读（nonrepeatable reads）
* 幻读（phantom reads）

#### 保护点（Savepoints）

##### 定义

保护点是在事务中创建，提供细粒度事务控制

##### 场景

* 部分事务回滚
* 选择性释放



## JDBC 4.0（JSR-221）

### 核心接口

* 驱动接口
  * `java.sql.Drvier`
* 驱动管理
  * `java.sql.DriverManager`
* 数据源
  * `javax.sql.DataSource`
* 数据连接
  * `java.sql.Connection`
* 执行语句
  * `java.sql.Statement`
* 查询结果集
  * `java.sql.ResultSet`



## Statement（包括PrepareStatement）

* execute 一般用于 DDL，例如创建一个表
* executeUpdate 一般用于 DML，例如新增一条记录，返回值为修改的行数
* executeQuery 用于查询结果



## SQL语句

### SQL分类

#### DQL(Data Query Language)数据查询语言

select

#### DML(Data Manipulation Language)数据操纵语言

insert/update/delete

#### DDL(Data Definition Language)数据定义语言

create/truncate/drop

#### DCL(Data Control Language)数据控制语言

grant/rollback/commit



## SpringBoot 方法

### 数据源

SpringBoot 数据源会默认以 Bean 的方式注入，可以使用 `@Autowired` 注解拿到数据源，在 SpringBoot 2.1.5 中默认使用的 `com.zaxxer.hikari.HikariDataSource` 作为数据源，可以是增加别的数据源的依赖来改变默认的数据源

### 事务

SpringBoot 的事务有两种

* 声明式

  * 使用 `@EnableTransactionManagement` 来激活事务，其中 proxyTargetClass 默认值为 false，表示默认使用的是 JDK 的动态代理，可以更改为 CGLIB，只需要将 false 改为 true 即可
  * 在类或者方法上使用 `@Transactional` 注解标明方法或者类中的方法都是开启事务的
    * isolation：隔离级别
    * propagation：事务传播类型
      * REQUIRED：表示当前方法必须运行在事务中。如果当前事务存在，方法将会在该事务中运行。否则，会启动一个新的事务
      * SUPPORTS：表示当前方法不需要事务上下文，但是如果存在当前事务的话，那么该方法会在这个事务中运行
      * MANDATORY：表示该方法必须在事务中运行，如果当前事务不存在，则会抛出一个异常
      * REQUIRES_NEW：表示当前方法必须运行在它自己的事务中。一个新的事务将被启动。如果存在当前事务，在该方法执行期间，当前事务会被挂起
      * NOT_SUPPORTED：表示该方法不应该运行在事务中。如果存在当前事务，在该方法运行期间，当前事务将被挂起
      * NEVER：表示当前方法不应该运行在事务上下文中。如果当前正有一个事务在运行，则会抛出异常
      * NESTED：表示如果当前已经存在一个事务，那么该方法将会在嵌套事务中运行。嵌套的事务可以独立于当前事务进行单独地提交或回滚。如果当前事务不存在，那么其行为与 REQUIRED 一样
    * rollbackFor：什么异常会导致事务回滚
    * noRollbackFor：什么异常不会导致事务回滚

* 编程式

  ```java
  @Autowired
  private JdbcTemplate jdbcTemplate;
  
  @Autowired
  private PlatformTransactionManager manager;
  
  public boolean save(User user){
      TransactionDefinition definition = new DefaultTransactionDefinition();
      TransactionStatus transaction = manager.getTransaction(definition);
      boolean result = false;
      try {
          result = save(user);
          manager.commit(transaction);
      } catch(Exception e){
          manager.rollback(transaction);
      }
      return result;
  }
  ```

