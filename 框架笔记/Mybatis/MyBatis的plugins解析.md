# MyBatis 的 plugins 解析

## 插件

MyBatis 允许你在已映射语句执行过程中的某一点进行拦截调用。默认情况下，MyBatis 允许使用插件来拦截的方法调用包括：

- Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
- ParameterHandler (getParameterObject, setParameters)
- ResultSetHandler (handleResultSets, handleOutputParameters)
- StatementHandler (prepare, parameterize, batch, update, query)

其配置方式如下

```xml
<plugins>
    <plugin interceptor="com.yczuoxin.interceptor.SQLInterceptor">
        <property name="zuoxin" value="sql" />
    </plugin>
    <plugin interceptor="com.yczuoxin.interceptor.MyPageInterceptor" />
</plugins>
```

### Interceptor

```java
public interface Interceptor {
	// 把增强的方法写在这个这个方法的实现类中
    Object intercept(Invocation invocation) throws Throwable;
	// interceptorChian 会默认调用这个方法将我们要代理的对象进行代理
    default Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    default void setProperties(Properties properties) {
        // NOP
    }

}
```



### Invocation

```java
public class Invocation {

    private final Object target;
    private final Method method;
    private final Object[] args;

    public Invocation(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }
	// 被代理对象
    public Object getTarget() {
        return target;
    }
	// 被代理方法
    public Method getMethod() {
        return method;
    }
	// 被代理方法的参数
    public Object[] getArgs() {
        return args;
    }
	// 调用被代理对象的被代理方法
    public Object proceed() throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }

}
```



### @Intercepts

这个注解放在拦截器插件类上来标明这个插件拦截的是哪个类上的哪个方法

* @Intercepts
  * @Signature[] 
    * type 哪个类
    * method 哪个方法
    * args[] 入参是什么



### InterceptorChain

Mybatis 利用这个对象将插件保存在一个 ArrayList 中

```java
public class InterceptorChain {

    private final List<Interceptor> interceptors = new ArrayList<>();
	
    // 此处可以看出被代理过的对象还可以再次被代理
    public Object pluginAll(Object target) {
        for (Interceptor interceptor : interceptors) {
            target = interceptor.plugin(target);
        }
        return target;
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public List<Interceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

}
```

看名字就知道这个类是采用的责任链模式处理插件的，并且使用的是拦截器而不是过滤器的方式，所以代理模式应该是实现插件必不可少的



### Plugin

此时我们再看下 Plugin 类就知道 MyBatis 是用什么方式实现插件功能了

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
            // 动态代理方法
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
            Set<Method> methods = signatureMap.get(method.getDeclaringClass());
            if (methods != null && methods.contains(method)) {
                return interceptor.intercept(new Invocation(target, method, args));
            }
            return method.invoke(target, args);
        } catch (Exception e) {
            throw ExceptionUtil.unwrapThrowable(e);
        }
    }

    private static Map<Class<?>, Set<Method>> getSignatureMap(Interceptor interceptor) {
        Intercepts interceptsAnnotation = interceptor.getClass().getAnnotation(Intercepts.class);
        // issue #251
        if (interceptsAnnotation == null) {
            throw new PluginException("No @Intercepts annotation was found in interceptor " + interceptor.getClass().getName());
        }
        Signature[] sigs = interceptsAnnotation.value();
        Map<Class<?>, Set<Method>> signatureMap = new HashMap<>();
        for (Signature sig : sigs) {
            Set<Method> methods = signatureMap.computeIfAbsent(sig.type(), k -> new HashSet<>());
            try {
                Method method = sig.type().getMethod(sig.method(), sig.args());
                methods.add(method);
            } catch (NoSuchMethodException e) {
                throw new PluginException("Could not find method on " + sig.type() + " named " + sig.method() + ". Cause: " + e, e);
            }
        }
        return signatureMap;
    }

    private static Class<?>[] getAllInterfaces(Class<?> type, Map<Class<?>, Set<Method>> signatureMap) {
        Set<Class<?>> interfaces = new HashSet<>();
        while (type != null) {
            for (Class<?> c : type.getInterfaces()) {
                if (signatureMap.containsKey(c)) {
                    interfaces.add(c);
                }
            }
            type = type.getSuperclass();
        }
        return interfaces.toArray(new Class<?>[interfaces.size()]);
    }

}
```



## 读取配置

```java
private void pluginElement(XNode parent) throws Exception {
    if (parent != null) {
        for (XNode child : parent.getChildren()) {
            String interceptor = child.getStringAttribute("interceptor");
            Properties properties = child.getChildrenAsProperties();
            Interceptor interceptorInstance = (Interceptor) resolveClass(interceptor).getDeclaredConstructor().newInstance();
            interceptorInstance.setProperties(properties);
            configuration.addInterceptor(interceptorInstance);
        }
    }
}

public void addInterceptor(Interceptor interceptor) {
    interceptorChain.addInterceptor(interceptor);
}
```

仅仅是在 InterceptorChain 的 list 中增加一个元素的操作



## 工作原理

### 何时开始工作的

InterceptorChain 责任链调用应当是工作的开始，那么查看 InterceptorChain.pluginAll() 方法应该是入口，找到调用地点就知道是何时开始工作的了

代码是 Configuration 中提取的

```java
public class Configuration {

	...
    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
        parameterHandler = (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
        return parameterHandler;
    }

    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler,
                                                ResultHandler resultHandler, BoundSql boundSql) {
        ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
        resultSetHandler = (ResultSetHandler) interceptorChain.pluginAll(resultSetHandler);
        return resultSetHandler;
    }

    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        StatementHandler statementHandler = new RoutingStatementHandler(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
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
        executor = (Executor) interceptorChain.pluginAll(executor);
        return executor;
    }
    ...
}
```

可见在新建 Executor、ParameterHandler、ResultSetHandler 以及 StatementHandler 时，都是返回的被代理过的对象

### 代理对象是什么时候新建的呢？

* Executor
  * SqlSessionFactory 在 openSession 时调用，生命周期和 SqlSession 一致
* ParameterHandler
  * 在准备 Statement 的时候创建的，生命周期是一次SQL执行
* ResultSetHandler
  * 在准备 Statement 的时候创建的，是在 ParameterHandler 新建后立马调用的，生命周期是一次SQL执行
* StatementHandler
  * 在 doQuery 的时候调用，生命周期是一次SQL执行



## 自定义

控制台输出 SQL 语句代替 log 日志，并且记录 SQL 执行时间

```java
@Intercepts({ @Signature(type = StatementHandler.class, method = "query", args = { Statement.class, ResultHandler.class}) })
public class SQLInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        String sql = boundSql.getSql();
        System.out.println("获取到 SQL 语句："+sql);
        try {
            return invocation.proceed();
        }finally {
            System.out.println("SQL 执行耗时：" + (System.currentTimeMillis() - startTime) +"ms ");
        }
    }
}
```

