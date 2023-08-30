# Join Point 条件接口 - Pointcut

## 核心组件

* 类过滤器
  * ClassFilter 判断类是不是匹配
* 方法匹配器
  * MethodMatcher 判断方法是不是匹配
    * isRuntime 如果是 false，就是没有入参的方法，如果是 true 就回调用对应的方法



## AspectJ 实现

桥接 AspectJ 原有的功能实现

### 实现类

AspectJExpressionPointcut

### 指令支持

SUPPORTED_PRIMITIVES 字段

### 表达式

PointcutExpression



## 组合实现

ComposablePointcut



### 工具类

* ClassFilter 工具类 = ClassFilters
* MethodMatcher 工具类 - MethodMatchers
* Pointcut 工具类 - Pointcuts



## 静态实现

StaticMethodMatcherPointcut



## 正则表达式实现

JdkRegexpMethodPointcut



## 控制流实现

ControlFlowPointcut