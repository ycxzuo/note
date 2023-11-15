# MapStruct + Springboot 踩坑笔记

在日常工作中经常遇到对象转换的问题，众所周知，利用反射的工具虽然很方便，但是在多次调用的情况下，性能会变得很低，反射代价太大，于是想尝试使用一下 `MapStruct` 工具



## 准备

[MapStruct 官网](https://mapstruct.org/)

[MapStruct 快速入门](https://mapstruct.org/documentation/stable/reference/html/#Preface)



## 第一步 工具引入

我的工程是 SpringBoot 的 Maven 工程，引入依赖

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.2.Final</version>
</dependency>
```



## 第二步 创建互相转换的对象

```java
@Data
public class User {

    private Long id;

    private String name;

    private int age;

}
```

```java
@Data
public class UserDTO {

    private Long id;

    private String nickName;

    private int age;

}
```



## 第三步 创建转换类

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE, unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapping {

    @Mapping(target = "name", source = "nickName")
    User sourceToTarget(UserDTO userDTO);

    @Mapping(target = "nickName", source = "name")
    UserDTO targetToSource(User user);

}
```



## 第四步 编写测试用例

```java
@SpringBootTest
class MybatisPlusApplicationTests {

    @Autowired
    private UserMapping userMapping;

    @Test
    void testMapping() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(123L);
        userDTO.setNickName("zuoxin");
        userDTO.setAge(10);
        System.out.println(userDTO);
        User user = userMapping.sourceToTarget(userDTO);
        System.out.println(user);
    }
}
```



本来这里就应该结束了，可是却报错了

报错信息

> 	Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.yczuoxin.mybatisplus.mapping.UserMapping' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {@org.springframework.beans.factory.annotation.Autowired(required=true)}
> 	at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1790)
> 	at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1346)
> 	at org.springframework.beans.factory.support.DefaultListableBeanFactory.resolveDependency(DefaultListableBeanFactory.java:1300)
> 	at org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor$AutowiredFieldElement.resolveFieldValue(AutowiredAnnotationBeanPostProcessor.java:656)

很明显，这个转换工具类应该被注入 Spring 容器的，但是并没有在 Spring 容器中找到，于是我去官网查看了资料，发现在 pom 文件中需要加入一段内容

```xml
<properties>
    <org.mapstruct.version>1.5.2.Final</org.mapstruct.version>
</properties>
...
<dependencies>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${org.mapstruct.version}</version>
    </dependency>
</dependencies>
...
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${org.mapstruct.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

再次跑测试用例

> java: 找不到符号
>   符号:   方法 setAge(int)
>   位置: 类型为com.yczuoxin.mybatisplus.entity.User的变量 user

分析是加入之后 build 信息后，lombok 失效了，所以我在想，是不是这里导致 lombok 动态生成的类没地方放了，所以我改用增加依赖的方式解决了这个问题

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.2.Final</version>
</dependency>
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
            </configuration>
        </plugin>
    </plugins>
</build>
```

再次跑测试用例

> UserDTO(id=123, nickName=zuoxin, age=10)
> User(id=123, name=zuoxin, age=10)

终于大功告成，在此记录，也方便有遇到类似问题的朋友顺利解决问题

