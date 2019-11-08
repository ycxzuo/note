# 简单CRUD

```properties
// 查看索引相关信息
GET kibana_sample_data_ecommerce

// 查看索引的文档数
GET kibana_sample_data_ecommerce/_count

// 搜索内容
POST kibana_sample_data_ecommerce/_search

// 查看 indices
GET /_cat/indices/kibana*?v&s=index

// 查看状态为绿色的索引
GET /_cat/indices?v&health=green

// 按文档个数排序
GET /_cat/indices?v&s=docs.count:desc

// 查看索引占用的内存并降序
GET /_cat/indices?v&h=i,tm&s=tm:desc

GET _cluster/health
GET _cat/nodes
GET _cat/shards

POST users/_doc
{
  "user":"Mick",
  "post_date":"2019-04-15T14:12:12",
  "message":"trying out Kibana"
}

// 创建文档
PUT users/_doc/1?op_type=create
{
  "user":"Jack",
  "post_date":"2019-05-15T14:12:12",
  "message":"trying out Elasticsearch"
}

// 覆盖文档
POST users/_doc/1
{
  "user":"Jack",
  "post_date":"2019-05-15T14:12:12",
  "message":"trying out Elasticsearch"
}

GET users/_doc/1

// 覆盖
PUT users/_doc/1
{
  "user":"Mick"
}

// 增加字段
POST users/_doc/1/_update
{
  "doc":{
    "post_date":"2019-05-15T14:12:12",
    "message":"trying out Elasticsearch"
  }
}

// 删除文档
DELETE users/_doc/1

// Bluk 批量操作,一组操作中不允许有换行
POST _bulk
{"index":{"_index":"test","_type":"data","_id":"1"}}
{"field1":"value1"}
{"delete":{"_index":"test","_type":"data","_id":"2"}}
{"create":{"_index":"test2","_type":"data","_id":"3"}}
{"field1":"value3"}
{"update":{"_id":"1","_type":"data","_index":"test"}}
{"doc":{"field2":"value2"}}

// mget 批量读取
GET _mget
{
  "docs":[
    {
      "_index":"test",
      "_type":"data",
      "_id":"1"
      
    },
    {
      "_index":"test2",
      "_type":"data",
      "_id":"3"
    }
  ]
}

// msearch 批量搜索,一组操作中不允许有换行
POST kibana_sample_data_ecommerce/_msearch
{}
{"query":{"match_all":{}},"size":"1"}
{"index":"users"}
{"query":{"match_all":{}},"size":"2"}
```

