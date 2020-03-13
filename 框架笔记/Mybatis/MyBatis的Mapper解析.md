# MyBatis 的 Mapper 解析

## 映射器

既然 MyBatis 的行为已经由上述元素配置完了，我们现在就要定义 SQL 映射语句了。 但是首先我们需要告诉 MyBatis 到哪里去找到这些语句。 Java 在自动查找这方面没有提供一个很好的方法，所以最佳的方式是告诉 MyBatis 到哪里去找映射文件。 你可以使用相对于类路径的资源引用， 或完全限定资源定位符（包括 `file:///` 的 URL），或类名和包名等。这些配置会告诉了 MyBatis 去哪里找映射文件

使用的时候配置有四种方式，按读取顺序排

1. package

配置方法如下

```xml
<mappers>
    <package name="com.yczuoxin.mapper"/>
</mappers>
```

这个方法有个弊端就是要求 XML 文件和 Mapper 接口要在一个包下，Maven 项目的话需要放在 resource 文件下相同路径的包下



2. resource

配置方法如下

```xml
<mappers>
    <mapper resource="BlogMapper.xml"/>
    <mapper resource="BlogMapperExt.xml"/>
</mappers>
```

这个方法显式的指明了要引入的 XML 资源的相对路径



3. url

配置方法如下

```xml
<mappers>
    <mapper url="file:D:\learn\mybatis-test\src\main\resources\BlogMapper.xml"/>
    <mapper url="file:D:\learn\mybatis-test\src\main\resources\BlogMapperExt.xml"/>
</mappers>
```

使用要引入的 XML 资源的完全限定资源定位符



4. class

配置方法如下

```xml
<mappers>
    <mapper class="com.yczuoxin.mapper.BlogMapper"/>
    <mapper class="com.yczuoxin.mapper.BlogMapperExt"/>
</mappers>
```

使用映射器接口实现类的完全限定类名，XML 的资源路径如同 package 的配置一样



### 源码分析

#### MapperRegistry 实现类

```java
public class MapperRegistry {

    private final Configuration config;
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    public MapperRegistry(Configuration config) {
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    public <T> void addMapper(Class<T> type) {
        if (type.isInterface()) {
            // 判断是否已经注册到 knownMappers 中了，如果已经注册过了就会抛出异常
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            boolean loadCompleted = false;
            try {
                // 将 type 作为 key 值，新建一个 MapperProxyFactory 放入到 map 中
                knownMappers.put(type, new MapperProxyFactory<>(type));
                // It's important that the type is added before the parser is run
                // otherwise the binding may automatically be attempted by the
                // mapper parser. If the type is already known, it won't try.
                // 代码见下
                MapperAnnotationBuilder parser = new MapperAnnotationBuilder(config, type);
                parser.parse();
                loadCompleted = true;
            } finally {
                // 如果没有解析成功就从 map 中移除
                if (!loadCompleted) {
                    knownMappers.remove(type);
                }
            }
        }
    }

    /**
   * @since 3.2.2
   */
    public Collection<Class<?>> getMappers() {
        return Collections.unmodifiableCollection(knownMappers.keySet());
    }

    /**
   * @since 3.2.2
   */
    public void addMappers(String packageName, Class<?> superType) {
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
        resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
        Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();
        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }

    /**
   * @since 3.2.2
   */
    public void addMappers(String packageName) {
        addMappers(packageName, Object.class);
    }

}
```

#### MapperAnnotationBuilder 实现类

