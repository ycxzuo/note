# elasticsearch 6.3.2 windows 安装

## 下载

[elasticsearch 下载网址](https://www.elastic.co/downloads/elasticsearch)

![download.jpg](http://wx1.sinaimg.cn/mw690/0060lm7Tly1ftnkn38dvjj30t10jfta8.jpg)

还需要电脑有java8以上的环境.

[java8的下载地址](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

## 无脑解压

解压不解释

## 启动

进入到解压文件的bin目录下,直接双击 `elasticsearch.bat` 脚本运行.一般建议直接使用cmd命令将elasticsearch变成一个计算机服务,以后不用麻烦来开启elasticsearch了

### 步骤

* 执行命令
  * 将elasticsearch安装为windows服务
    * `elasticsearch-service.bat install `
  * 删除服务
    * `elasticsearch-service.bat remove`
  * 启动服务(必先安装服务)
    * `elasticsearch-service.bat start`
  * 停止服务
    * `elasticsearch-service.bat stop`
  * 利用GUI管理服务
    * `elasticsearch-service.bat manager`

## 使用谷歌插件管理

谷歌网上应用商店内部的谷歌浏览器插件,所以要利用翻墙或者谷歌访问助手.这里有纯净的[谷歌访问助手zip包](https://download.csdn.net/download/ycxzuoxin/10566608).

[插件网址](https://chrome.google.com/webstore/detail/elasticsearch-head/ffmkiejjmecolpfloofpjologoblkegm)下载ElasticSearch Head插件

安装后直接在谷歌浏览器上点击右上角的工具图标就可以使用,无需繁琐的安装node.js等工具.