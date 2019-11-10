# Mapping 解析

## 概念

Mapping 类似数据库中的 schema 的定义，作用

* 定义索引中的字段的名称
* 定义字段的数据类型，例如字符串，数字，布尔值
* 字段，倒排索引的相关配置，（Analyzed or Not Analyzed，Analyzer）



Mapping 会把 JSON 文档映射成 Lucene 所需要的扁平格式

一个 Mapping 属于一个索引的 Type

* 每个文档都属于一个 Type
* 一个 Type 有一个 Mapping 定义
* 7.0 开始，不需要再 Mapping 定义中指定 Type 信息



## 字段数据类型

* 简单类型
  * Text / Keyword
  * Date
  * Long / Float
  * Boolean
  * IPv4 & IPv6
* 复杂类型 - 对象和嵌套类型
  * 对象类型 / 嵌套类型
* 特殊类型
  * geo_point & geo_shape / percolator



## Dynamic Mapping

* 在写入文档的时候，如果索引不存在，会自动创建索引
* Dynamic Mapping 的机制，使得我们无须手动定义 Mapping，Elasticsearch 会自动根据文档信息，推算字段类型
* 但是有时候推算不准，例如地理位置信息
* 当类型如果设置不对，会导致一些功能无法正常运行，例如 Range 查询

**这个属性可以在不重构索引时修改**



### 类型的自动识别

* 字符串
  * 匹配日期格式，设置成 Date
  * 匹配数字设置为 Float 或者 Long
  * 设置为 Text，并且增加 keyword 子字段
* 布尔值
  * boolean
* 浮点数
  * float
* 整数
  * long
* 对象
  * object
* 数组
  * 有第一个**非空值**的类型决定
* 空值
  * 忽略



## 更改 Mapping 的字段类型

* 新增字段
  * Dynamic 设置为 true，一旦有新增字段的文档写入，Mapping 也同时被更新
  * Dynamic 设置为 false，Mapping 不会被更新，新增字段的数据无法被索引，但是信息会出现在 _source 中
  * Dynamic 设置为 strict，文档写入失败，直接报错 400
* 对已有字段，一旦已经有数据写入，就不再支持修改字段定义
  * Lucene 实现的倒排索引，一旦生成，不允许修改
* 如果希望改变字段类型，必须 Reindex API，重建索引

如果修改了字段的数据类型，会导致已被索引的数据无法被搜索，但是新增不会有影响





```properties
// 写入文档，查看 Mapping
PUT mapping_test/_doc/1
{
  "firstName":"Chan",
  "lastName": "Jackie",
  "loginDate":"2018-07-24T10:29:48.103Z"
}

// 查看 Mapping文件
GET mapping_test/_mapping


// Delete index
DELETE mapping_test

// dynamic mapping，推断字段的类型
PUT mapping_test/_doc/1
{
    "uid" : "123",
    "isVip" : false,
    "isAdmin": "true",
    "age":19,
    "heigh":180
}

// 查看 Dynamic
GET mapping_test/_mapping


// 默认Mapping支持dynamic，写入的文档中加入新的字段
PUT dynamic_mapping_test/_doc/1
{
  "newField":"someValue"
}

// 该字段可以被搜索，数据也在_source中出现
POST dynamic_mapping_test/_search
{
  "query":{
    "match":{
      "newField":"someValue"
    }
  }
}


// 修改为dynamic false
PUT dynamic_mapping_test/_mapping
{
  "dynamic": false
}

// 新增 anotherField
PUT dynamic_mapping_test/_doc/10
{
  "anotherField":"someValue"
}


// 该字段不可以被搜索，因为dynamic已经被设置为false
POST dynamic_mapping_test/_search
{
  "query":{
    "match":{
      "anotherField":"someValue"
    }
  }
}

get dynamic_mapping_test/_doc/10

// 修改为strict
PUT dynamic_mapping_test/_mapping
{
  "dynamic": "strict"
}



// 写入数据出错，HTTP Code 400
PUT dynamic_mapping_test/_doc/12
{
  "lastField":"value"
}

DELETE dynamic_mapping_test
```







