# Spring 泛型处理

## Java 泛型基础

* 泛型类型
  * 泛型类型是在类型上参数化的泛型类或接口
* 泛型使用场景
  * 编译时强类型检查
  * 避免类型强转
  * 实现通用算法
* 泛型类型擦写
  * 泛型被引入到 Java 语言中，以便在编译时提供更严格的类型检查并支持泛型编程。类型擦写确保不会为参数化类型创建新类，因此，泛型不会产生运行时开销。为了实现泛型，编译器将类型擦写运用于
    * 将泛型类型中的所有类型参数替换为其边界，如果类型参数是无边界的，则将其替换为 Object。因此，生成的字节码只包含普通类、接口和方法
    * 必要时插入类型转换以保持类型安全
    * 生成桥方法以保留扩展泛型类型中的多态性



## Java 5 类型接口

* Java 5 类型接口：java.lang.reflect.Type

| 派生类或接口                        | 说明                                   |
| ----------------------------------- | -------------------------------------- |
| java.lang.Class                     | Java 类 API，如 java.lang.String       |
| java.lang.reflect.GenericArrayType  | 泛型数组类型                           |
| java.lang.reflect.ParameterizedType | 泛型参数类型                           |
| java.lang.reflect.TypeVariable      | 泛型类型变量，如 Collection\<E> 中的 E |
| java.lang.reflect.WildcardType      | 泛型通配类型                           |

* Java 泛型反射 API

| 类型                             | API                                    |
| -------------------------------- | -------------------------------------- |
| 泛型信息（Generics Info）        | java.lang.Class#getGenericInfo()       |
| 泛型参数（Parameters）           | java.lang.reflect.ParameterizedType    |
| 泛型父类（Super Classes）        | java.lang.Class.getGenericSuperClass() |
| 泛型接口（Interfaces）           | java.lang.Class.getGenericInterfaces() |
| 泛型声明（Generics Declaration） | java.lang.reflect.GenericDeclaration   |



## Spring 泛型类型辅助类

* 核心 API：org.spring.framework.core.GenericTypeResolver
  * 版本支持：2.5.2+
  * 处理类型相关（Type）相关方法
    * resolveReturnType
    * resolveType
  * 处理泛型参数类型（ParameterizedType）相关方法
    * resolveReturnTypeArgument
    * resolveTypeArgument
    * resolveTypeArguments
  * 处理泛型类型变量（TypeVariable）相关方法
    * getTypeVariableMap



## Spring 泛型集合类型辅助类

* 核心 API：org.springframework.core.GenericCollectionTypeResolver
  * 版本支持：2.0~4.3
  * 替换实现：org.springframework.core.ResolvableType
  * 处理 Collection 相关
    * getCollection*Type
  * 处理 Map 相关
    * getMapKey*Type
    * getMapValue*Type



## Spring 方法参数封装

核心 API：org.springframework.core.MethodParameter

* 起始版本：2.0+
* 元信息
  * 关联的方法：Method
  * 关联的构造器：Constructor
  * 构造器或方法参数索引：parameterIndex
  * 构造器或方法参数类型：parameterType
  * 构造器或方法参数泛型类型：genericParameterType
  * 构造器或方法参数参数名称：parameterName（Spring MVC 获取参数名称代替 @Value 的手段），这个索引如果是 -1，则表示找返回值，入参从 0 开始
  * 所在的类：containingClass

*注意*：获取参数名称

* jdk1.8+：
  * 如果开启 jvm 编译启动参数 `javac -parameters`
  * 还需要给 MethodParameter 注册 DefaultParameterNameDiscoverer，方法如下

```java
methodParameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
```

* 默认使用的是 LocalVariableTableParameterNameDiscoverer 读取 class 的 inputStream 去拿到参数名称



## Spring 4.0 泛型优化实现：ResolvableType

核心 API：org.springframework.core.ResolvableType

* 起始版本：4.0+
* 扮演角色：GenericTypeResolver 和 GenericCollectionTypeResolver 替代者
* 工厂方法：for* 方法
* 转换方法：as* 方法
* 处理方法：resolve 方法
* 泛型参数：resolveGenerics/resolveGeneric(index) 方法，如果没有或者超过边界，会返回 ResolvableType.NONE，就是一个 ？



## ResolvableType 的局限性

* 局限一：ResolvableType 无法处理泛型擦写
* 局限二：ResolvableType 无法处理非具体化的 ParameterizedType