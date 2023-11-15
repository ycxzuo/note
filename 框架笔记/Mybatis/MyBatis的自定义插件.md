# MyBatis的自定义插件

## 前置知识

MyBatis 可以拦截的四大组件

* Executor - 执行器
* StatementHandler - SQL 语句构造器
* ParameterHandler - 参数处理器
* ResultSetHandler - 结果集处理器



## 自定义 MyBatis 插件

```java
/**
 * 打印 sql 执行的时间插件
 */
@Intercepts(
	// 指定拦截器拦截的对象、方法和参数类型
    {@Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
    @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
    @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})}
)
// 注册到 Spring 容器，不是 Spring 环境的话可以用 mybatis 的 config 配置进去
@Component
public class SqlExecuteTimePrintMybatisPlugin implements Interceptor {

    protected Logger logger = LoggerFactory.getLogger(SqlExecuteTimePrintMybatisPlugin.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取代理对象
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        // 获取执行 sql
        BoundSql boundSql = statementHandler.getBoundSql();
        // 此处简单处理一下，只打印参数替换前的 sql，目的是演示自定义插件
        String sql = boundSql.getSql();
        long start = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            logger.info("sql -> {}, takes time -> {}", sql, System.currentTimeMillis() - start);
        }
    }
}
```

效果如下

```properties
2023-10-14 17:18:39.297  INFO 25972 --- [p-nio-80-exec-1] c.y.m.c.SqlExecuteTimePrintMybatisPlugin : sql -> SELECT * FROM `INFO` WHERE `id` = ? , takes time -> 57
2023-10-14 17:18:39.324  INFO 25972 --- [p-nio-80-exec-1] c.y.m.c.SqlExecuteTimePrintMybatisPlugin : sql -> SELECT `id`, `info_id`, `extend_info` FROM `INFO_DETAIL` WHERE `info_id` = ?, takes time -> 4
```



## 源码解析

创建四大对象的代码如下

```java
public class Configuration {
    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
        // 此处增加拦截器责任链
        parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
        return parameterHandler;
    }

    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler,ResultHandler resultHandler, BoundSql boundSql) {
        ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
        // 此处增加拦截器责任链
        resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
        return resultSetHandler;
    }

    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
        // 此处增加拦截器责任链
        statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
        return statementHandler;
    }

    public Executor newExecutor(Transaction transaction) {
    	return newExecutor(transaction, defaultExecutorType);
    }

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
        	executor = new CachingExecutor(executor);
        }
        // 此处增加拦截器责任链
        executor = (Executor) interceptorChain.pluginAll(executor);
        return executor;
    }
}
```

1. 首先在创建 Executor、StatementHandler、ParameterHandler、ResultSetHandler 四个对象时，将插件（plugins）注入

2. 调用 InterceptorChain.pluginAll() 方法将插件增加到责任链，并返回代理后的 target 包装对象，InterceptorChain 保存了所有的拦截器（Interceptors）
3. 最终在执行的时候调用的其实是 JDK 动态代理的对象，执行 MyBatis 中 `InvocationHandler` 的实现 `org.apache.ibatis.plugin.Plugin` 的 invoke 方法

```java
public class InterceptorChain {

    private final List<Interceptor> interceptors = new ArrayList<>();

    public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
     	target = interceptor.plugin(target);
    }
    	return target;
    }
}
```

```java
public interface Interceptor {
	// 拦截器增强方法
    Object intercept(Invocation invocation) throws Throwable;
	// 包装原来的对象
    default Object plugin(Object target) {
    	return Plugin.wrap(target, this);
    }

    default void setProperties(Properties properties) {
    	// NOP
    }

}
```

```java
public class Plugin implements InvocationHandler {

    private final Object target;
    private final Interceptor interceptor;
    private final Map<Class<?>, Set<Method>> signatureMap;

    private Plugin(Object target, Interceptor interceptor, Map<Class<?>, Set<Method>> signatureMap) {
        this.target = target;
        this.interceptor = interceptor;
        this.signatureMap = signatureMap;
    }

    public static Object wrap(Object target, Interceptor interceptor) {
    Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
        Class<?> type = target.getClass();
        Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
        if (interfaces.length > 0) {
		// jdk 动态代理
        return Proxy.newProxyInstance(
            type.getClassLoader(),
            interfaces,
            new Plugin(target, interceptor, signatureMap));
        }
        return target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            // 获取插件中生命要增强的方法
            Set<Method> methods = signatureMap.get(method.getDeclaringClass());
            // 如果命中该方法，就使用执行插件中增强的方法
            if (methods != null && methods.contains(method)) {
            	return interceptor.intercept(new Invocation(target, method, args));
            }
            // 没有命中就不对方法进行增强
            return method.invoke(target, args);
        } catch (Exception e) {
            throw ExceptionUtil.unwrapThrowable(e);
        }
    }
    ...
}
```



## 备注

> 不明白的需要去看下 JDK 动态代理实现原理，概括的来讲就是 `Proxy#newProxyInstance` 时，通过字节码增强的方法，生成一个实现了跟被代理类相同接口并继承了 `java.lang.reflect.Proxy` 的类并返回其实例，调用这个代理类的方法时，实际上调用的是 Proxy.InvocationHandler.invoke(this, method, new Object[]{args}) 方法