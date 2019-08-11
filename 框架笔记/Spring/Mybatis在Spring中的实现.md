# Mybatis在Spring中的实现

`MapperScannerConfigurer` 是 Spring 整合 Mybatis 的核心类，其作用是扫描 DAO 类，将其创建为 Mybatis 的Mapper 对象（MapperProxy）

### MapperProxy创建序列图

![Mapper创建时序图](http://tva1.sinaimg.cn/large/0060lm7Tly1g56g76qsw3j31bf0j4js8.jpg)

