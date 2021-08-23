# Spring 数据绑定

## Spring 数据绑定组件

* 标准组件
  * org.springframework.validation.DataBinder
* Web 组件
  * org.springframework.web.bind.WebDataBinder
  * org.springframework.web.bing.ServletRequestDataBinder
  * org.springframework.web.bind.support.WebRequestDataBinder
  * org.springframework.web.bind.support.WebExchangeDataBinder(since 5.0)
* DataBinder 核心属性

| 属性                 | 说明                           |
| -------------------- | ------------------------------ |
| target               | 关联目标 Bean                  |
| objectName           | 目标 Bean 名称                 |
| bingingResult        | 属性绑定结果                   |
| typeConverter        | 类型转换器                     |
| conversionService    | 类型转换服务                   |
| messageCodesResolver | 校验错误文案 Code 处理器       |
| validators           | 关联的 Bean Validator 实例集合 |



## Spring 数据绑定元数据

DataBinder 元数据：PropertyValues

| 特征         | 说明                                                         |
| ------------ | ------------------------------------------------------------ |
| 数据来源     | BeanDefinition，主要来源 XML 资源配置 BeanDefinition         |
| 数据结构     | 由一个或多个 PropertyValue 组成                              |
| 成员结构     | PropertyValue 包含属性名称，以及属性值（包括原始值、类型转换后的值） |
| 常见实现     | MutablePropertyValues                                        |
| Web 扩展实现 | ServletConfigProperyValues、ServiceRequestParameterPropertyValues |
| 相关生命周期 | InstantionAwareBeanPostProcessor#postProcessProperties       |



## Spring 数据绑定控制参数

* DataBinder 绑定特殊场景分析

  * 当 PropertyValues 中包含名称 x 的 PropertyValue，目标对象 B 不存在 x 属性，当 bind 方法执行时会发生什么？：默认会忽略
  * 当 PropertyValues 中包含名称 x 的 PropertyValue，目标对象 B 中存在 x 属性，当 bind 方法执行时，如何避免 B 属性 x 不被绑定？
  * 当 PropertyValues 中包含名称 x.y 的 PropertyValue，目标对象 B 中存在 x 属性（嵌套 y 属性），当 bind 方法执行时，会发生什么？：会给嵌套对象赋值

* DataBinder 绑定控制参数

  | 参数名称            | 说明                               |
  | ------------------- | ---------------------------------- |
  | IgnoreUnknownFields | 是否忽略未知字段，默认值：true     |
  | IgnoreInvalIdFields | 是否忽略非法字段，默认值：false    |
  | autoGrowNestedPaths | 是否自动增加嵌套路径，默认值：true |
  | allowedFields       | 绑定字段白名单                     |
  | disallowedFields    | 绑定字段黑名单                     |
  | requiredFields      | 必须绑定字段                       |

  

## Spring 底层 Java Beans 替换实现

* JavaBeans 核心实现：java.beans.BeanInfo
  * 属性（Property）
    * java.beans.PropertyEditor
  * 方法（Method）
  * 事件（Event）
  * 表达式（Expression）
* Spring 替代实现：org.springframework.beans.BeanWarpper
  * 属性（Property）
    * java.beans.PropertyEditor
  * 嵌套属性路径（nested path）



## BeanWrapper 的使用场景

BeanWrapper

* Spring 底层 JavaBeans 基础设施的中心化接口
* 通常不会直接使用，间接用于 BeanFactory 和 DataBinder
* 提供标准 JavaBeans 分析和操作，能够单独或批量存储 Java Bean 的属性（properties）
* 支持嵌套属性路径（nested path）
* 实现类 org.springframework.beans.BeanWrapperImpl



## 标准 JavaBeans 是如何操作属性的

| API                           | 说明                     |
| ----------------------------- | ------------------------ |
| java.beans.Introspector       | Java Beans 内省 API      |
| java.beans.BeanInfo           | Java Bean 元信息 API     |
| java.beans.BeanDescriptor     | Java Bean 信息描述符     |
| java.beans.PropertyDescriptor | Java Bean 属性描述符     |
| java.beans.MethodDescriptor   | Java Bean 方法描述符     |
| java.beans.EventSetDescriptor | Java Bean 事件集合描述符 |



## DataBinder 数据校验

DataBinder 与 BeanWrapper

* DataBinder.bind 方法生成 BeanPropertyBindingResult
* BeanPropertyBindingResult 关联 BeanWrapper