```java
public class MapperAnnotationBuilder {

    private static final Set<Class<? extends Annotation>> SQL_ANNOTATION_TYPES = new HashSet<>();
    private static final Set<Class<? extends Annotation>> SQL_PROVIDER_ANNOTATION_TYPES = new HashSet<>();

    private final Configuration configuration;
    private final MapperBuilderAssistant assistant;
    private final Class<?> type;

    static {
        SQL_ANNOTATION_TYPES.add(Select.class);
        SQL_ANNOTATION_TYPES.add(Insert.class);
        SQL_ANNOTATION_TYPES.add(Update.class);
        SQL_ANNOTATION_TYPES.add(Delete.class);

        SQL_PROVIDER_ANNOTATION_TYPES.add(SelectProvider.class);
        SQL_PROVIDER_ANNOTATION_TYPES.add(InsertProvider.class);
        SQL_PROVIDER_ANNOTATION_TYPES.add(UpdateProvider.class);
        SQL_PROVIDER_ANNOTATION_TYPES.add(DeleteProvider.class);
    }

    public MapperAnnotationBuilder(Configuration configuration, Class<?> type) {
        String resource = type.getName().replace('.', '/') + ".java (best guess)";
        this.assistant = new MapperBuilderAssistant(configuration, resource);
        this.configuration = configuration;
        this.type = type;
    }

    public void parse() {
        String resource = type.toString();
        if (!configuration.isResourceLoaded(resource)) {
            // 在 package 下找到对应的 xml 文件
            loadXmlResource();
            configuration.addLoadedResource(resource);
            assistant.setCurrentNamespace(type.getName());
            parseCache();
            parseCacheRef();
            Method[] methods = type.getMethods();
            for (Method method : methods) {
                try {
                    // issue #237
                    if (!method.isBridge()) {
                        parseStatement(method);
                    }
                } catch (IncompleteElementException e) {
                    configuration.addIncompleteMethod(new MethodResolver(this, method));
                }
            }
        }
        parsePendingMethods();
    }

    private void parsePendingMethods() {
        Collection<MethodResolver> incompleteMethods = configuration.getIncompleteMethods();
        synchronized (incompleteMethods) {
            Iterator<MethodResolver> iter = incompleteMethods.iterator();
            while (iter.hasNext()) {
                try {
                    iter.next().resolve();
                    iter.remove();
                } catch (IncompleteElementException e) {
                    // This method is still missing a resource
                }
            }
        }
    }

    private void loadXmlResource() {
        // Spring 可能不知道实际的资源名称，因此我们检查一个标志，以防止再次加载一个资源。这个标志设置在		 // XMLMapperBuilder#bindMapperForNamespace
        if (!configuration.isResourceLoaded("namespace:" + type.getName())) {
            String xmlResource = type.getName().replace('.', '/') + ".xml";
            // #1347
            InputStream inputStream = type.getResourceAsStream("/" + xmlResource);
            if (inputStream == null) {
                // 在 classpath 下找 xml 文件
                try {
                    inputStream = Resources.getResourceAsStream(type.getClassLoader(), xmlResource);
                } catch (IOException e2) {
                    // 忽略异常，因为 xml 不是一定要配置的
                }
            }
            if (inputStream != null) {
                // 构造一个 mapper 的 XML 的解析器
                XMLMapperBuilder xmlParser = new XMLMapperBuilder(inputStream, assistant.getConfiguration(), xmlResource, configuration.getSqlFragments(), type.getName());
                // 解析 mapper 的 XML 文件
                xmlParser.parse();
            }
        }
    }
	...
}
```

## 读取配置

```java
private void mapperElement(XNode parent) throws Exception {
    if (parent != null) {
        for (XNode child : parent.getChildren()) {
            if ("package".equals(child.getName())) {
                String mapperPackage = child.getStringAttribute("name");
                configuration.addMappers(mapperPackage);
            } else {
                String resource = child.getStringAttribute("resource");
                String url = child.getStringAttribute("url");
                String mapperClass = child.getStringAttribute("class");
                if (resource != null && url == null && mapperClass == null) {
                    ErrorContext.instance().resource(resource);
                    InputStream inputStream = Resources.getResourceAsStream(resource);
                    XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
                    // 解析 mapper 的 XML
                    mapperParser.parse();
                } else if (resource == null && url != null && mapperClass == null) {
                    ErrorContext.instance().resource(url);
                    InputStream inputStream = Resources.getUrlAsStream(url);
                    XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
                    // 解析 mapper 的 XML
                    mapperParser.parse();
                } else if (resource == null && url == null && mapperClass != null) {
                    Class<?> mapperInterface = Resources.classForName(mapperClass);
                    configuration.addMapper(mapperInterface);
                } else {
                    throw new BuilderException("A mapper element may only specify a url, resource or class, but not more than one.");
                }
            }
        }
    }
}
```