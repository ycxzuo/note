# 解析 JVM 参数

## JCommander

Java 解析命令行参数的工具

常用 `@Parameter` 设置以下几个属性值：

* names 设置命令行参数，如 `-old`
* required 设置此参数是否必须
* description 设置参数的描述
* order 设置帮助文档的顺序
* help 设置此参数是否为展示帮助文档或者辅助功能

示例代码

```java
public class Cmd {

    // boolean 类型的可以不需要复制，有这个参数就是 true
    @Parameter(names = {"-?", "-help"}, description = "print help message", order = 3, help = true)
    boolean helpFlag = false;

    @Parameter(names = "-version", description = "print version and exit", order = 2)
    boolean versionFlag = false;

    @Parameter(names = {"-cp", "-classpath"}, description = "classpath", order = 1)
    String classpath;

    @Parameter(description = "main class and args")
    List<String> mainClassAndArgs;

    boolean ok;

    String getMainClass() {
        return mainClassAndArgs != null && !mainClassAndArgs.isEmpty()
                ? mainClassAndArgs.get(0)
                : null;
    }

    List<String> getAppArgs() {
        return mainClassAndArgs != null && mainClassAndArgs.size() > 1
                ? mainClassAndArgs.subList(1, mainClassAndArgs.size())
                : null;
    }

    static Cmd parse(String[] argv) {
        Cmd args = new Cmd();
        JCommander cmd = JCommander.newBuilder().addObject(args).build();
        cmd.parse(argv);
        args.ok = true;
        return args;
    }
}
```

