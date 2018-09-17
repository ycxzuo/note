# Spring Boot之REST

## 基础知识概扩

### RPC(Remote Precedure Call)

* 语言相关
  * java -> RMI(Remote Method invocation)
  * .Net -> COM+
* 语言无关
  * SOA
  * WebService
    * SOAP (传输介质协议)
    * HTTP, SMTP (通信协议)
  * MSA (微服务)
  * REST(Representational state transfer)
    * Json, xml, HTML等 (传输介质)
    * HTTP (通讯协议)
    * HTTP 1.1
      * 短连接
      * Keep-Alive
      * 连接池
      * Long Polling
    * HTTP/2
      * 长连接
    * 技术
      * Spring Framework: `RestTemplate` (Spring 3.0版本引入)
      * Spring MVC: `@RestController` = `@Controller` + `@ResponseBody`
      * Spring Cloud: `RrestTemplate扩展` + `@LoadBalanced`