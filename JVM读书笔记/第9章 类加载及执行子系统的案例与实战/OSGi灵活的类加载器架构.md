# OSGi灵活的类加载器架构

OSGi`Open Service Gateway Initiative`是 OSGi 联盟`OSGi Alliance`制定的一个基于 Java 语言的动态模块化规范，这个规范最初由 Sun、IBM 等公司联合发起，目的是使服务提供商通过住宅网关为各种家用智能设备提供各种服务。Eclipse IDE 是 OSGi 在 Java 程序中最著名的应用案例

OSGi 中的每个模块（`Bundle`）与普通的 Java 类库区别并不大，两者一般都以 Jar 格式进行封装，并且内部存储都是 Java Package 和 Class。但是一个`Bundle`可以生命他所依赖的 Java Package（通过`Import-Package`描述），也可以声明它允许到处发布的 Java Package（通过`Export-Package`描述）。在 OSGi 里面，`Bundle`之间的依赖关系从传统的上层模块依赖转变为平级模块之间的依赖，而且类库的可见性能得到非常精确的控制，一个模块里只有被`Export`过的`Package`才可能有外界访问，其他的 Package 和 Class 将会隐藏起来。除了更精确的模块划分和可见性控制外，引入 OSGi 的另外一个重要理由是，**基于 OSGi 的程序很可能（只是可能）可以实现模块级别的热插拔功能**，当程序升级更新或调试出错时，可以只停用、重新安装然后启用程序的其中一部分，这对企业级程序开发来说是一个非常有诱惑力的特性。

**OSGi 之所以能有上述特点，要归功于它灵活的类加载器架构**。OSGi 的`Bundle`类加载器之间只有规则，没有固定的委派关系。例如，某个`Bundle`声明了一个它依赖的 Package，如果有其它`Bundle`声明发布了这个 Package，那么所有对这个 Package 的类加载动作都会委派给发布它的`Bundle`类加载器去完成。不涉及某个具体的 Package 时，各个`Bundle`加载器都是平级关系，只有具体使用某个 Package 和 Class 的时候，才会根据 Package 导入导出定义来构造`Bundle`间的委派和依赖。

