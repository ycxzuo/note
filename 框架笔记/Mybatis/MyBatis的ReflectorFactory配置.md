# MyBatis 的 ReflectorFactory 配置

## 反射工厂

MyBatis 用于缓存 Reflector 的功能，其配置如下

```xml
<reflectorFactory type="com.yczuoxin.reflectorFactory.MyReflectorFactory"/>
```

### ReflectorFactory 接口

```java
public interface ReflectorFactory {
	// 此 Class 的 Reflector 是否需要缓存
    boolean isClassCacheEnabled();
	// 设置 Class 的 Reflector 是否需要缓存
    void setClassCacheEnabled(boolean classCacheEnabled);
	// 找到对应 Class 的 Reflector
    Reflector findForClass(Class<?> type);
}
```

其继承关系如下

* `org.apache.ibatis.reflection.ReflectorFactory`
  * `org.apache.ibatis.reflection.DefaultReflectorFactory`

它只有一个默认实现类 DefaultReflectorFactory

```java
public class DefaultReflectorFactory implements ReflectorFactory {
    private boolean classCacheEnabled = true;
    private final ConcurrentMap<Class<?>, Reflector> reflectorMap = new ConcurrentHashMap<>();

    public DefaultReflectorFactory() {
    }

    @Override
    public boolean isClassCacheEnabled() {
        return classCacheEnabled;
    }

    @Override
    public void setClassCacheEnabled(boolean classCacheEnabled) {
        this.classCacheEnabled = classCacheEnabled;
    }

    @Override
    public Reflector findForClass(Class<?> type) {
        if (classCacheEnabled) {
            // synchronized (type) removed see issue #461
            return reflectorMap.computeIfAbsent(type, Reflector::new);
        } else {
            return new Reflector(type);
        }
    }

}
```

很显然，DefaultReflectorFactory 用了一个 ConcurrentMap<Class<?>, Reflector> 来缓存需要缓存的 Reflector

### Reflector

那么 Reflector 有什么作用呢？这类的方法比较多，这里主要看下它的主要属性和构造函数

```java
public class Reflector {

    private final Class<?> type;
    private final String[] readablePropertyNames;
    private final String[] writablePropertyNames;
    private final Map<String, Invoker> setMethods = new HashMap<>();
    private final Map<String, Invoker> getMethods = new HashMap<>();
    private final Map<String, Class<?>> setTypes = new HashMap<>();
    private final Map<String, Class<?>> getTypes = new HashMap<>();
    private Constructor<?> defaultConstructor;

    private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

    public Reflector(Class<?> clazz) {
        type = clazz;
        addDefaultConstructor(clazz);
        addGetMethods(clazz);
        addSetMethods(clazz);
        addFields(clazz);
        readablePropertyNames = getMethods.keySet().toArray(new String[0]);
        writablePropertyNames = setMethods.keySet().toArray(new String[0]);
        for (String propName : readablePropertyNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
        for (String propName : writablePropertyNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
    }
    ...
}
```

主要是获取了 Class 对象的构造器、属性及其的 getter 和 setter 方法

### MetaClass

MyBatis 中有一个类与这两个对象息息相关：MetaClass，其保存的类中的一些元信息

