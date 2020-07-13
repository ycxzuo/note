# @ImplementedBy

这个注解在看 Eureka 的源码时发现的注解

```java
package com.netflix.discovery;

import com.google.inject.ImplementedBy;

@ImplementedBy(DiscoveryClient.class)
public interface EurekaClient extends LookupService {
	...
}
```

## 作用

指定接口的默认实现类是哪个类