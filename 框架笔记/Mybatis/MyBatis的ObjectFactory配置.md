# MyBatis 的 ObjectFactory 配置

## 对象工厂

MyBatis 每次创建结果对象的新实例时，它都会使用一个对象工厂（ObjectFactory）实例来完成。 默认的对象工厂需要做的仅仅是实例化目标类，要么通过默认构造方法，要么在参数映射存在的时候通过参数构造方法来实例化。 如果想覆盖对象工厂的默认行为，则可以通过创建自己的对象工厂来实现，然后通过以下配置就可以使用了

```xml
<objectFactory type="com.yczuoxin.objectfactory.MyObjectFactory">
    <property name="age" value="18"/>
</objectFactory>
```

### ObjectFactory 接口

```java
public interface ObjectFactory {
	// 默认方法，但是是空实现
    default void setProperties(Properties properties) {
        // NOP
    }

    // 实例化无参构造
    <T> T create(Class<T> type);
    // 实例化有参构造
    <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);
    // 判断是否是集合对象
    <T> boolean isCollection(Class<T> type);

}
```

对象工厂默认是使用的 `org.apache.ibatis.reflection.factory.DefaultObjectFactory`，其源码如下

```java
public class DefaultObjectFactory implements ObjectFactory, Serializable {

    private static final long serialVersionUID = -8855120656740914948L;

    @Override
    public <T> T create(Class<T> type) {
        // 调用无参构造函数实例化一个对象
        return create(type, null, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        Class<?> classToCreate = resolveInterface(type);
        // we know types are assignable
        return (T) instantiateClass(classToCreate, constructorArgTypes, constructorArgs);
    }

    private  <T> T instantiateClass(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        try {
            Constructor<T> constructor;
            if (constructorArgTypes == null || constructorArgs == null) {
                constructor = type.getDeclaredConstructor();
                try {
                    return constructor.newInstance();
                } catch (IllegalAccessException e) {
                    // 如果是权限问题不能实例化对象，就暴力获取权限
                    if (Reflector.canControlMemberAccessible()) {
                        constructor.setAccessible(true);
                        return constructor.newInstance();
                    } else {
                        throw e;
                    }
                }
            }
            // 走到此处说明要使用有参构造
            constructor = type.getDeclaredConstructor(constructorArgTypes.toArray(new Class[constructorArgTypes.size()]));
            try {
                return constructor.newInstance(constructorArgs.toArray(new Object[constructorArgs.size()]));
            } catch (IllegalAccessException e) {
                if (Reflector.canControlMemberAccessible()) {
                    constructor.setAccessible(true);
                    return constructor.newInstance(constructorArgs.toArray(new Object[constructorArgs.size()]));
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            // 此处以下是打印报错信息的代码
            String argTypes = Optional.ofNullable(constructorArgTypes).orElseGet(Collections::emptyList)
                .stream().map(Class::getSimpleName).collect(Collectors.joining(","));
            String argValues = Optional.ofNullable(constructorArgs).orElseGet(Collections::emptyList)
                .stream().map(String::valueOf).collect(Collectors.joining(","));
            throw new ReflectionException("Error instantiating " + type + " with invalid types (" + argTypes + ") or values (" + argValues + "). Cause: " + e, e);
        }
    }

    // 对于集合进行预处理
    protected Class<?> resolveInterface(Class<?> type) {
        Class<?> classToCreate;
        if (type == List.class || type == Collection.class || type == Iterable.class) {
            classToCreate = ArrayList.class;
        } else if (type == Map.class) {
            classToCreate = HashMap.class;
        } else if (type == SortedSet.class) { // issue #510 Collections Support
            classToCreate = TreeSet.class;
        } else if (type == Set.class) {
            classToCreate = HashSet.class;
        } else {
            classToCreate = type;
        }
        return classToCreate;
    }

    @Override
    public <T> boolean isCollection(Class<T> type) {
        // 判断 type 是不是 Collection 或其的子类
        return Collection.class.isAssignableFrom(type);
    }

}
```





## 读取配置

解析部分的源码如下

```java
private void objectFactoryElement(XNode context) throws Exception {
    if (context != null) {
        String type = context.getStringAttribute("type");
        Properties properties = context.getChildrenAsProperties();
        ObjectFactory factory = (ObjectFactory) resolveClass(type).getDeclaredConstructor().newInstance();
        // ObjectFactory 的 default 方法，是一个空实现，个人认为这里是一个扩展点，下面自定义时说明
        factory.setProperties(properties);
        // 替换 DefaultObjectFactory
        configuration.setObjectFactory(factory);
    }
}
```



## 自定义

自定义对象工厂，可以实现 ObjectFactory 接口，也可以继承 DefaultObjectFactory，当然继承方便很多

```java
public class MyObjectFactory extends DefaultObjectFactory {
    // 此处就是设置的配置
    private String age;
    
    @Override
    public Object create(Class type) {
        System.out.println("创建对象方法：" + type);
        if (type.equals(Blog.class)) {
            Blog blog = (Blog) super.create(type);
            // 在此处对构造的对象进行预处理
            blog.setName("object factory : age = " + age);
            blog.setBid(1111);
            blog.setAuthorId(2222);
            return blog;
        }
        Object result = super.create(type);
        return result;
    }

    @Override
    public void setProperties(Properties properties) {
        age = properties.getProperty("age");
    }
}
```