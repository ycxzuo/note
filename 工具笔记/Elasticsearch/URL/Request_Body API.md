# Request Body API

将查询语句通过 HTTP Request Body 发送给 Elasticsearch



## 分页

* from
  * 从哪个 offset 开始查
* size
  * 查询多少个结果
* 获取靠后的翻页成本较高



## 排序

* sort
  * 根据哪个字段排序，最好在 数字类型 和 日期类型 字段上排序
  * 因为对于多值类型或分析过的字段排序，系统会随机选一个值



## 获取需要的信息

* _source filtering
  * 如果 _source 没有存储，那就只返回匹配的文档的元数据
  * _source 支持使用通配符 _source["name\*", "desc\*"]
  * 如果文档数据量大并且部分信息不需要查时使用



## 脚本字段

* script_fields
  * 对值进行计算或者拼接



## Phrase 查询

顺序必须保持一致，且中间可以有别的词，但是得使用 slop 指定数量



```properties
// ignore_unavailable=true，可以忽略尝试访问不存在的索引“404_idx”导致的报错
POST /movies,404_idx/_search?ignore_unavailable=true
{
  "profile": true,
	"query": {
		"match_all": {}
	}
}

// 查询movies分页
POST /kibana_sample_data_ecommerce/_search
{
  "from":10,
  "size":20,
  "query":{
    "match_all": {}
  }
}


// 对日期排序
POST /kibana_sample_data_ecommerce/_search
{
  "sort":[{"order_date":"desc"}],
  "query":{
    "match_all": {}
  }

}

// source filtering
POST /kibana_sample_data_ecommerce/_search
{
  "_source":["order_date"],
  "query":{
    "match_all": {}
  }
}


// 脚本字段
GET /kibana_sample_data_ecommerce/_search
{
  "script_fields": {
    "new_field": {
      "script": {
        "lang": "painless",
        "source": "doc['order_date'].value+'hello'"
      }
    }
  },
  "query": {
    "match_all": {}
  }
}

// 普通的 match 查询
POST /movies/_search
{
  "query": {
    "match": {
      "title": "last christmas"
    }
  }
}

// 带逻辑的搜索
POST /movies/_search
{
  "query": {
    "match": {
      "title": {
        "query": "last christmas",
        "operator": "and"
      }
    }
  }
}

// Phrase 查询
POST /movies/_search
{
  "query": {
    "match_phrase": {
      "title":{
        "query": "one love"
      }
    }
  }
}

// 中间可以插入一个词
POST /movies/_search
{
  "query": {
    "match_phrase": {
      "title":{
        "query": "one love",
        "slop": 1
      }
    }
  }
}

```

