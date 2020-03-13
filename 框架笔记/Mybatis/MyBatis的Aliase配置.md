# MyBatis 的 Aliase 配置

## 类型别名

配置方法有两种：

1. 使用别名和类的全限定名一一对应，当这样配置时，`Blog` 可以用在任何使用 `domain.blog.Blog` 的地方

```xml
<typeAliases>
  <typeAlias alias="Author" type="domain.blog.Author"/>
  <typeAlias alias="Blog" type="domain.blog.Blog"/>
  <typeAlias alias="Comment" type="domain.blog.Comment"/>
  <typeAlias alias="Post" type="domain.blog.Post"/>
  <typeAlias alias="Section" type="domain.blog.Section"/>
  <typeAlias alias="Tag" type="domain.blog.Tag"/>
</typeAliases>
```

2. 设置包名，MyBatis 会在包名下面搜索需要的 Java Bean，在没有注解的情况下，会使用 Bean 的首字母小写的非限定类名来作为它的别名。 比如 `domain.blog.Author` 的别名为 `author`；若有注解，则别名为其注解值

```xml
<typeAliases>
  <package name="domain.blog"/>
</typeAliases>
```

Java Bean 中

```java
@Alias("author")
public class Author {
    ...
}
```



## 读取配置

类型别名是为 Java 类型设置一个短的名字。 它只和 XML 配置有关，存在的意义仅在于用来减少类完全限定名的冗余，这部分的源码如下

```java
private void typeAliasesElement(XNode parent) {
    if (parent != null) {
        for (XNode child : parent.getChildren()) {
            if ("package".equals(child.getName())) {
                String typeAliasPackage = child.getStringAttribute("name");
                configuration.getTypeAliasRegistry().registerAliases(typeAliasPackage);
            } else {
                String alias = child.getStringAttribute("alias");
                String type = child.getStringAttribute("type");
                try {
                    Class<?> clazz = Resources.classForName(type);
                    if (alias == null) {
                        typeAliasRegistry.registerAlias(clazz);
                    } else {
                        typeAliasRegistry.registerAlias(alias, clazz);
                    }
                } catch (ClassNotFoundException e) {
                    throw new BuilderException("Error registering typeAlias for '" + alias + "'. Cause: " + e, e);
                }
            }
        }
    }
}
```



MyBatis 中 Java 类型内建的相应的类型别名。它们都是不区分大小写的，注意对基本类型名称重复采取的特殊命名风格是在类型前加上下划线，如`_byte`，源码如下，最终的别名都会放在 TypeAliasRegistry 中属性名为 typeAliases 的 HashMap 中，其中 key 为别名，value 为对应的 class 全限定名

```java
public class TypeAliasRegistry {

  private final Map<String, Class<?>> typeAliases = new HashMap<>();

  public TypeAliasRegistry() {
    registerAlias("string", String.class);

    registerAlias("byte", Byte.class);
    registerAlias("long", Long.class);
    registerAlias("short", Short.class);
    registerAlias("int", Integer.class);
    registerAlias("integer", Integer.class);
    registerAlias("double", Double.class);
    registerAlias("float", Float.class);
    registerAlias("boolean", Boolean.class);

    registerAlias("byte[]", Byte[].class);
    registerAlias("long[]", Long[].class);
    registerAlias("short[]", Short[].class);
    registerAlias("int[]", Integer[].class);
    registerAlias("integer[]", Integer[].class);
    registerAlias("double[]", Double[].class);
    registerAlias("float[]", Float[].class);
    registerAlias("boolean[]", Boolean[].class);

    registerAlias("_byte", byte.class);
    registerAlias("_long", long.class);
    registerAlias("_short", short.class);
    registerAlias("_int", int.class);
    registerAlias("_integer", int.class);
    registerAlias("_double", double.class);
    registerAlias("_float", float.class);
    registerAlias("_boolean", boolean.class);

    registerAlias("_byte[]", byte[].class);
    registerAlias("_long[]", long[].class);
    registerAlias("_short[]", short[].class);
    registerAlias("_int[]", int[].class);
    registerAlias("_integer[]", int[].class);
    registerAlias("_double[]", double[].class);
    registerAlias("_float[]", float[].class);
    registerAlias("_boolean[]", boolean[].class);

    registerAlias("date", Date.class);
    registerAlias("decimal", BigDecimal.class);
    registerAlias("bigdecimal", BigDecimal.class);
    registerAlias("biginteger", BigInteger.class);
    registerAlias("object", Object.class);

    registerAlias("date[]", Date[].class);
    registerAlias("decimal[]", BigDecimal[].class);
    registerAlias("bigdecimal[]", BigDecimal[].class);
    registerAlias("biginteger[]", BigInteger[].class);
    registerAlias("object[]", Object[].class);

    registerAlias("map", Map.class);
    registerAlias("hashmap", HashMap.class);
    registerAlias("list", List.class);
    registerAlias("arraylist", ArrayList.class);
    registerAlias("collection", Collection.class);
    registerAlias("iterator", Iterator.class);

    registerAlias("ResultSet", ResultSet.class);
  }
  
  // 注册的时候会跳过三种类型的文件，内部类，接口和成员类
  public void registerAliases(String packageName, Class<?> superType) {
    ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
    // 此处 superType 是 Object
    // 底层使用 Class 的 isAssignableFrom() 过滤 Object 是传入的 class 相同或者是其父类的 class
    resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
    Set<Class<? extends Class<?>>> typeSet = resolverUtil.getClasses();
    for (Class<?> type : typeSet) {
      // Ignore inner classes and interfaces (including package-info.java)
      // Skip also inner classes. See issue #6
      if (!type.isAnonymousClass() && !type.isInterface() && !type.isMemberClass()) {
        registerAlias(type);
      }
    }
  }
}
```

