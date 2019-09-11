# springboot-tomcat容器

注册 tomcat 配置方式:`org.springframework.boot.web.server.WebServerFactoryCustomizer#customize`

实现:`org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryCustomizer`

## 实战

### springboot 1.x

```java
@Bean
public static EmbeddedServletContainerCustomizer embeddedServletContainerCustomizer() {
    return new EmbeddedServletContainerCustomizer() {

        @Override
        public void customize(ConfigurableEmbeddedServletContainer container) {


            if (container instanceof TomcatEmbeddedServletContainerFactory) {
                TomcatEmbeddedServletContainerFactory factory = TomcatEmbeddedServletContainerFactory.class.cast(container);

                factory.addContextCustomizers(new TomcatContextCustomizer() {
                    @Override
                    public void customize(Context context) {

                        context.setPath("/spring-boot");
                    }
                });

                factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
                    @Override
                    public void customize(Connector connector) {
                        connector.setPort(8888);
                        connector.setProtocol(Http11Nio2Protocol.class.getName());
                    }
                });
            }

        }
    };
}
```



### springboot 2.0

注册 Tomcat 的地方`org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory#getWebServer`

```java
public WebServer getWebServer(ServletContextInitializer... initializers) {
    Tomcat tomcat = new Tomcat();
    File baseDir = (this.baseDirectory != null) ? this.baseDirectory : createTempDir("tomcat");
    tomcat.setBaseDir(baseDir.getAbsolutePath());
    Connector connector = new Connector(this.protocol);
    tomcat.getService().addConnector(connector);
    customizeConnector(connector);
    tomcat.setConnector(connector);
    tomcat.getHost().setAutoDeploy(false);
    configureEngine(tomcat.getEngine());
    for (Connector additionalConnector : this.additionalTomcatConnectors) {
        tomcat.getService().addConnector(additionalConnector);
    }
    prepareContext(tomcat.getHost(), initializers);
    return getTomcatWebServer(tomcat);
}
```

`TomcatServletWebServerFactory` 中维护了一个 `Connector` 列表和 `Servlet` 列表

```java
@Bean
public static WebServerFactoryCustomizer<TomcatServletWebServerFactory> webServerFactory() {
    return (SpringbootTest5Application::customize);
}

public static void customize(TomcatServletWebServerFactory factory){
    factory.setPort(8888);
    factory.setProtocol(Http11Nio2Protocol.class.getName());
    factory.setContextPath("");

    // 修改定制化 tomcat 中的 connector
    factory.addConnectorCustomizers(SpringbootTest5Application::customize);

    // 新增定制化 tomcat 中的 connector
    Connector connector = new Connector();
    connector.setPort(8887);
    factory.addAdditionalTomcatConnectors(connector);
}

public static void customize(Connector connector){
    connector.setPort(8889);
}
```

