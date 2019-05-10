# Mysql打卡第三天

## MySQL表数据类型

* 数值类型
* 日期和时间类型
* 字符串类型



## 用SQL语句创建表

### 语句解释

```mysql
CREATE TABLE table_name (
    column1 datatyoe,
    column2 datatyoe,
    column3 datatyoe,
);
```

table_name -> 表名

column -> 列名

datatype -> 数据类型

### 设定列类型、大小、约束

#### 列类型

* 建表时设置，如上方法

* 添加列时设定

  ```mysql
  ALTER TABLE table_name ADD column_name datatype;
  ```

* 修改列类型

  ```mysql
  ALTER TABLE table_name ALTER COLUMN column_name datatype;
  ```

#### 大小

* 建表时设置，在类型后面用括号表示其最大长度

* 修改列大小

  ```mysql
  ALTER TABLE table_name modify column column_name varchar(50) ;
  ```

#### 约束

* 建表时设置，在列大小后面增加

* 修改列约束

  ```mysql
   ALTER table table_name add constraint my_un unique(column_name);
  ```

#### 设定主键

* 建表时是这，在最后加入 `PRIMARY KEY (column_name)`;

* 修改表约束为主键约束

  ```mysql
  ALTER table table_name add constraint key_name primary key(column_name);
  ```



## 用SQL语句向表中添加数据

### 语句解释

* 不指定列名

  ```mysql
  INSERT INTO table_name VALUES ('value1', 'value2', 'value3', 'value4');
  ```

  根据列名的顺序插入值，不插入的要用 `null` 填充

* 指定列名

  ```mysql
  INSERT INTO table_name (column1,column2,column3) VALUES (value1,value2,value3);
  ```

  根据列名的顺序插入值



## 用SQL语句删除表

* DELETE

  ```mysql
  DELETE FROM table_name;	
  ```

  系统一行一行地删除表的内容，表的结构还存在，不释放空间，可以回滚恢复；

* DROP

  ```mysql
  DROP TABLE table_name;
  ```

  直接删除表内容和结构，释放空间，没有备份表之前要慎用；

* TRUNCATE

  ```mysql
  TRUNCATE TABLE table_name;
  ```

  直接先删除表的内容，再建表，可以释放空间,没有备份表之前要慎用。



## MySQL别名

```mysql
SELECT column1 AS my_name FROM table_name my_table_name;
```

用 AS 或者省略



## 作业

> 项目三：超过5名学生的课（难度：简单）
>
> 创建如下所示的courses 表 ，有: student (学生) 和 class (课程)。
>
> 例如,表:
>
> +---------+------------+
>
> | student | class      |
>
> +---------+------------+‘’
>
> | A       | Math       |
>
> | B       | English    |
>
> | C       | Math       |
>
> | D       | Biology    |
>
> | E       | Math       |
>
> | F       | Computer   |
>
> | G       | Math       |
>
> | H       | Math       |
>
> | I       | Math       |
>
> | A      | Math       |
>
> +---------+------------+
>
> 
>
> 编写一个 SQL 查询，列出所有超过或等于5名学生的课。
>
> 应该输出:
>
> +---------+
>
> | class   |
>
> +---------+
>
> | Math    |
>
> +---------+
>
> Note:
>
> 学生在每个课中不应被重复计算。

```mysql
select class from courses GROUP BY class HAVING COUNT( DISTINCT student) >= 5
```



> ​	项目四：交换工资（难度：简单）
>
> 创建一个 salary表，如下所示，有m=男性 和 f=女性的值 。
>
> 例如:
>
> | id | name | sex | salary |
>
> |----|------|-----|--------|
>
> | 1  | A    | m   | 2500   |
>
> | 2  | B    | f   | 1500   |
>
> | 3  | C    | m   | 5500   |
>
> | 4  | D    | f   | 500    |
>
> 
>
> 交换所有的 f 和 m 值(例如，将所有 f 值更改为 m，反之亦然)。要求使用一个更新查询，并且没有中间临时表。
>
> 运行你所编写的查询语句之后，将会得到以下表:
>
> | id | name | sex | salary |
>
> |----|------|-----|--------|
>
> | 1  | A    | f  | 2500   |
>
> | 2  | B    | m   | 1500   |
>
> | 3  | C    | f   | 5500   |
>
> | 4  | D    | m   | 500    |

```mysql
UPDATE salary SET sex = ( CASE WHEN 'SEX' = 'f' THEN 'm' ELSE 'm' END);
```

