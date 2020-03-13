# MyBatis 的 ObjectWrapperFactory 配置

## 对象加工工厂

MyBatis 提供在构造对象的时候，对于指定的对象进行特殊的加工，其配置方式如下

```xml
<objectWrapperFactory type="com.yczuoxin.objectwrapperfactory.MapWrapperFactory"/>
```

### ObjectWrapperFactory 接口

```java
public interface ObjectWrapperFactory {
	// 对象是否需要加工
    boolean hasWrapperFor(Object object);
	// 获取一个 ObjectWrapper 对象
    ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);

}
```

ObjectWrapperFactory 的继承关系

* ObjectWrapperFactory
  * `org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory`



可以看到这里出现了另一个一个很重要的接口 ObjectWrapper，方法名顾名知意，就不写注释了

```java
public interface ObjectWrapper {
	// 获取对应 prop 在当前类中属性对象
    Object get(PropertyTokenizer prop);
	// 给对应 prop 在当前类中属性对象赋值
    void set(PropertyTokenizer prop, Object value);
	// 查找对应属性名 name 在当前类中属性并返回
    String findProperty(String name, boolean useCamelCaseMapping);
	// 返回类的所有 get 方法名
    String[] getGetterNames();
	// 返回类的所有 set 方法名
    String[] getSetterNames();
	// 返回对应属性名 name 在当前类中属性的 set 方法的参数类型
    Class<?> getSetterType(String name);
	// 返回对应属性名 name 在当前类中属性的 get 方法的参数类型
    Class<?> getGetterType(String name);
	// 返回是否有对应属性名 name 在当前类中属性的 set 方法
    boolean hasSetter(String name);
	// 返回是否有对应属性名 name 在当前类中属性的 get 方法
    boolean hasGetter(String name);
	// 实例化属性对象
    MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);

    boolean isCollection();

    void add(Object element);

    <E> void addAll(List<E> element);

}

```

* `org.apache.ibatis.reflection.wrapper.ObjectWrapper` 接口
  * `org.apache.ibatis.reflection.wrapper.BaseWrapper` 默认实现抽象类，其中包含了一些对于对象的集合处理方法实现
    * `org.apache.ibatis.reflection.wrapper.BeanWrapper` 对于一些实体 Bean 进行的加工
    * `org.apache.ibatis.reflection.wrapper.MapWrapper` 对于 Map 对象的加工
  * `org.apache.ibatis.reflection.wrapper.CollectionWrapper` 实现类，其包含了对于集合对象的处理实现，许多方法都是不支持的，会抛出 `UnsupportedOperationException` 异常

说起这几个类，不难看出他们是在 reflection 包中，并且实现类的构造函数都有两个参数

* `org.apache.ibatis.reflection.MetaObject`
* `java.lang.Object`

这部分知识在后续讲



## 读取配置

读取配置的代码如下，很简单，就是注册了以下 ObjectWrapperFactory

```java
private void objectWrapperFactoryElement(XNode context) throws Exception {
    if (context != null) {
        String type = context.getStringAttribute("type");
        ObjectWrapperFactory factory = (ObjectWrapperFactory) resolveClass(type).getDeclaredConstructor().newInstance();
        configuration.setObjectWrapperFactory(factory);
    }
}
```



## 自定义

首先我们的目的是对返回值 Map 做一层将带下划线的 key 值变成驼峰命名的封装，会继承到类 BaseWrapper 和 MapWrapper

### BaseWrapper 

```java
public abstract class BaseWrapper implements ObjectWrapper {

    protected static final Object[] NO_ARGUMENTS = new Object[0];
	// 当前对应的 MetaObject
    protected final MetaObject metaObject;

    // 有参构造
    protected BaseWrapper(MetaObject metaObject) {
        this.metaObject = metaObject;
    }
	// 处理集合对象
    protected Object resolveCollection(PropertyTokenizer prop, Object object) {
        // 如果是空串，表示是对象 object 本身
        if ("".equals(prop.getName())) {
            return object;
        // 否则从 ObjectWrapper 中获取对应 prop 在当前类中属性对象
        } else {
            return metaObject.getValue(prop.getName());
        }
    }

    // 获取集合中下标或 key 为 index 的值
    protected Object getCollectionValue(PropertyTokenizer prop, Object collection) {
        if (collection instanceof Map) {
            return ((Map) collection).get(prop.getIndex());
        } else {
            int i = Integer.parseInt(prop.getIndex());
            if (collection instanceof List) {
                return ((List) collection).get(i);
            } else if (collection instanceof Object[]) {
                return ((Object[]) collection)[i];
            } else if (collection instanceof char[]) {
                return ((char[]) collection)[i];
            } else if (collection instanceof boolean[]) {
                return ((boolean[]) collection)[i];
            } else if (collection instanceof byte[]) {
                return ((byte[]) collection)[i];
            } else if (collection instanceof double[]) {
                return ((double[]) collection)[i];
            } else if (collection instanceof float[]) {
                return ((float[]) collection)[i];
            } else if (collection instanceof int[]) {
                return ((int[]) collection)[i];
            } else if (collection instanceof long[]) {
                return ((long[]) collection)[i];
            } else if (collection instanceof short[]) {
                return ((short[]) collection)[i];
            } else {
                throw new ReflectionException("The '" + prop.getName() + "' property of " + collection + " is not a List or Array.");
            }
        }
    }

    // 对集合中下标或 key 为 index 的元素赋值
    protected void setCollectionValue(PropertyTokenizer prop, Object collection, Object value) {
        if (collection instanceof Map) {
            ((Map) collection).put(prop.getIndex(), value);
        } else {
            int i = Integer.parseInt(prop.getIndex());
            if (collection instanceof List) {
                ((List) collection).set(i, value);
            } else if (collection instanceof Object[]) {
                ((Object[]) collection)[i] = value;
            } else if (collection instanceof char[]) {
                ((char[]) collection)[i] = (Character) value;
            } else if (collection instanceof boolean[]) {
                ((boolean[]) collection)[i] = (Boolean) value;
            } else if (collection instanceof byte[]) {
                ((byte[]) collection)[i] = (Byte) value;
            } else if (collection instanceof double[]) {
                ((double[]) collection)[i] = (Double) value;
            } else if (collection instanceof float[]) {
                ((float[]) collection)[i] = (Float) value;
            } else if (collection instanceof int[]) {
                ((int[]) collection)[i] = (Integer) value;
            } else if (collection instanceof long[]) {
                ((long[]) collection)[i] = (Long) value;
            } else if (collection instanceof short[]) {
                ((short[]) collection)[i] = (Short) value;
            } else {
                throw new ReflectionException("The '" + prop.getName() + "' property of " + collection + " is not a List or Array.");
            }
        }
    }

}
```