```java
public class MetaClass {

    private final ReflectorFactory reflectorFactory;
    private final Reflector reflector;

    // 私有构造函数
    private MetaClass(Class<?> type, ReflectorFactory reflectorFactory) {
        this.reflectorFactory = reflectorFactory;
        this.reflector = reflectorFactory.findForClass(type);
    }

    // 静态方法实例化一个新的 MetaClass
    public static MetaClass forClass(Class<?> type, ReflectorFactory reflectorFactory) {
        return new MetaClass(type, reflectorFactory);
    }

    // 传入类，并构造一个新的 MetaClass
    public MetaClass metaClassForProperty(String name) {
        Class<?> propType = reflector.getGetterType(name);
        return MetaClass.forClass(propType, reflectorFactory);
    }

    public String findProperty(String name) {
        StringBuilder prop = buildProperty(name, new StringBuilder());
        return prop.length() > 0 ? prop.toString() : null;
    }

    public String findProperty(String name, boolean useCamelCaseMapping) {
        if (useCamelCaseMapping) {
            name = name.replace("_", "");
        }
        return findProperty(name);
    }

    public String[] getGetterNames() {
        return reflector.getGetablePropertyNames();
    }

    public String[] getSetterNames() {
        return reflector.getSetablePropertyNames();
    }

    public Class<?> getSetterType(String name) {
        // 先经过属性表达式解析，如果有下一级属性，则递归调用
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaClass metaProp = metaClassForProperty(prop.getName());
            return metaProp.getSetterType(prop.getChildren());
        } else {
            return reflector.getSetterType(prop.getName());
        }
    }

    public Class<?> getGetterType(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaClass metaProp = metaClassForProperty(prop);
            return metaProp.getGetterType(prop.getChildren());
        }
        // issue #506. Resolve the type inside a Collection Object
        return getGetterType(prop);
    }

    private MetaClass metaClassForProperty(PropertyTokenizer prop) {
        Class<?> propType = getGetterType(prop);
        return MetaClass.forClass(propType, reflectorFactory);
    }

    private Class<?> getGetterType(PropertyTokenizer prop) {
        Class<?> type = reflector.getGetterType(prop.getName());
        if (prop.getIndex() != null && Collection.class.isAssignableFrom(type)) {
            Type returnType = getGenericGetterType(prop.getName());
            if (returnType instanceof ParameterizedType) {
                Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
                if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                    returnType = actualTypeArguments[0];
                    if (returnType instanceof Class) {
                        type = (Class<?>) returnType;
                    } else if (returnType instanceof ParameterizedType) {
                        type = (Class<?>) ((ParameterizedType) returnType).getRawType();
                    }
                }
            }
        }
        return type;
    }

    private Type getGenericGetterType(String propertyName) {
        try {
            Invoker invoker = reflector.getGetInvoker(propertyName);
            if (invoker instanceof MethodInvoker) {
                Field _method = MethodInvoker.class.getDeclaredField("method");
                _method.setAccessible(true);
                Method method = (Method) _method.get(invoker);
                return TypeParameterResolver.resolveReturnType(method, reflector.getType());
            } else if (invoker instanceof GetFieldInvoker) {
                Field _field = GetFieldInvoker.class.getDeclaredField("field");
                _field.setAccessible(true);
                Field field = (Field) _field.get(invoker);
                return TypeParameterResolver.resolveFieldType(field, reflector.getType());
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return null;
    }

    public boolean hasSetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            if (reflector.hasSetter(prop.getName())) {
                MetaClass metaProp = metaClassForProperty(prop.getName());
                return metaProp.hasSetter(prop.getChildren());
            } else {
                return false;
            }
        } else {
            return reflector.hasSetter(prop.getName());
        }
    }

    public boolean hasGetter(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            if (reflector.hasGetter(prop.getName())) {
                MetaClass metaProp = metaClassForProperty(prop);
                return metaProp.hasGetter(prop.getChildren());
            } else {
                return false;
            }
        } else {
            return reflector.hasGetter(prop.getName());
        }
    }

    public Invoker getGetInvoker(String name) {
        return reflector.getGetInvoker(name);
    }

    public Invoker getSetInvoker(String name) {
        return reflector.getSetInvoker(name);
    }

    // 该类中比较重要的方法
    private StringBuilder buildProperty(String name, StringBuilder builder) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        // 是否有子表达式
        if (prop.hasNext()) {
            // 查找对应的属性
            String propertyName = reflector.findPropertyName(prop.getName());
            if (propertyName != null) {
                // 追加属性名
                builder.append(propertyName);
                builder.append(".");
                // 创建对应的 MetaClass 对象
                MetaClass metaProp = metaClassForProperty(propertyName);
                // 递归调用
                metaProp.buildProperty(prop.getChildren(), builder);
            }
        } else {
            // 根据名称查找属性
            String propertyName = reflector.findPropertyName(name);
            if (propertyName != null) {
                builder.append(propertyName);
            }
        }
        return builder;
    }

    public boolean hasDefaultConstructor() {
        return reflector.hasDefaultConstructor();
    }

}
```

### PropertyTokenizer

这边把 PropertyTokenizer 带着看一眼，这个其实只是对传入的参数进行一个表达式的封装，只是一个中间传递的对象工具

```java
public class PropertyTokenizer implements Iterator<PropertyTokenizer> {
    // 当前第一个 '.' 前面的值
    private String name;
    // name + index 的合成值
    private final String indexedName;
    // 如果是集合对象，那么 [] 中的数字会保存在这个属性值中
    private String index;
    // 当前第一个 '.' 后面的值
    private final String children;

    public PropertyTokenizer(String fullname) {
       	// 处理带 '.' 的表达式，如 Person.name 之类的
        int delim = fullname.indexOf('.');
        if (delim > -1) {
            name = fullname.substring(0, delim);
            children = fullname.substring(delim + 1);
        } else {
            name = fullname;
            children = null;
        }
        indexedName = name;
        // 处理带 '[]' 的表达式，如 Person[0].name 之类的
        delim = name.indexOf('[');
        if (delim > -1) {
            index = name.substring(delim + 1, name.length() - 1);
            name = name.substring(0, delim);
        }
    }

    // 返回属性值，如 Person
    public String getName() {
        return name;
    }

    // 返回下标，如 0
    public String getIndex() {
        return index;
    }

    // 返回属性值和下标，如 Person[0]
    public String getIndexedName() {
        return indexedName;
    }

    // 返回子属性，如 name
    public String getChildren() {
        return children;
    }

    // 是不是最终属性的值
    @Override
    public boolean hasNext() {
        return children != null;
    }

    @Override
    public PropertyTokenizer next() {
        return new PropertyTokenizer(children);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
    }
}
```



## 读取配置

```java
private void reflectorFactoryElement(XNode context) throws Exception {
    if (context != null) {
        String type = context.getStringAttribute("type");
        ReflectorFactory factory = (ReflectorFactory) resolveClass(type).getDeclaredConstructor().newInstance();
        configuration.setReflectorFactory(factory);
    }
}
```



## 自定义

由于方法比较简单，就没有添加实现

```java
public class MyReflectorFactory extends DefaultReflectorFactory {

}
```

