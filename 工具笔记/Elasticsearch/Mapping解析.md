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



### 如何显式定义一个 Mapping

```properties
PUT /movies
{
	"mappings": {
		// define
	}
}
```

* 可以参考 API，纯手写
* 为了减少输入，减少出错概率
  * 创建一个临时的 index，写一些样本数据
  * 通过访问 Mapping API 获得该临时文件的动态 Mapping 定义
  * 修改后，使用改配置创建你的索引
  * 删除临时索引



### 控制当前字段是否被索引

index 属性可以控制当前字段是否被索引，默认为 true。如果设置为 false，该字段不可被搜索



### Index Options

* 四种不同级别的 Index Options 配置，可以控制倒排索引记录的内容
  * docs
    * 记录 doc id
  * freqs
    * 记录 doc id 和 term frequencies
  * positions
    * 记录 doc id / term frequencies / term position
  * offsets
    * 记录 doc id / term frequencies / term position / character offects
* Test 类型默认记录 positions，其它默认为 docs
* 记录内容越多，占用存储空间越大



### null_value

* 需要对 Null 值实现搜索
* **只有 Keyword 类型支持设定 Null_Value**



### _all 或 copy_to

* _all 在 7 中被 copy_to 所代替
* 满足一些特性的搜索需求
* copy_to 将字段的数值拷贝到目标字段，实现类似 _all 的作用
* copy_to 的目标字段不出现在 _source 中



### 数组类型

Elasticsearch 中不提供专门的数组类型，但是任何字段，都可以包含多个相同类型的数值



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


// 设置 index 为 false
DELETE users
PUT users
{
    "mappings" : {
      "properties" : {
        "firstName" : {
          "type" : "text"
        },
        "lastName" : {
          "type" : "text"
        },
        "mobile" : {
          "type" : "text",
          "index": false
        }
      }
    }
}

PUT users/_doc/1
{
  "firstName":"Ruan",
  "lastName": "Yiming",
  "mobile": "12345678"
}

POST /users/_search
{
  "query": {
    "match": {
      "mobile":"12345678"
    }
  }
}




// 设定Null_value
DELETE users
PUT users
{
    "mappings" : {
      "properties" : {
        "firstName" : {
          "type" : "text"
        },
        "lastName" : {
          "type" : "text"
        },
        "mobile" : {
          "type" : "keyword",
          "null_value": "NULL"
        }

      }
    }
}

PUT users/_doc/1
{
  "firstName":"Ruan",
  "lastName": "Yiming",
  "mobile": null
}


PUT users/_doc/2
{
  "firstName":"Ruan2",
  "lastName": "Yiming2"

}

GET users/_search
{
  "query": {
    "match": {
      "mobile":"NULL"
    }
  }

}



// 设置 Copy to
DELETE users
PUT users
{
  "mappings": {
    "properties": {
      "firstName":{
        "type": "text",
        "copy_to": "fullName"
      },
      "lastName":{
        "type": "text",
        "copy_to": "fullName"
      }
    }
  }
}
PUT users/_doc/1
{
  "firstName":"Ruan",
  "lastName": "Yiming"
}

GET users/_search?q=fullName:(Ruan Yiming)

POST users/_search
{
  "query": {
    "match": {
       "fullName":{
        "query": "Ruan Yiming",
        "operator": "and"
      }
    }
  }
}


// 数组类型
PUT users/_doc/1
{
  "name":"onebird",
  "interests":"reading"
}

PUT users/_doc/1
{
  "name":"twobirds",
  "interests":["reading","music"]
}

POST users/_search
{
  "query": {
		"match_all": {}
	}
}

GET users/_mapping
```