### MapWrapper

```java
public class MapWrapper extends BaseWrapper {

  private final Map<String, Object> map;

  public MapWrapper(MetaObject metaObject, Map<String, Object> map) {
    super(metaObject);
    this.map = map;
  }

  @Override
  public Object get(PropertyTokenizer prop) {
    if (prop.getIndex() != null) {
      Object collection = resolveCollection(prop, map);
      return getCollectionValue(prop, collection);
    } else {
      return map.get(prop.getName());
    }
  }

  @Override
  public void set(PropertyTokenizer prop, Object value) {
    if (prop.getIndex() != null) {
      Object collection = resolveCollection(prop, map);
      setCollectionValue(prop, collection, value);
    } else {
      map.put(prop.getName(), value);
    }
  }

  @Override
  public String findProperty(String name, boolean useCamelCaseMapping) {
    return name;
  }

  @Override
  public String[] getGetterNames() {
    return map.keySet().toArray(new String[map.keySet().size()]);
  }

  @Override
  public String[] getSetterNames() {
    return map.keySet().toArray(new String[map.keySet().size()]);
  }

  @Override
  public Class<?> getSetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        return Object.class;
      } else {
        return metaValue.getSetterType(prop.getChildren());
      }
    } else {
      if (map.get(name) != null) {
        return map.get(name).getClass();
      } else {
        return Object.class;
      }
    }
  }

  @Override
  public Class<?> getGetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        return Object.class;
      } else {
        return metaValue.getGetterType(prop.getChildren());
      }
    } else {
      if (map.get(name) != null) {
        return map.get(name).getClass();
      } else {
        return Object.class;
      }
    }
  }

  @Override
  public boolean hasSetter(String name) {
    return true;
  }

  @Override
  public boolean hasGetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (map.containsKey(prop.getIndexedName())) {
        MetaObject metaValue = metaObject.metaObjectForProperty(prop.getIndexedName());
        if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
          return true;
        } else {
          return metaValue.hasGetter(prop.getChildren());
        }
      } else {
        return false;
      }
    } else {
      return map.containsKey(prop.getName());
    }
  }

  @Override
  public MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory) {
    HashMap<String, Object> map = new HashMap<>();
    set(prop, map);
    return MetaObject.forObject(map, metaObject.getObjectFactory(), metaObject.getObjectWrapperFactory(), metaObject.getReflectorFactory());
  }

  @Override
  public boolean isCollection() {
    return false;
  }

  @Override
  public void add(Object element) {
    throw new UnsupportedOperationExcepton();
  }

  @Override
  public <E> void addAll(List<E> element) {
    throw new UnsupportedOperationException();
  }
}
```

自定义 MyMapWrapper

```java
public class MyMapWrapper extends MapWrapper {

    public MyMapWrapper(MetaObject metaObject, Map<String, Object> map) {
        super(metaObject, map);
    }

    @Override
    public String findProperty(String name, boolean useCamelCaseMapping) {
        // 此处需要在 settings 里面配置 mapUnderscoreToCamelCase 为 true
        if (useCamelCaseMapping
                && ((name.charAt(0) >= 'A' && name.charAt(0) <= 'Z')
                || name.contains("_"))) {
            return underlineToCamelCase(name);
        }
        return name;
    }
    
    // 将下划线进行驼峰转换
    public String underlineToCamelCase(String inputString) {
        StringBuilder sb = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < inputString.length(); i++) {
            char c = inputString.charAt(i);
            if (c == '_') {
                if (sb.length() > 0) {
                    nextUpperCase = true;
                }
            } else {
                if (nextUpperCase) {
                    sb.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }
}
```

自定义 MapWrapperFactory

```java
public class MapWrapperFactory implements ObjectWrapperFactory {
    @Override
    public boolean hasWrapperFor(Object object) {
        return object instanceof Map;
    }

    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        return new MyMapWrapper(metaObject, (Map<String, Object>)object);
    }
}
```

