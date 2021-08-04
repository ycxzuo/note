# CentOS7 环境 Docker 创建 ElasticSearch + Kibana + ElasticSearch-head

## 准备工作

拉取 docker 镜像

```sh
docker pull elasticsearch:7.14.0
```

```sh
docker pull kibana:7.14.0
```

```sh
docker pull mobz/elasticsearch-head:5
```



创建自定义的网络

```sh
docker network create elasticsearch
```



## 安装 ES

设置 max_map_count，默认是 65530，无法启动 ES

```sh
sysctl -w vm.max_map_count=262144
```



启动镜像

```sh
docker run --name elasticsearch -d --net elasticsearch -e ES_JAVA_OPTS="-Xms512m -Xmx512m" -e "discovery.type=single-node" -p 9200:9200 -p 9300:9300 elasticsearch:7.14.0
```



进入容器增加跨域能力

```sh
docker exec -it elasticsearch /bin/bash
```

```sh
docker exec -it elasticsearch /bin/bash
```

> 在文件末尾增加如下两行配置

```sh
http.cors.enabled: true 
http.cors.allow-origin: "*"
```



退出 docker 容器

```sh
exit
```



重新启动容器

```sh
docker restart elasticsearch
```



## 安装 Kibana

启动镜像

```sh
docker run -d --name kibana --net elasticsearch -p 5601:5601 kibana:7.14.0
```



## 安装 elasticsearch-head

启动镜像

```sh
docker create --name elasticsearch-head --net elasticsearch -p 9100:9100 mobz/elasticsearch-head:5
```



修改 js 的配置

> 首先找到 elasticsearch-head 的容器 id

```sh
docker ps
```

> 复制出 vendor.js

```sh
docker cp 2eb47e6ce02a:/usr/src/app/_site/vendor.js /usr/local/
```

> 修改 vendor.js

```sh
vi /usr/local/vendor.js
```

> 替换请求头参数

```sh
sed -i 's#x-www-form-urlencoded#json;charset=UTF-8#g' /usr/local/vendor.js
```

> 将文件复制回容器

```sh
docker cp /usr/local/vendor.js  2eb47e6ce02a:/usr/src/app/_site
```

> 重启容器

```sh
docker restart elasticsearch-head
```

