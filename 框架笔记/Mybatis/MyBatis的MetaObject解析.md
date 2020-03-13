# Mybatis 的 MetaObject 解析

## MetaObject 源码

源码部分

```java
public class MetaObject {

    private final Object originalObject;
    private final ObjectWrapper objectWrapper;
    private final ObjectFactory objectFactory;
    private final ObjectWrapperFactory objectWrapperFactory;
    private final ReflectorFactory reflectorFactory;
	// 私有构造函数
    private MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
        this.originalObject = object;
        this.objectFactory = objectFactory;
        this.objectWrapperFactory = objectWrapperFactory;
        this.reflectorFactory = reflectorFactory;
		// 如果参数对象实现了 ObjectWrapper 接口
        if (object instanceof ObjectWrapper) {
            this.objectWrapper = (ObjectWrapper) object;
        // 如果 ObjectWrapperFactory 对此对象进行加工，调用 getWrapperFor 方法获取加工对象
        } else if (objectWrapperFactory.hasWrapperFor(object)) {
            this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
        // 如果是 Map 对象，则使用 MapWrapper 作为加工对象
        } else if (object instanceof Map) {
            this.objectWrapper = new MapWrapper(this, (Map) object);
        // 如果是 Collection 对象，则使用 CollectionWrapper 作为加工对象
        } else if (object instanceof Collection) {
            this.objectWrapper = new CollectionWrapper(this, (Collection) object);
        // 其他默认使用 BeanWrapper 作为加工对象
        } else {
            this.objectWrapper = new BeanWrapper(this, object);
        }
    }
	// 调用私有构造方法
    public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
        if (object == null) {
            return SystemMetaObject.NULL_META_OBJECT;
        } else {
            return new MetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
        }
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }

    public ReflectorFactory getReflectorFactory() {
        return reflectorFactory;
    }

    public Object getOriginalObject() {
        return originalObject;
    }

    public String findProperty(String propName, boolean useCamelCaseMapping) {
        return objectWrapper.findProperty(propName, useCamelCaseMapping);
    }

    public String[] getGetterNames() {
        return objectWrapper.getGetterNames();
    }

    public String[] getSetterNames() {
        return objectWrapper.getSetterNames();
    }

    public Class<?> getSetterType(String name) {
        return objectWrapper.getSetterType(name);
    }

    public Class<?> getGetterType(String name) {
        return objectWrapper.getGetterType(name);
    }

    public boolean hasSetter(String name) {
        return objectWrapper.hasSetter(name);
    }

    public boolean hasGetter(String name) {
        return objectWrapper.hasGetter(name);
    }

    public Object getValue(String name) {
        // 先经过属性表达式解析
        PropertyTokenizer prop = new PropertyTokenizer(name);
        // 如果有下一级属性
        if (prop.hasNext()) {
            // 构造其 MetaObject 对象
            MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
            // 如果与 SystemMetaObject.NULL_META_OBJECT 相等，即传入的 NullObject.class
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                return null;
            // 否则递归调用
            } else {
                return metaValue.getValue(prop.getChildren());
            }
        } else {
            return objectWrapper.get(prop);
        }
    }

    public void setValue(String name, Object value) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                // 如果值是 null，就不用初始化下一级元素了
                if (value == null) {
                    // don't instantiate child path if value is null
                    return;
                // 否则初始化 metaValue，然后递归调用
                } else {
                    metaValue = objectWrapper.instantiatePropertyValue(name, prop, objectFactory);
                }
            }
            metaValue.setValue(prop.getChildren(), value);
        } else {
            // 给指定找到最后一个对象，对其赋值
            objectWrapper.set(prop, value);
        }
    }
	// 根据传入 name 值构造 MateObject 对象
    public MetaObject metaObjectForProperty(String name) {
        Object value = getValue(name);
        return MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
    }

    public ObjectWrapper getObjectWrapper() {
        return objectWrapper;
    }

    public boolean isCollection() {
        return objectWrapper.isCollection();
    }

    public void add(Object element) {
        objectWrapper.add(element);
    }

    public <E> void addAll(List<E> list) {
        objectWrapper.addAll(list);
    }

}
```



## 测试类

```java
@Test
public void setValue() {
    Blog blog = new Blog();
    MetaObject meta = SystemMetaObject.forObject(blog);
    meta.setValue("bid", "1");
    assertEquals("1", meta.getValue("bid"));
}
```

`SystemMetaObject.forObject()` 方法是采用默认的 `DefaultObjectFactory`、`DefaultObjectWrapperFactory` 和 `DefaultReflectorFactory` 作为 `MetaObject` 的构造函数的入参，可以 Debug 进去一步一步看清楚是如何一步一步为实例对象赋值的



## MetaObject 作用

可以看到 `MetaObject` 主要是对实例化的对象进行赋值和取值用的，其底层也是利用的反射获取实例的 getter 和 setter 方法进行赋值，而这些 getter 和 setter 方法其实都是和 `MetaObject` 名类似的 `MetaClass` 通过反射去获取的，所以 `MetaClass` 主要是用来保存 Class 的一些元信息的



