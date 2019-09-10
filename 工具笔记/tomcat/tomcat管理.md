# Tomcat 架构管理

Tomcat 如何统一管理组建的创建、初始化、启动、停止和销毁？如何做到代码清晰，如何方便地添加或者删除组件？如何做到组件启动和停止不遗漏、不重复？

## 组件之间的关系

仔细分析过这些组件，可以发现组件分为两层

* 第一层关系是组件有大有小，大组件管理小组件，例如 Server 管理 Service，Service 又管理 Connector 和 Container
* 第二层关系是组件有外有内，外层组件控制内层组件，例如 Connector 是外层组件，负责对外交流，外曾组建调用内层组件完成业务功能。也就是说，**请求的处理过程是由外层组件来驱动的**

这两层关系决定了系统在创建组件时应该遵循一定的顺序

* 第一个原则是先创建子组件，再创建父组件，子组件需要被注入到父组件中
* 第二个原则是先创建内层组件，再创建外层组件，内层组件需要被注入到外层组件中

但是如果按照这个思路设计，不仅会造成代码逻辑混乱和组件遗漏，而且也不利于后期的功能扩展。为了解决这个问题，需要找到一种通用的、统一的方法来管理组建的生命周期，有着一键启动的效果

### 一键启动：Lifecycle 接口

这里的不变点是每个组件都要经历创建、初始化、启动这几个过程，这些状态以及状态的转化是不变的；变化点是每个具体组件的初始化方法，也就是启动方法是不一样的

因此，我们把不变点抽象出来成为一个接口。这个接口跟生命周期有关，叫做 Lifecycle。Lifecycle 接口里应该定义几个方法：init、start、stop 和 destory，每个具体组件去实现这些方法

在父组件的 init 方法里需要创建子组件并调用子组件的 init 方法。同样在父组件的 start 方法也需要调用子组件的 start 方法，因此调用者可以无差别的调用各组件的 init 方法和 start 方法，这就是**组合模式**的使用，只需要调用最顶层组件，也就是 Server 组件的 init 和 start 方法，整个 Tomcat 就启动起来了

```java
public interface Lifecycle {
    ...
    public void init() throws LifecycleException;
    public void start() throws LifecycleException;
    public void stop() throws LifecycleException;
    public void destory() throws LifecycleException;
}
```

### 可扩展性：Lifecycle 事件

因为各个组件的 init 和 start 方法的具体实现是复杂多变的，比如在 Host 容器启动方法里需要扫描 webapps 目录下的 Web 应用，创建相应的 Context 容器，如果将来需要增加新的逻辑，不能重新修改 start 方法（违反开闭原则，但是可以构建新的类）。

组件的 init 和 start 方法调用是由它的父组件的状态变化触发的，上层组件的初始化会触发子组件的初始化，上层组件的启动会触发子组件的启动，因此我们把组件的生命周期定义成一个个状态，把状态的转变看作是一个事件。而事件是有监听器的，在监听器里可以实现一些逻辑，并且监听器也可以方便的添加和删除，这就是**观察者模式**

具体来说就是 Lifecycle 接口中加入两个方法：添加监听器和删除监听器。除此之外，我们还需要定一个 Enum 来表示组件有哪些状态，以及处在什么状态会触发什么样的事件。因此 Lifecycle 接口和 LifecycleState 定义如下

```java
public interface Lifecycle {
    ...
    public void addLifecycleListener(LifecycleListener listener);
    public LifecycleListener[] findLifecycleListeners();
    public void removeLifecycleListener(LifecycleListener listener);
}
```

```java
public enum LifecycleState {
    NEW(false, null),
    INITIALIZING(false, Lifecycle.BEFORE_INIT_EVENT),
    INITIALIZED(false, Lifecycle.AFTER_INIT_EVENT),
    STARTING_PREP(false, Lifecycle.BEFORE_START_EVENT),
    STARTING(true, Lifecycle.START_EVENT),
    STARTED(true, Lifecycle.AFTER_START_EVENT),
    STOPPING_PREP(true, Lifecycle.BEFORE_STOP_EVENT),
    STOPPING(false, Lifecycle.STOP_EVENT),
    STOPPED(false, Lifecycle.AFTER_STOP_EVENT),
    DESTROYING(false, Lifecycle.BEFORE_DESTROY_EVENT),
    DESTROYED(false, Lifecycle.AFTER_DESTROY_EVENT),
    FAILED(false, null);

    private final boolean available;
    private final String lifecycleEvent;

    private LifecycleState(boolean available, String lifecycleEvent) {
        this.available = available;
        this.lifecycleEvent = lifecycleEvent;
    }
    public boolean isAvailable() {
        return available;
    }

    public String getLifecycleEvent() {
        return lifecycleEvent;
    }
}
```

由 LifecycleState 可以看出来，每种状态对应一个事件，如果有监听器在监听这个事件，它的方法就会被调用



### 重用性：LifecycleBase 抽象基类

其实定义一个基类来实现共同的逻辑，然后让各个子类去继承它，就达到了重用的目的，而基类往往要定义一些抽象方法，所谓抽象方法就是说基类不会实现这些方法，而是调用这些房的来实现骨架逻。抽象方法是留给子类去实现的

Tomcat 定义了一个基类 LifecycleBase 来实现 Lifecycle 接口，把一些公共的逻辑放到基类中，例如生命状态的转变与维护，生命事件的触发以及监听器的添加和删除等，而子类就负责实现自己的初始化、启动和停止等方法，为了避免与原方法同名，把具体是现房的改了名字，在后面加上 Internal

```java
public abstract class LifecycleBase implements Lifecycle {
	protected abstract void initInternal() throws LifecycleException;
    protected abstract void startInternal() throws LifecycleException;
    protected abstract void stopInternal() throws LifecycleException;
    protected abstract void destroyInternal() throws LifecycleException;
}
```

LifecycleBase 实现类 Lifecycle 所有的方法，并且定义了 抽象方法给子类实现，这是**模板模式**

那么监听器是什么时候注册的呢？

分两种情况

* Tomcat 自定义了一些监听器，这些监听器是由父组件再创建子组件的过程中注册到子组件的，例如 MemoryLeakTrackingListener 监听器用来检测 Context 容器中的内存泄漏，这个监听器是 Host 容器再创建 Context 容器时注册到 Context 中的
* 在server.xml 中定义自己的监听器，Tomcat 启动时会解析 server.xml，创建监听器并注册到容器组件中



## 生命周期管理类图

![架构图](http://tva1.sinaimg.cn/large/007X8olVly1g6uidhmut4j30ux0i1gmf.jpg)

图中 StandardServer、StandardService 等是 Server 和 Service 组件的具体实现类，都继承了 LifecycleBase

StandardEngine、StandardHost、StandardContext 和 StandardWrapper 是相应容器组件的具体实现类，因为他们都是容器，所以继承了 ContainerBase 抽象基类，而 ContainerBase 基类实现了 Container 接口，也继承了 LifecycleBase 类，它们的生命周期管理接口和功能接口是分开的，这也符合设计的**接口分类的原则**

