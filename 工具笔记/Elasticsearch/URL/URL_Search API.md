# URL Search API

主要分为了两大类

* URL Search
  * 在 URL 中使用查询参数
* Request Body Search
  * 使用 Elasticsearch 提供的，基于 JSON 格式的更加完备的 Query Domain Specific  Language （DSL）



### 指定查询的索引

| 语法                   | 范围                |
| ---------------------- | ------------------- |
| /_search               | 集群上所有的索引    |
| /index1/_search        | index1              |
| /index1,index2/_search | index1 和 index2    |
| /index*/_search        | 以 index 开头的索引 |



## URI 查询

顾名思义，用 URI 实现搜索

* q 指定查询语句，使用 query string syntax
* df 默认字段，不指定是，会对所有的字段进行查询
* Sort 排序 / from 和 size 用于分页
* Profile 可以设置为 “true” 查看查询是如何被执行的

### 指定字段 v.s 泛查询

* q=title:2012  / q=2012



### Term v.s Phrase

* Beautifl Mind 等效于 Beautiful OR Mind
* “Beautifl Mind“ 等效于 Beautiful AND Mind。Phrase 查询，还要求前后顺序保持一致



### 分组与引号

* title:(Beautiful AND Mind)
* title="Beautiful Mind"



### 布尔操作

* AND/OR/NOT 或者 &&/||/!
  * 必须大写
  * title:(matrix NOT reloaded)



### 分组

* +表示 must
* -表示 must_not
* title:(+matrix -reloaded)



### 范围查询

* 区间表示：[] 闭区间， {} 开区间
  * year:{2019 TO 2018]
  * year:[* TO 2018]



### 算术符号

* year:>2010
* year:(>2010 && < 2018)
* year:(+>2010 +<=2018)



### 通配符查询（效率低，占用内存大，不建议使用。特别是放在最前面）

* ？代表 1 个字符，* 代表 0 或多个字符
  * title:mi?d
  * title:be*



### 正则表达

* title:[bt]oy



### 模糊匹配与近似查询

* title:befutifl~1
* title:"lord rings"~2





例如

```properties
// 指定字段查询
GET /movies/_search?q=2012&df=title
{
  "profile":"true"
}

GET /movies/_search?q=title:2012
{
  "profile":"true"
}

// 泛查询
GET /movies/_search?q=2012
{
  "profile":"true"
}

// 使用引号，Phrase 查询
GET /movies/_search?q=title:"Beautiful Mind"
{
  "profile": "true"
}

// Mind 为泛查询
GET /movies/_search?q=title:Beautiful Mind
{
  "profile": "true"
}

// 分组，Bool 查询
GET /movies/_search?q=title:(Beautiful Mind)
{
  "profile": "true"
}

// AND 查询
GET /movies/_search?q=title:(Beautiful AND Mind)
{
  "profile": "true"
}

// NOT 查询
GET /movies/_search?q=title:(Beautiful NOT Mind)
{
  "profile": "true"
}

// %2B 为 + 查询
GET /movies/_search?q=title:(Beautiful %2BMind)
{
  "profile": "true"
}

// 范围查询
GET /movies/_search?q=title:beautiful AND year:[1980 TO 2018]
{
  "profile": "true"
}

// 通配符查询
GET /movies/_search?q=title:b*
{
  "profile": "true"
}

// 模糊匹配
GET /movies/_search?q=title:beautifl~1
{
  "profile": "true"
}

// 模糊匹配
GET /movies/_search?q=title:"Lord Rings"~2
{
  "profile": "true"
}
```





## Request Body 查询

例如

curl -XGET "http://localhost:9200/kibana_sample_data_ecommerce/_search" -H 'Content-Type:application/json' -d'

{

​	"query":{

​			"match_all":{}

​		}

}'