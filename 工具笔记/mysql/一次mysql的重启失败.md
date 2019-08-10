# 一次mysql的重启失败

近期阿里云一直提示修复漏洞，然后重启了mysql，导致 mysql 无法启动，看到提示

 ```properties
Can't connect to local MySQL server through socket '/tmp/mysql.sock'
 ```

是否存在，去目录看，果真不在了，那么只能想法办重新构建一个 mysql.sock 文件了。

### 解决方式

重新启动 mysql 

```
sudo /etc/init.d/mysql start
```

 提示错误 

```properties
The server quit without updating PID file (/[FAILED]mysqld/mysqld.pid)
```

。查看日志文件

 ```properties
cat /var/log/mysqld.log | tail -n 200
 ```

发现报错信息为

 ```properties
/usr/local/tools/mysql/bin/mysqld: Can't create/write to file '/var/run/mysqld/mysqld.pid' (Errcode: 2 - No such file or directory)
 ```

发现根本没有 mysqld 这个文件夹，于是新建文件夹，并赋予权限

```properties
cd /var/run/
mkdir mysqld
chown -R mysql /var/run/mysqld
chgrp -R mysql /var/run/mysqld
```

然后启动 mysql

```properties
sudo /etc/init.d/mysql start
```

成功



### 反思

那么 mysql.sock 是做什么的呢？

mysql.sock 是 mysql 的主机和客户机在同一host上的时候，使用 unix domain socket 做为通讯协议的载体，它比 tcp 快。

Mysql 有两种连接方式： 

（1）TCP/IP 

（2）socket 

对 mysql.sock 来说，其作用是程序与 mysqlserver 处于同一台机器，发起本地连接时可用。 

例如你无须定义连接host的具体 IP 得，只要为空或 localhost 就可以。 

在此种情况下，即使你改变 mysql 的外部 port 也是一样可能正常连接。 

因为你在 my.ini 中或 my.cnf 中改变端口后，mysql.sock 是随每一次 mysql server 启动生成的。已经根据你在更改完 my.cnf 后重启 mysq l时重新生成了一次，信息已跟着变更。

那么对于外部连接，必须是要变更 port 才能连接的。 