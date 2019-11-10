# Analyzer 分词器

分词器是专门处理分词的组件，Analyzer 由三部分组成

* Character Filters
  * 针对原始文本处理，例如去除 html 标签
* Tokenizer
  * 按照规则切分为单词
* Token Filter
  * 将切分的单词进行加工，小写，删除 stopwords，增加同义词





## 自带分词器种类

* Standerd Analyzer
  * 默认分词器，按词切分，小写处理
  * 中文会分成一个一个的字
* Simple Analyzer
  * 按照非字母切分（非字母符号都被过滤），小写处理
* Stop Analyzer
  * 小写处理，停用词过滤（the, a is）
* Whitespace Analyzer
  * 按照空格切分，不转小写
* Keyword Analyzer
  * 不分词，直接将输入当做输出
* Patter Analyzer
  * 正则表达式，默认 \W+  （非字符分割），小写处理
* Language
  * 提供了 30 多种常见语言的分词器
  * english
* Customer Analyzer
  * 自定义分词器



中文建议的分词器

* IK
  * https://github.com/medcl/elasticsearch-analyzer-ik
* THULAC
  * https://github.com/microbun/elasticsearch-thulac-plugin



###  指定 Analyzer 查看如何分词

```properties
GET /_analyzer
{
	"analyzer":"standard",
	"text":"trying out Elasticsearch"
}
```

