# MyBatis 缓存

MyBatis 是现在国内比较流行的 ORM 框架，在学习 MyBatis 的时候，不得不了解 MyBatis 的两级缓存，要了解 MyBatis 的缓存，先要了解 MyBatis 几个重要的对象

* SqlSession - 对应的一次数据库会话，由 SqlSessionFactory 的 openSession 创建，一次会话并不代表只能执行一条 SQL
* MappedStatement - 存储了 SQL 对应的所有信息，XMLStatementBuilder 解析 XML 或者注解的时候，由 parseStatementNode 方法生成，放入到 configuration 中保存
* Executor - 真正对数据库操作的对象，由 Configuration 的 newExecutor 创建
* namespace - 用来区分 sql 命令，和 statementid 一起生成的 key 值作为 sql 的唯一标识



## MyBatis 一级缓存

首先，一级缓存的配置有两种

* SESSION（默认）
* STATEMENT

```properties
<configuration>
    <settings>
    	<setting name="localCacheScope" value="SESSION"/>
    </settings>
<configuration>
```

所以 MyBatis 的一级缓存可以是 SqlSession 级别的，也可以是 Statement 级别的



### 原理

当客户端执行 SQL 的时候，会将查询结果封装到 SqlSession 的 Executor（BaseExecutor） 中的 localCache 属性中（Executor 的 query 方法），其底层是一个 HashMap

```java
protected PerpetualCache localCache;

private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    List<E> list;
    localCache.putObject(key, EXECUTION_PLACEHOLDER);
    try {
      list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
    } finally {
      localCache.removeObject(key);
    }
    // 放入缓存
    localCache.putObject(key, list);
    if (ms.getStatementType() == StatementType.CALLABLE) {
      localOutputParameterCache.putObject(key, parameter);
    }
    return list;
}
```

key 值为 MappedStatementId + Offset + Limit + SQL + SQL 中的参数一起构成 CacheKey（Executor 的 createCacheKey 方法），生成 Key 的方法

```java
  @Override
  public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }
    CacheKey cacheKey = new CacheKey();
    cacheKey.update(ms.getId());
    cacheKey.update(rowBounds.getOffset());
    cacheKey.update(rowBounds.getLimit());
    cacheKey.update(boundSql.getSql());
    List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
    TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();
    // mimic DefaultParameterHandler logic
    for (ParameterMapping parameterMapping : parameterMappings) {
      if (parameterMapping.getMode() != ParameterMode.OUT) {
        Object value;
        String propertyName = parameterMapping.getProperty();
        if (boundSql.hasAdditionalParameter(propertyName)) {
          value = boundSql.getAdditionalParameter(propertyName);
        } else if (parameterObject == null) {
          value = null;
        } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
          value = parameterObject;
        } else {
          MetaObject metaObject = configuration.newMetaObject(parameterObject);
          value = metaObject.getValue(propertyName);
        }
        cacheKey.update(value);
      }
    }
    if (configuration.getEnvironment() != null) {
      // issue #176
      cacheKey.update(configuration.getEnvironment().getId());
    }
    return cacheKey;
  }
```

作为在市场叱咤了这么多年的框架，当然会考虑在数据更新之后查到缓存的问题，所以在更新数据的时候会将缓存清除（此处是无差别攻击）

```java
@Override
public int update(MappedStatement ms, Object parameter) throws SQLException {
    ErrorContext.instance().resource(ms.getResource()).activity("executing an update").object(ms.getId());
    if (closed) {
      throw new ExecutorException("Executor was closed.");
    }
    // 清除缓存
    clearLocalCache();
    return doUpdate(ms, parameter);
}
```

如果想跳过一级缓存，可以配置 `<select flushCache = ture>`  即可



### 思考

现在系统都是分布式集群，这种一级缓存应该也是有问题的



## MyBatis 二级缓存

配置方式

* config 配置

```properties
<settings>
     <setting name="cacheEnabled" value="true"/>
</settings>
```

* Mapper.xml 配置

```xml
<cache />
```

* mapper 接口

```java
@Mapper
@CacheNamespace // 接口级别
public interface TestDao {
    @Options(useCache = true) // 方法级别
    @Select({"select * from test"})
    Test getTest();
}
```

* statement 语句中配置

```xml
<select id ="xxx" useCache="true"> ... </select>
```

可以理解 MyBatis 的二级缓存是 namespace 级别或者可以理解是 mapper 级别的



### 原理

MyBatis 的二级缓存是可以扩展很多的，它的核心接口是 `org.apache.ibatis.cache.Cache`

```java
public interface Cache {

  /**
   * @return The identifier of this cache
   */
  String getId();

  /**
   * @param key
   *          Can be any object but usually it is a {@link CacheKey}
   * @param value
   *          The result of a select.
   */
  void putObject(Object key, Object value);

  /**
   * @param key
   *          The key
   * @return The object stored in the cache.
   */
  Object getObject(Object key);

  /**
   * As of 3.3.0 this method is only called during a rollback
   * for any previous value that was missing in the cache.
   * This lets any blocking cache to release the lock that
   * may have previously put on the key.
   * A blocking cache puts a lock when a value is null
   * and releases it when the value is back again.
   * This way other threads will wait for the value to be
   * available instead of hitting the database.
   *
   *
   * @param key
   *          The key
   * @return Not used
   */
  Object removeObject(Object key);

  /**
   * Clears this cache instance.
   */
  void clear();

  /**
   * Optional. This method is not called by the core.
   *
   * @return The number of elements stored in the cache (not its capacity).
   */
  int getSize();

  /**
   * Optional. As of 3.2.6 this method is no longer called by the core.
   * <p>
   * Any locking needed by the cache must be provided internally by the cache provider.
   *
   * @return A ReadWriteLock
   */
  default ReadWriteLock getReadWriteLock() {
    return null;
  }

```