在使用包名读取文件的时候有一段代码，其中 VFS 抽象类是 MyBatis 封装的，有两个实现类

* org.apache.ibatis.io.JBoss6VFS
* org.apache.ibatis.io.DefaultVFS

先会判断 JBoss6VFS 环境有没有，如果没有就会使用 DefaultVFS

```java
public ResolverUtil<T> find(Test test, String packageName) {
    String path = getPackagePath(packageName);

    try {
        // 使用 VFS 读取文件，可以读取导路径下的文件（包括子文件夹内的）及文件夹
        List<String> children = VFS.getInstance().list(path);
        for (String child : children) {
            if (child.endsWith(".class")) {
                addIfMatching(test, child);
            }
        }
    } catch (IOException ioe) {
        log.error("Could not read package: " + packageName, ioe);
    }
    return this;
}
```

> *VFS：虚拟文件系统，全名 Virtual File Systems，是由 Sun microsystems 公司在定义网络文件系统(NFS)时创造的。它是一种用于网络环境的分布式文件系统，是允许和操作系统使用不同的文件系统实现的接口。虚拟文件系统（VFS）是物理文件系统与服务之间的一个接口层，它对 Linux 的每个文件系统的所有细节进行抽象，使得不同的文件系统在 Linux 核心以及系统中运行的其他进程看来，都是相同的。严格说来，VFS 并不是一种实际的文件系统。它只存在于内存中，不存在于任何外存空间。VFS 在系统启动时建立，在系统关闭时消亡。* 
>
> 摘自百度百科

在实例化 VFS 的代码如下

```java
public abstract class VFS {
    // 静态内部类实现单例
    private static class VFSHolder {
        static final VFS INSTANCE = createVFS();

        @SuppressWarnings("unchecked")
        static VFS createVFS() {
          // Try the user implementations first, then the built-ins
          List<Class<? extends VFS>> impls = new ArrayList<>();
          // 用户自定义的 vfsImpl 实现加入 list
          impls.addAll(USER_IMPLEMENTATIONS);
          // Mybatis 内置的两种 VFS 实现 JBoss6VFS，DefaultVFS
          impls.addAll(Arrays.asList((Class<? extends VFS>[]) IMPLEMENTATIONS));

          VFS vfs = null;
          // 找到一个可以使用的 VFS 类
          for (int i = 0; vfs == null || !vfs.isValid(); i++) {
            Class<? extends VFS> impl = impls.get(i);
            try {
              vfs = impl.getDeclaredConstructor().newInstance();
              if (!vfs.isValid()) {
                if (log.isDebugEnabled()) {
                  log.debug("VFS implementation " + impl.getName() +
                      " is not valid in this environment.");
                }
              }
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
              log.error("Failed to instantiate " + impl, e);
              return null;
            }
          }

          if (log.isDebugEnabled()) {
            log.debug("Using VFS adapter " + vfs.getClass().getName());
          }

          return vfs;
        }
      }

      /**
       * Get the singleton {@link VFS} instance. If no {@link VFS} implementation can be found for the
       * current environment, then this method returns null.
       */
    public static VFS getInstance() {
        return VFSHolder.INSTANCE;
    }
}
```

JBoss6VFS 的部分代码

```java
protected static synchronized void initialize() {
    if (valid == null) {
      // 默认为 true
      valid = Boolean.TRUE;
      // 查看是否存在 JBoss 的 VFS 类
      VFS.VFS = checkNotNull(getClass("org.jboss.vfs.VFS"));
      VirtualFile.VirtualFile = checkNotNull(getClass("org.jboss.vfs.VirtualFile"));

      // Look up and verify required methods
      VFS.getChild = checkNotNull(getMethod(VFS.VFS, "getChild", URL.class));
      VirtualFile.getChildrenRecursively = checkNotNull(getMethod(VirtualFile.VirtualFile,
          "getChildrenRecursively"));
      VirtualFile.getPathNameRelativeTo = checkNotNull(getMethod(VirtualFile.VirtualFile,
          "getPathNameRelativeTo", VirtualFile.VirtualFile));

      // Verify that the API has not changed
      checkReturnType(VFS.getChild, VirtualFile.VirtualFile);
      checkReturnType(VirtualFile.getChildrenRecursively, List.class);
      checkReturnType(VirtualFile.getPathNameRelativeTo, String.class);
    }
}

 protected static <T> T checkNotNull(T object) {
    if (object == null) {
      // 如果不存在 JBoss 的 VFS 类，就将 valid 设置为 false
      setInvalid();
    }
    return object;
 }

 protected static void setInvalid() {
    if (JBoss6VFS.valid == Boolean.TRUE) {
      log.debug("JBoss 6 VFS API is not available in this environment.");
      JBoss6VFS.valid = Boolean.FALSE;
    }
 }
```

DefaultVFS 的 isValid() 方法更简单粗暴，是一个兜底方案

```java
public boolean isValid() {
    return true;
}
```

所以使用顺序是

* 用户自定义的 VFS 实现
* JBoss 的 VFS 实现
* MyBatis 提供的兜底方案 DefaultVFS

