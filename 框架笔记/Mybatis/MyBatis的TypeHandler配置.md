# MyBatis 的 TypeHandler 配置

## 类型处理器

无论是 MyBatis 在预处理语句（PreparedStatement）中设置一个参数时，还是从结果集中取出一个值时， 都会用类型处理器将获取的值以合适的方式转换成 Java 类型。下表描述了一些默认的类型处理器

*从 3.4.5 开始，MyBatis 默认支持 JSR-310（日期和时间 API）*

### TypeHandler< T> 接口和 BaseTypeHandler< T> 默认实现抽象类

```java
public interface TypeHandler<T> {

    // 在构造 PreparedStatement 的时候对传入查询参数进行处理的方法
    void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;
	
    T getResult(ResultSet rs, String columnName) throws SQLException;

    T getResult(ResultSet rs, int columnIndex) throws SQLException;

    T getResult(CallableStatement cs, int columnIndex) throws SQLException;

}
```

```java
public abstract class BaseTypeHandler<T> extends TypeReference<T> implements TypeHandler<T> {

    /**
   	 * 设置时必须手动调用 setConfiguration 才能被设置成功，将在 3.5.0 版本删除该方法
   	 */
    @Deprecated
    protected Configuration configuration;

    /**
   	 * 理由同上
   	 */
    @Deprecated
    public void setConfiguration(Configuration c) {
        this.configuration = c;
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        // 对于参数是 null 的操作
        if (parameter == null) {
            if (jdbcType == null) {
                throw new TypeException("JDBC requires that the JdbcType must be specified for all nullable parameters.");
            }
            try {
                ps.setNull(i, jdbcType.TYPE_CODE);
            } catch (SQLException e) {
                throw new TypeException("Error setting null for parameter #" + i + " with JdbcType " + jdbcType + " . "
                                        + "Try setting a different JdbcType for this parameter or a different jdbcTypeForNull configuration property. "
                                        + "Cause: " + e, e);
            }
        } else {
            // 对于参数不是 null 的操作
            try {
                setNonNullParameter(ps, i, parameter, jdbcType);
            } catch (Exception e) {
                throw new TypeException("Error setting non null for parameter #" + i + " with JdbcType " + jdbcType + " . "
                                        + "Try setting a different JdbcType for this parameter or a different configuration property. "
                                        + "Cause: " + e, e);
            }
        }
    }

    @Override
    public T getResult(ResultSet rs, String columnName) throws SQLException {
        try {
            return getNullableResult(rs, columnName);
        } catch (Exception e) {
            throw new ResultMapException("Error attempting to get column '" + columnName + "' from result set.  Cause: " + e, e);
        }
    }

    @Override
    public T getResult(ResultSet rs, int columnIndex) throws SQLException {
        try {
            return getNullableResult(rs, columnIndex);
        } catch (Exception e) {
            throw new ResultMapException("Error attempting to get column #" + columnIndex + " from result set.  Cause: " + e, e);
        }
    }

    @Override
    public T getResult(CallableStatement cs, int columnIndex) throws SQLException {
        try {
            return getNullableResult(cs, columnIndex);
        } catch (Exception e) {
            throw new ResultMapException("Error attempting to get column #" + columnIndex + " from callable statement.  Cause: " + e, e);
        }
    }

    public abstract void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

    public abstract T getNullableResult(ResultSet rs, String columnName) throws SQLException;

    public abstract T getNullableResult(ResultSet rs, int columnIndex) throws SQLException;

    public abstract T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException;

}
```



## 读取配置

读取配置的源码如下

