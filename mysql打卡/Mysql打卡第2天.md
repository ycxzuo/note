# Mysql打卡第二天

## 导入示例数据库

1. 登录Mysql

   ```properties
   mysql -uroot -p****
   ```

2. 创建数据库

   ```properties
   CREATE DATABASE IF NOT EXISTS yiibaidb DEFAULT CHARSET utf8 COLLATE utf8_general_ci;
   use yiibaidb;
   ```

3. 导入数据

   ```properties
   use yiibaidb;
   source D:/worksp/yiibaidb.sql;
   ```



## SQL是什么？MySQL是什么？

* SQL：结构化查询语言(Structured Query Language)简称SQL，结构化查询语言是一种数据库查询和程序设计语言，用于存取数据以及查询、更新和管理关系数据库系统
* Mysql：一种关系数据库管理系统，关系数据库将数据保存在不同的表中，而不是将所有数据放在一个大仓库内，这样就增加了速度并提高了灵活性



## 查询语句 SELECT FROM

```properties
SELECT column_name,column_name FROM table_name；
```

### 语句解释

column_name -> 要查询的列名

table_name -> 要查询的表名

### 去重语句

```mysql
 SELECT DISTINCT column_name,column_name FROM table_name；
```

去重语句会根据选择的列名进行匹配，必须选择的列的值全部相等才会被去重

### 前N个语句

```mysql
SELECT column_name,column_name FROM table_name limit 0, offset；
```

### CASE...END判断语句

用于列的操作，有两种方式

方式一：

```mysql
CASE 
            WHEN 条件1 THEN 结果1
            WHEN 条件2 THEN 结果2
            WHEN 条件3 THEN 结果3
            .........
            WHEN 条件N THEN 结果N
        END
```

方式二：

```mysql
CASE SEX 
                WHEN '1' THEN '男'
                WHEN '0'  THEN '女'
                ELSE '其他' END
```



## 筛选语句 WHERE

```mysql
SELECT DISTINCT column_name,column_name FROM table_name WHERE column_name = 'value'；
```

判断哪个列等于或者其他条件运算符

| 运算符             | 说明                                                         |
| :----------------- | :----------------------------------------------------------- |
| =                  | 等于                                                         |
| !=                 | 不等于，某些数据库系统也写作 <>                              |
| >                  | 大于                                                         |
| <                  | 小于                                                         |
| >=                 | 大于或等于                                                   |
| <=                 | 小于或等于                                                   |
| BETWEEN … AND …    | 介于某个范围之内，例：WHERE age BETWEEN 20 AND 30            |
| NOT BETWEEN …AND … | 不在某个范围之内                                             |
| IN(项1,项2,…)      | 在指定项内，例：WHERE city IN('beijing','shanghai')          |
| NOT IN(项1,项2,…)  | 不在指定项内                                                 |
| LIKE               | 搜索匹配，常与模式匹配符配合使用                             |
| NOT LIKE           | LIKE的反义                                                   |
| IS NULL            | 空值判断符                                                   |
| IS NOT NULL        | 非空判断符                                                   |
| NOT、AND、OR       | 逻辑运算符，分别表示否、并且、或，用于多个逻辑连接。 优先级：NOT > AND > OR |
| %                  | 模式匹配符，表示任意字串，例：WHERE username LIKE '%user'    |



## 分组语句 GROUP BY

```mysql
SELECT column_name, function(column_name) FROM table_name WHERE column_name = 'value' GROUP BY column_name;
```

利用函数对于查询结果进行分组操作

### 聚合函数

* AVG -> 平均数
* COUNT -> 个数
* SUN -> 总数
* MAX -> 最大值
* MIN -> 最小值

### 条件语句 HAVING

```mysql
SELECT column_name, function(column_name) FROM table_name WHERE column_name = value GROUP BY column_name HAVING column_name2 = 'value’;
```

对分组查询出来的结果再次使用条件筛选



## 排序语句 ORDER BY 

```mysql
SELECT column_name, function(column_name) FROM table_name ORDER BY column_name DESC;
```

根据某个列进行升序或者降序排序

* DESC -> 降序
* ACS -> 升序



## 函数

### 时间函数

| 函数                               | 说明             |
| :--------------------------------- | :--------------- |
| NOW()                              | 当前日期和时间   |
| CURDATE                            | 当前日期         |
| CURTIME()                          | 当前时间         |
| DATE(date)                         | 获取日期部分     |
| DATE_ADD(date, INTERVAL expr type) | 时间添加         |
| DATE_SUB(date, INTERVAL expr type) | 时间相减         |
| DATEDIFF(dat21, date2)             | 获取两个时间间隔 |
| DATEFORMAT(date, format)           | 不在某个范围之内 |



## SQL注释

### 单行注释

```mysql
 -- SELECT column_name, function(column_name) FROM table_name ORDER BY column_name DESC;
```

### 多行注释

```mysql
/*
  注释
*/
```



*作业可以查看阿里云服务器搭建的数据库*