# Spring Cloud Config Server

[Spring官方文档](http://cloud.spring.io/spring-cloud-static/Finchley.SR1/single/spring-cloud.html#_spring_cloud_config_server)

## 流程分析

#### 入口`@EnableConfigServer `

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ConfigServerConfiguration.class)
public @interface EnableConfigServer {

}
```

#### 实际配置类`ConfigServerConfiguration`

```java
@Configuration
public class ConfigServerConfiguration {
	class Marker {}

	@Bean
	public Marker enableConfigServerMarker() {
		return new Marker();
	}
}
```

#### 找到使用Marker的地方`ConfigServerAutoConfiguration`

```java
@Configuration
@ConditionalOnBean(ConfigServerConfiguration.Marker.class)
@EnableConfigurationProperties(ConfigServerProperties.class)
@Import({ EnvironmentRepositoryConfiguration.class, CompositeConfiguration.class, ResourceRepositoryConfiguration.class,
		ConfigServerEncryptionConfiguration.class, ConfigServerMvcConfiguration.class })
public class ConfigServerAutoConfiguration {

}
```

#### 导入各种配置`EnvironmentRepositoryConfiguration`

```java
@Configuration
@EnableConfigurationProperties({ SvnKitEnvironmentProperties.class,
		JdbcEnvironmentProperties.class, NativeEnvironmentProperties.class, VaultEnvironmentProperties.class })
@Import({ CompositeRepositoryConfiguration.class, JdbcRepositoryConfiguration.class, VaultRepositoryConfiguration.class,
		SvnRepositoryConfiguration.class, NativeRepositoryConfiguration.class, GitRepositoryConfiguration.class,
		DefaultRepositoryConfiguration.class })
public class EnvironmentRepositoryConfiguration {
    ...
}
```

- 看一个熟悉的jdbc的环境配置

  ```java
  @Configuration
  @Profile("jdbc")
  @ConditionalOnClass(JdbcTemplate.class)
  class JdbcRepositoryConfiguration {
  
  	@Bean
  	@ConditionalOnBean(JdbcTemplate.class)
  	public JdbcEnvironmentRepository jdbcEnvironmentRepository(JdbcEnvironmentRepositoryFactory factory, JdbcEnvironmentProperties environmentProperties) {
  		return factory.build(environmentProperties);
  	}
  }
  ```

  - 查看jdbcTemplate注入的地方`JdbcTemplateAutoConfiguration`

    ```java
    @Configuration
    @ConditionalOnClass({ DataSource.class, JdbcTemplate.class })
    @ConditionalOnSingleCandidate(DataSource.class)
    @AutoConfigureAfter(DataSourceAutoConfiguration.class)
    @EnableConfigurationProperties(JdbcProperties.class)
    public class JdbcTemplateAutoConfiguration {
        @Configuration
    	static class JdbcTemplateConfiguration {
            ...
            @Bean
            @Primary
            @ConditionalOnMissingBean(JdbcOperations.class)
            public JdbcTemplate jdbcTemplate() {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);
                JdbcProperties.Template template = this.properties.getTemplate();
                jdbcTemplate.setFetchSize(template.getFetchSize());
                jdbcTemplate.setMaxRows(template.getMaxRows());
                if (template.getQueryTimeout() != null) {
                    jdbcTemplate
                        .setQueryTimeout((int) template.getQueryTimeout().getSeconds());
                }
                return jdbcTemplate;
            }
        }
        ...
    }
    ```

  - Sql注入的地方`JdbcEnvironmentProperties`

    `spring.cloud.config.server.jdbc`,不配置就默认用DEFAULT_SQL

    **标名:PROPERTIES**

    | KEY  |  VALUE   | APPLICATION | PROFILE | LABEL  |
    | :--: | :------: | :---------: | :-----: | :----: |
    | name | yczuoxin |   config    | default | master |

    ```java
    @ConfigurationProperties("spring.cloud.config.server.jdbc")
    public class JdbcEnvironmentProperties implements EnvironmentRepositoryProperties {
    	private static final String DEFAULT_SQL = "SELECT KEY, VALUE from PROPERTIES where APPLICATION=? and PROFILE=? and LABEL=?";
        ...
        public String getSql() {
    		return sql;
    	}
    }
    ```

- 看下默认的配置仓储`DefaultRepositoryConfiguration`

  ```java
  @Configuration
  @ConditionalOnMissingBean(value = EnvironmentRepository.class, search = SearchStrategy.CURRENT)
  class DefaultRepositoryConfiguration {
      ...
      @Bean
  	public MultipleJGitEnvironmentRepository defaultEnvironmentRepository(
  	        MultipleJGitEnvironmentRepositoryFactory gitEnvironmentRepositoryFactory,
  			MultipleJGitEnvironmentProperties environmentProperties) throws Exception {
  		return gitEnvironmentRepositoryFactory.build(environmentProperties);
  	}
  }
  ```

  这就是为何git为默认的配置仓储的原因.

## 自定义配置

由上可以看出读取配置的核心接口是`EnvironmentRepository`

自己实现一个`EnvironmentRepository`

```java
@SpringBootApplication
@EnableConfigServer
public class SpringCloudConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudConfigServerApplication.class, args);
    }

    @Bean
    public EnvironmentRepository myEnvironmentRepository(){
        return (String application, String profile, String label) -> {
            Environment environment = new Environment("default", profile);
            environment.setLabel(label);
            environment.setState("ON");
            environment.setVersion("1.0.0");
            List<PropertySource> propertySources = environment.getPropertySources();
            Map<String, String> resource = new HashMap<>(1);
            resource.put("name","yczuoxin");
            PropertySource propertySource =new PropertySource("source",resource);
            propertySources.add(propertySource);
            return environment;
        };
    }
}
```

[访问网站http://localhost:8080/config/dev/master](http://localhost:8080/config/dev/master)

```json
{
  "name": "config",
  "profiles": [
    "dev"
  ],
  "label": "master",
  "version": "1.0.0",
  "state": "ON",
  "propertySources": [
    {
      "name": "source",
      "source": {
        "name": "yczuoxin"
      }
    }
  ]
}
```

## HTTP请求

由上可以看见json值与请求路径有关`EnvironmentController`

`@RequestMapping("/{name}/{profile}/{label}")`

## 使用场景

可以给与配置中心动态更改配置,也可以根据不同的条件拉取不同的配置,使得管理配置更简单.例如在profile栏填写区域,在label栏填写分支,便可以根据不同区域拉取不同的配置,而管理配置只需要关注配置服务器上对应的配置文件即可.

