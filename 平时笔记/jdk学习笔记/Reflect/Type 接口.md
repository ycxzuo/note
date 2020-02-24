# Type 接口

**讨论版本为 JDK1.8**

`java.lang.reflect.Type`，所有类型的父接口，在 JDK1.5 引入，接口的源码如下

```java
public interface Type {
    /**
     * Returns a string describing this type, including information
     * about any type parameters.
     *
     * @implSpec The default implementation calls {@code toString}.
     *
     * @return a string describing this type
     * @since 1.8
     */
    default String getTypeName() {
        return toString();
    }
}
```

其实继承关系如下

![继承关系](http://tva1.sinaimg.cn/large/0080xEK2ly1gc0bu9t4t0j30g009gt9q.jpg)

其中有四个子接口和一个实现类

* Type
  * GenericArrayType 泛型数组
  * ParameterizedType 参数化类型
  * WildcardType 泛型表达式
  * TypeVariable 类型变量
  * Class 普通类



## GenericArrayType

### 定义

类似 `T[]` 或者 `List<T>[]` 等，描述的是 ParameterizedType 类型以及 TypeVariable 类型数组

### 源码

```java
public interface GenericArrayType extends Type {
 	// 返回去掉最右边数组后的类型
    Type getGenericComponentType();
}
```

### 例子

```java
public class GenericArrayTypeTest {
    public static void main(String[] args) throws NoSuchFieldException {
        GenericArrayType type = (GenericArrayType)GenericArrayTypeBean.class.getDeclaredField("data").getGenericType();
        System.out.println(type.getTypeName());
        System.out.println(type.getGenericComponentType());
    }
}

class GenericArrayTypeBean<T> {
    List<T>[][] data;
}

// 输出结果
// java.util.List<T>[][]
// java.util.List<T>[]
```



## ParameterizedType

### 定义

参数化类型，类似 `Map<T, K>`，即常说的泛型

### 源码

```java
public interface ParameterizedType extends Type {
    // 获取所有的泛型，因为有两个或以上的泛型，所以返回值为数组，只获取第一层 <> 内的内容
    Type[] getActualTypeArguments();
	// 获取参数 <> 外的类型
    Type getRawType();
	// 获取参数类型所属的类，即是哪个类的内部类，如果没有，就返回 null
    Type getOwnerType();
}
```

### 例子

```java
public class ParameterizedTypeTest {
    public static void main(String[] args) throws NoSuchMethodException {
        // 获取方法的参数类型
        Type[] types = ParameterizedTypeBean.class.getMethod("parameterizedTypeTest", Map.Entry.class).getGenericParameterTypes();
        ParameterizedType type = (ParameterizedType) types[0];
        System.out.println(type.getTypeName());
        System.out.println(type.getActualTypeArguments()[0]);
        System.out.println(type.getActualTypeArguments()[1]);
        System.out.println(type.getRawType());
        System.out.println(type.getOwnerType());
    }
}

class ParameterizedTypeBean<T, K> {
    public void parameterizedTypeTest(Map.Entry<T, List<K>> data) {
        return;
    }
}

// 输出结果
// java.util.Map$Entry<T, java.util.List<K>>
// T
// java.util.List<K>
// interface java.util.Map$Entry
// interface java.util.Map
```



## WildcardType 

### 定义

返回类型的表达式，例如 `?`、`? extends Number` 等，根据 java doc 所言，返回值虽然为数组，只是为了以后扩展用的，现在只会返回一个

### 源码

```java
public interface WildcardType extends Type {
	// 获取上限
    Type[] getUpperBounds();
	// 获取下限
    Type[] getLowerBounds();
    // one or many? Up to language spec; currently only one, but this API
    // allows for generalization.
}
```

### 例子

```java
public class WildcardTypeTest {
    public static void main(String[] args) throws NoSuchMethodException {
        ParameterizedType parameterizedType = (ParameterizedType)WildcardTypeBean.class.getMethod("wildcardTypeTest", Map.class).getGenericReturnType();
        for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
            WildcardType type = (WildcardType) actualTypeArgument;
            System.out.println(type.getTypeName());
            System.out.println(type.getUpperBounds()[0]);
        }
    }
}

class WildcardTypeBean<T, K> {
    public List<? extends Number> wildcardTypeTest(Map<T, K> map) {
        return null;
    }
}

// 输出结果
// ? extends java.lang.Number
// class java.lang.Number
```



## TypeVariable

### 定义

在类或者方法上定义类型变量，可以看到也有一个泛型参数，要求需要是 GenericDeclaration 的子类，其继承关系如下

GenericDeclaration

* Class
* Executable
  * Method
  * Constructor

所以我们只能在类型，例如 Class，Interface、方法和构造函数这三个地方声明泛型参数

### 源码

```java
public interface TypeVariable<D extends GenericDeclaration> extends Type, AnnotatedElement {
	// 获得该类型变量的上限，若无显式定义，默认为 Object
    Type[] getBounds();
	// 获得定义这个类型参数的类
    D getGenericDeclaration();
	// 获得这个类型变量在定义时候的名称
    String getName();

    // since1.8 如果这个这个泛型参数类型的上界用注解标记了，可以通过它拿到相应的注解
    AnnotatedType[] getAnnotatedBounds();
}
```

### 例子

```java
public class TypeVariableTest {
    public static void main(String[] args) throws NoSuchMethodException {
        TypeVariable<Class<TypeVariableBean>>[] typeParameters = TypeVariableBean.class.getTypeParameters();
        for (TypeVariable<Class<TypeVariableBean>> typeParameter : typeParameters) {
            System.out.println(typeParameter.getTypeName());
            System.out.println(typeParameter.getGenericDeclaration());
            for (AnnotatedType annotatedBound : typeParameter.getAnnotatedBounds()) {
                for (Annotation annotation : annotatedBound.getAnnotations()) {
                    System.out.println(annotation);
                }
            }
            for (Type bound : typeParameter.getBounds()) {
                System.out.println(bound.getTypeName());
            }
            System.out.println("-------------------------------------");
        }
    }
}

class TypeVariableBean<T extends @CustomAnnotation Number & Comparable<T>, K> {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
@interface CustomAnnotation {
}

// 输出结果
// T
// class com.yczuoxin.demo.TypeVariableBean
// @com.yczuoxin.demo.CustomAnnotation()
// java.lang.Number
// java.lang.Comparable<T>
// -------------------------------------
// K
// class com.yczuoxin.demo.TypeVariableBean
// java.lang.Object
// java.lang.Object
-------------------------------------
```