```java
private void typeHandlerElement(XNode parent) {
    if (parent != null) {
        for (XNode child : parent.getChildren()) {
            if ("package".equals(child.getName())) {
                String typeHandlerPackage = child.getStringAttribute("name");
                typeHandlerRegistry.register(typeHandlerPackage);
            } else {
                String javaTypeName = child.getStringAttribute("javaType");
                String jdbcTypeName = child.getStringAttribute("jdbcType");
                String handlerTypeName = child.getStringAttribute("handler");
                // 解析传入的 javaType，别名也是在这里处理的
                Class<?> javaTypeClass = resolveClass(javaTypeName);
                // 解析传入的 jdbcType，会校验 jdbcType 传入是否有误，有误抛出异常
                JdbcType jdbcType = resolveJdbcType(jdbcTypeName);
                Class<?> typeHandlerClass = resolveClass(handlerTypeName);
                // 如果不配置 javaType 的话，就会读取类中的泛型变量，而并不是不生效
                if (javaTypeClass != null) {
                    if (jdbcType == null) {
                        typeHandlerRegistry.register(javaTypeClass, typeHandlerClass);
                    } else {
                        typeHandlerRegistry.register(javaTypeClass, jdbcType, typeHandlerClass);
                    }
                } else {
                    // jdbcType 配置没效果，除非使用了 @MappedJdbcTypes 注解
                    typeHandlerRegistry.register(typeHandlerClass);
                }
            }
        }
    }
}
```

其内置了部分类型处理，代码如下

```java
public final class TypeHandlerRegistry {

  // jdbc 类型转换器
  private final Map<JdbcType, TypeHandler<?>>  jdbcTypeHandlerMap = new EnumMap<>(JdbcType.class);
  private final Map<Type, Map<JdbcType, TypeHandler<?>>> typeHandlerMap = new ConcurrentHashMap<>();
  private final TypeHandler<Object> unknownTypeHandler = new UnknownTypeHandler(this);
  private final Map<Class<?>, TypeHandler<?>> allTypeHandlersMap = new HashMap<>();

  private static final Map<JdbcType, TypeHandler<?>> NULL_TYPE_HANDLER_MAP = Collections.emptyMap();

  private Class<? extends TypeHandler> defaultEnumTypeHandler = EnumTypeHandler.class;

  // 注册默认的 java 类型转换器
  public TypeHandlerRegistry() {
    register(Boolean.class, new BooleanTypeHandler());
    register(boolean.class, new BooleanTypeHandler());
    register(JdbcType.BOOLEAN, new BooleanTypeHandler());
    register(JdbcType.BIT, new BooleanTypeHandler());

    register(Byte.class, new ByteTypeHandler());
    register(byte.class, new ByteTypeHandler());
    register(JdbcType.TINYINT, new ByteTypeHandler());

    register(Short.class, new ShortTypeHandler());
    register(short.class, new ShortTypeHandler());
    register(JdbcType.SMALLINT, new ShortTypeHandler());

    register(Integer.class, new IntegerTypeHandler());
    register(int.class, new IntegerTypeHandler());
    register(JdbcType.INTEGER, new IntegerTypeHandler());

    register(Long.class, new LongTypeHandler());
    register(long.class, new LongTypeHandler());

    register(Float.class, new FloatTypeHandler());
    register(float.class, new FloatTypeHandler());
    register(JdbcType.FLOAT, new FloatTypeHandler());

    register(Double.class, new DoubleTypeHandler());
    register(double.class, new DoubleTypeHandler());
    register(JdbcType.DOUBLE, new DoubleTypeHandler());

    register(Reader.class, new ClobReaderTypeHandler());
    register(String.class, new StringTypeHandler());
    register(String.class, JdbcType.CHAR, new StringTypeHandler());
    register(String.class, JdbcType.CLOB, new ClobTypeHandler());
    register(String.class, JdbcType.VARCHAR, new StringTypeHandler());
    register(String.class, JdbcType.LONGVARCHAR, new StringTypeHandler());
    register(String.class, JdbcType.NVARCHAR, new NStringTypeHandler());
    register(String.class, JdbcType.NCHAR, new NStringTypeHandler());
    register(String.class, JdbcType.NCLOB, new NClobTypeHandler());
    register(JdbcType.CHAR, new StringTypeHandler());
    register(JdbcType.VARCHAR, new StringTypeHandler());
    register(JdbcType.CLOB, new ClobTypeHandler());
    register(JdbcType.LONGVARCHAR, new StringTypeHandler());
    register(JdbcType.NVARCHAR, new NStringTypeHandler());
    register(JdbcType.NCHAR, new NStringTypeHandler());
    register(JdbcType.NCLOB, new NClobTypeHandler());

    register(Object.class, JdbcType.ARRAY, new ArrayTypeHandler());
    register(JdbcType.ARRAY, new ArrayTypeHandler());

    register(BigInteger.class, new BigIntegerTypeHandler());
    register(JdbcType.BIGINT, new LongTypeHandler());

    register(BigDecimal.class, new BigDecimalTypeHandler());
    register(JdbcType.REAL, new BigDecimalTypeHandler());
    register(JdbcType.DECIMAL, new BigDecimalTypeHandler());
    register(JdbcType.NUMERIC, new BigDecimalTypeHandler());

    register(InputStream.class, new BlobInputStreamTypeHandler());
    register(Byte[].class, new ByteObjectArrayTypeHandler());
    register(Byte[].class, JdbcType.BLOB, new BlobByteObjectArrayTypeHandler());
    register(Byte[].class, JdbcType.LONGVARBINARY, new BlobByteObjectArrayTypeHandler());
    register(byte[].class, new ByteArrayTypeHandler());
    register(byte[].class, JdbcType.BLOB, new BlobTypeHandler());
    register(byte[].class, JdbcType.LONGVARBINARY, new BlobTypeHandler());
    register(JdbcType.LONGVARBINARY, new BlobTypeHandler());
    register(JdbcType.BLOB, new BlobTypeHandler());

    register(Object.class, unknownTypeHandler);
    register(Object.class, JdbcType.OTHER, unknownTypeHandler);
    register(JdbcType.OTHER, unknownTypeHandler);

    register(Date.class, new DateTypeHandler());
    register(Date.class, JdbcType.DATE, new DateOnlyTypeHandler());
    register(Date.class, JdbcType.TIME, new TimeOnlyTypeHandler());
    register(JdbcType.TIMESTAMP, new DateTypeHandler());
    register(JdbcType.DATE, new DateOnlyTypeHandler());
    register(JdbcType.TIME, new TimeOnlyTypeHandler());

    register(java.sql.Date.class, new SqlDateTypeHandler());
    register(java.sql.Time.class, new SqlTimeTypeHandler());
    register(java.sql.Timestamp.class, new SqlTimestampTypeHandler());

    register(String.class, JdbcType.SQLXML, new SqlxmlTypeHandler());

    register(Instant.class, new InstantTypeHandler());
    register(LocalDateTime.class, new LocalDateTimeTypeHandler());
    register(LocalDate.class, new LocalDateTypeHandler());
    register(LocalTime.class, new LocalTimeTypeHandler());
    register(OffsetDateTime.class, new OffsetDateTimeTypeHandler());
    register(OffsetTime.class, new OffsetTimeTypeHandler());
    register(ZonedDateTime.class, new ZonedDateTimeTypeHandler());
    register(Month.class, new MonthTypeHandler());
    register(Year.class, new YearTypeHandler());
    register(YearMonth.class, new YearMonthTypeHandler());
    register(JapaneseDate.class, new JapaneseDateTypeHandler());

    // issue #273
    register(Character.class, new CharacterTypeHandler());
    register(char.class, new CharacterTypeHandler());
  }
}
```



