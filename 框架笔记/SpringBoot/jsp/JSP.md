# JSP on Spring Boot

## 激活

1. 增加依赖

   ```xml
   <!-- JSP 渲染引擎 -->
   <dependency>
       <groupId>org.apache.tomcat.embed</groupId>
       <artifactId>tomcat-embed-jasper</artifactId>
       <scope>provided</scope>
   </dependency>
   <!-- JSTL -->
   <dependency>
       <groupId>jstl</groupId>
       <artifactId>jstl</artifactId>
       <version>1.2</version>
   </dependency>
   ```

   

2. 激活传统 Servlet Web 部署

   * Spring Boot 1.4.0 开始
     * `org.springframework.boot.web.servlet.support.SpringBootServletInitializer`

3. 组装 `org.springframework.boot.builder.SpringApplicationBuilder`

4. 配置 JSP 视图

   * `org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties`
     * spring.mvc.view.prefix
     * spring.mvc.view.suffix