如果开启了二级缓存，最后执行的是 CachingExecutor，但是它其实是将 BaseExecutor 包装了一层的实现

```java
public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Executor executor;
    if (ExecutorType.BATCH == executorType) {
      	executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
      	executor = new ReuseExecutor(this, transaction);
    } else {
      	executor = new SimpleExecutor(this, transaction);
    }
    if (cacheEnabled) {
        // 传入 BaseExecutor 进行包装
      	executor = new CachingExecutor(executor);
    }
    executor = (Executor) interceptorChain.pluginAll(executor);
    return executor;
}
```

二级缓存存储代码

```java
private final TransactionalCacheManager tcm = new TransactionalCacheManager();

@Override
public <E> List<E> query(MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql)
  throws SQLException {
    // 此处需要于 MappedStatement 绑定的 Cache，如果打了标签默认是 
    Cache cache = ms.getCache();
    if (cache != null) {
      	flushCacheIfRequired(ms);
      	if (ms.isUseCache() && resultHandler == null) {
        	ensureNoOutParams(ms, boundSql);
        	@SuppressWarnings("unchecked")
            // 先查询的是二级缓存
        	List<E> list = (List<E>) tcm.getObject(cache, key);
        	if (list == null) {
                // 这里是调用 BaseExecutor 的 query 方法
          		list = delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
                // 此处是放入二级缓存
          		tcm.putObject(cache, key, list); // issue #578 and #116
        	}
        	return list;
      	}
    }
    return delegate.query(ms, parameterObject, rowBounds, resultHandler, key, boundSql);
}
```

其中 TransactionalCacheManager 中的缓存属性为

```java
// TransactionCache是装饰器对象，对Cache进行增强
private final Map<Cache, TransactionalCache> transactionalCaches = new HashMap<>();
```

TranactionalCache 的中缓存的属性为

```java
public class TransactionalCache implements Cache {

    private static final Log log = LogFactory.getLog(TransactionalCache.class);
    // 被增强的Cache
    private final Cache delegate;
    // 提交事务时，清空缓存的标识
    private boolean clearOnCommit;
    // 待提交的数据（只有在事务提交时，才会将数据存放在二级缓存中）
    private final Map<Object, Object> entriesToAddOnCommit;
    // 缓存中没有命中的数据
    private final Set<Object> entriesMissedInCache;
  ...
}
```

默认的 Cache 是在构建器 XMLMapperBuilder 解析 mapper 的时候动态插入的

```java
private void cacheElement(XNode context) {
    if (context != null) {
      	String type = context.getStringAttribute("type", "PERPETUAL");
      	Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);
      	String eviction = context.getStringAttribute("eviction", "LRU");
      	Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);
      	Long flushInterval = context.getLongAttribute("flushInterval");
      	Integer size = context.getIntAttribute("size");
      	boolean readWrite = !context.getBooleanAttribute("readOnly", false);
      	boolean blocking = context.getBooleanAttribute("blocking", false);
      	Properties props = context.getChildrenAsProperties();
		// 此处构建对应二级缓存的 Cache
      	builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, props);
    }
}
```

构建 Cache 的类型为被层层包装过了的 Cache

```java
public Cache useNewCache(Class<? extends Cache> typeClass,
  Class<? extends Cache> evictionClass,
  Long flushInterval,
  Integer size,
  boolean readWrite,
  boolean blocking,
  Properties props) {
    Cache cache = new CacheBuilder(currentNamespace)
        .implementation(valueOrDefault(typeClass, PerpetualCache.class))
        .addDecorator(valueOrDefault(evictionClass, LruCache.class))
        .clearInterval(flushInterval)
        .size(size)
        .readWrite(readWrite)
        .blocking(blocking)
        .properties(props)
        .build();
    configuration.addCache(cache);
    currentCache = cache;
    return cache;
}

public Cache build() {
    setDefaultImplementations();
    Cache cache = newBaseCacheInstance(implementation, id);
    setCacheProperties(cache);
    // issue #352, do not apply decorators to custom caches
    if (PerpetualCache.class.equals(cache.getClass())) {
      	for (Class<? extends Cache> decorator : decorators) {
        	cache = newCacheDecoratorInstance(decorator, cache);
        	setCacheProperties(cache);
      	}
      	cache = setStandardDecorators(cache);
    } else if (!LoggingCache.class.isAssignableFrom(cache.getClass())) {
      	cache = new LoggingCache(cache);
    }
    return cache;
}

// 这里将 Cache 一层一层往里面包装，看方法名称也知道是装饰器模式加强
private Cache setStandardDecorators(Cache cache) {
    try {
      	MetaObject metaCache = SystemMetaObject.forObject(cache);
      	if (size != null && metaCache.hasSetter("size")) {
        	metaCache.setValue("size", size);
      	}
      	if (clearInterval != null) {
        	cache = new ScheduledCache(cache);
        	((ScheduledCache) cache).setClearInterval(clearInterval);
      	}
      	if (readWrite) {
        	cache = new SerializedCache(cache);
      	}
      	cache = new LoggingCache(cache);
      	cache = new SynchronizedCache(cache);
      	if (blocking) {
        	cache = new BlockingCache(cache);
      	}
      	return cache;
    } catch (Exception e) {
      throw new CacheException("Error building standard cache decorators.  Cause: " + e, e);
    }
}
```



### 思考

二级缓存使用装饰者模式对 BaseExecutor 的方法进行增强，这种编码风格在日常编码中也可以使用