配置的方式有两种

1. 使用 package 的方式让 MyBatis 去扫描包下的 TypeHandler

```xml
<typeHandlers>
    <package name="com.yczuoxin.type"/>
</typeHandlers>
```

其注册到类型处理器的源码如下

```java
public void register(String packageName) {
    ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
    // 底层使用 Class 的 isAssignableFrom() 过滤 TypeHandler 是传入 class 相同或是其父类的 class
    resolverUtil.find(new ResolverUtil.IsA(TypeHandler.class), packageName);
    Set<Class<? extends Class<?>>> handlerSet = resolverUtil.getClasses();
    for (Class<?> type : handlerSet) {
        //Ignore inner classes and interfaces (including package-info.java) and abstract classes
        // 注册的时候会跳过三种类型的文件，内部类，接口和抽象类
        if (!type.isAnonymousClass() && !type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
            // 调用下面的方法
            register(type);
        }
    }
}

public void register(Class<?> typeHandlerClass) {
    boolean mappedTypeFound = false;
    // 获取类上的 @MappedTypes 注解，优先级最高
    MappedTypes mappedTypes = typeHandlerClass.getAnnotation(MappedTypes.class);
    // 处理有 @MappedTypes 注解的类
    if (mappedTypes != null) {
        // 此处可以看出注解是支持 javaType 数组传递的
        for (Class<?> javaTypeClass : mappedTypes.value()) {
            register(javaTypeClass, typeHandlerClass);
            mappedTypeFound = true;
        }
    }
    // 处理没有 @MappedTypes 注解的类
    if (!mappedTypeFound) {
        // 调用下面的方法，此处 getInstance() 是利用 Constractor.newInstance() 构造实例，但是 			// TypeReference 的构造函数会将泛型变量提取出来，作为自己的一个属性放在 rawType 属性字段中，并		// 返回，泛型变量，即 <> 中的类型
        register(getInstance(null, typeHandlerClass));
    }
}

public <T> void register(TypeHandler<T> typeHandler) {
    boolean mappedTypeFound = false;
    MappedTypes mappedTypes = typeHandler.getClass().getAnnotation(MappedTypes.class);
    // 有 @MappedTypes 注解的类
    if (mappedTypes != null) {
        for (Class<?> handledType : mappedTypes.value()) {
            register(handledType, typeHandler);
            mappedTypeFound = true;
        }
    }
    // @since 3.1.0 - try to auto-discover the mapped type
    // TypeReference 是 BaseTypeHandler 的父类
    if (!mappedTypeFound && typeHandler instanceof TypeReference) {
        try {
            TypeReference<T> typeReference = (TypeReference<T>) typeHandler;
            register(typeReference.getRawType(), typeHandler);
            mappedTypeFound = true;
        } catch (Throwable t) {
            // maybe users define the TypeReference with a different type and are not assignable, so just ignore it
        }
    }
    if (!mappedTypeFound) {
        register((Class<T>) null, typeHandler);
    }
}
```



