# JDK9

## 问题解决方案

### 问题

#### java.lang.TypeNotPresentException: Type javax.xml.bind.JAXBContext not present`

### 解决方案

#### 引入POM文件

```xml
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.3.0</version>
</dependency>
<dependency>
    <groupId>com.sun.xml.bind</groupId>
    <artifactId>jaxb-impl</artifactId>
    <version>2.3.0</version>
</dependency>
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>2.3.0</version>
</dependency>
<dependency>
    <groupId>javax.activation</groupId>
    <artifactId>activation</artifactId>
    <version>1.1.1</version>
</dependency>
```

### 原因

因为JAXB-API是JAVAEE的一部分，只是没有默认引入而已，Java9引入模块的概念，可以使用`--add-modules java.xml.bind`引入

JDK9默认未引入的模块

* javax.activation 
* javax.corba 
* javax.transaction 
* javax.xml.bind 
* javax.xml.ws 
* javax.xml.ws.annotation

### JAXB-API（Java API for XML Binding）简介

提供了一个快速便捷的方式将Java对象与XML进行转换，在JAXB中，将一个Java对象转换为XML的过程称之为**Marshal**，将XML转换为Java对象的过程称之为**UnMarshal**。

* @XmlRootElement  -> 将一个Java类映射为一段XML的根节点

  * 使用
    * 根结点
  * name -> 这个根节点的名称
  * namespace -> 这个根节点命名空间

* @XmlAccessorType -> 这个类中的何种类型需要映射到XML

  * 使用
    * 注释java类，关于什么类型的变量输出。
  * XmlAccessType枚举类
    * XmlAccessType.FIELD -> 映射这个类中的所有字段到XML
    * XmlAccessType.PROPERTY -> 映射这个类中的属性（get/set方法）到XML
    * XmlAccessType.PUBLIC_MEMBER（默认） -> 将这个类中的所有public的field或property同时映射到XML
    * XmlAccessType.NONE -> 不映射

* @XmlElement -> 指定一个字段或get/set方法映射到XML的节点

  * 使用
    * Java类的域变量。当类为public，或实现get/set，无须标注@XmlElement注释，也会根据默认值实现转化。有name、 namespace属性。name是输出xml的名。{nillable}?属性，为false，该域变量为空时不输出。required属性，为true表示该域变量必须存在
  * name(default) -> 节点名称
  * namespace -> 节点命名空间
  * required -> 是否必须（默认为false）
  * nillable -> 该字段是否包含 nillable="true" 属性（默认为false）
    * minOccurs
      * 值为null时，字段就不出现在xml中了，例如数组中会将null丢弃
    * nillable
      * nillable="true"，在null的时，xml还是完整的有，例如数组中会将null算作占位符
  * type -> 定义该字段或属性的关联类型

* @XmlAttribute -> 指定一个字段或get/set方法映射到XML的属性

  * name -> 属性名称
  * namespace -> 属性命名空间
  * required -> 是否必须（默认为false）

* @XmlTransient -> 某一字段或属性不需要被映射为XML，一般与XmlAccessorType共用

* @XmlType -> 映射的一些相关规则

  * 使用
    * 注释java类。在列集过程中，改变域变量输出顺序（默认字母顺序）。在反列集过程中利用@XmlType注释可以设置构造该java对象的方式。
  * propOrder -> 指定映射XML时的节点顺序
  * factoryClass -> 指定UnMarshal时生成映射类实例所需的工厂类，默认为这个类本身
  * factoryMethod -> 指定工厂类的工厂方法
  * name -> 定义XML Schema中type的名称
  * namespace -> 指定Schema中的命名空间

* @XmlElementWrapper -> 为数组元素或集合元素定义一个父节点，如：

  ```java
  @XmlElementWrapper(name="items") 
  @XmlElement(name="item") 
  ```

  * 用于collection、list、map等

* @XmlJavaTypeAdapter -> 自定义某一字段或属性映射到XML的适配器

* @XmlSchema -> 配置整个包的namespace，这个注解需放在`package-info.java`文件中

  * 使用
    * 用来注释java包。通过它可以配置整个包的namespace。由于它出现在import前，所以需要完整的类路径。


