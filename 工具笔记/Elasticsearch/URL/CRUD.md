# 简单CRUD

```properties
POST users/_doc
{
  "user":"Mick",
  "post_date":"2019-04-15T14:12:12",
  "message":"trying out Kibana"
}

POST users/_doc/1?op_type=create
{
  "user":"Jack",
  "post_date":"2019-05-15T14:12:12",
  "message":"trying out Elasticsearch"
}

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
```