2. 使用 javaType，jdbcType，handler 属性配置 TypeHandler

```xml
<typeHandlers>
    <!-- handler 属性直接配置我们要指定的 TypeHandler -->
    <typeHandler handler=""/>
    <!-- javaType 配置 java 类型，例如String -->
    <typeHandler javaType="" handler=""/>
    <!-- jdbcType 配置数据库基本数据类型，例如varchar，但是 jdbcType 不会生效 -->
    <typeHandler jdbcType="" handler=""/>
    <!-- 也可两者都配置 -->
    <typeHandler javaType="" jdbcType="" handler=""/>
</typeHandlers>
```

从源码中可以看出，在这几种配置中，有配置 javaType 的和没有配置 javaType 的分为了两大类

* 有 javaType
  * 有 jdbcType
    * 会将 jdbcType 和对应的 handler 存到 typeHandlerMap 中 key 为 javaType 的 value 中，其 value 依然为一个 Map，key 为 jdbcType 而 value 为 handler
  * 没有 jdbcType
    * 和上面一样，只不过 jdbcType 会为 null 值
* 没有 javaType
  * 有 jdbcType
    * 跟没有 jdbc 一样
  * 没有 jdbcType
    * 根有 javaType 一样，只不过 type 会读取 handler 的泛型变量



## 自定义

```java
public class MyTypeHandler extends BaseTypeHandler<String> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        // 设置 String 类型的参数的时候调用，Java类型到JDBC类型
        // 注意只有在字段上添加 typeHandler 属性才会生效
        // insertBlog name 字段
        System.out.println("--------------- setNonNullParameter1："+parameter);
        ps.setString(i, parameter);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 根据列名获取 String 类型的参数的时候调用，JDBC 类型到 java 类型
        // 注意只有在字段上添加 typeHandler 属性才会生效
        System.out.println("--------------- getNullableResult1："+columnName);
        return rs.getString(columnName);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 根据下标获取 String 类型的参数的时候调用
        System.out.println("--------------- getNullableResult2："+columnIndex);
        return rs.getString(columnIndex);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        System.out.println("--------------- getNullableResult3：");
        return cs.getString(columnIndex);
    }
}
```

