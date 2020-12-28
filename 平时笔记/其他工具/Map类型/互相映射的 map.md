# 互相映射的 map

## apache 的 BidiMap

### 实现原理

用两个 map 保存 key 和 value 的信息

* normalMap：原 map
* reverseMap：将 key 和 value 互换后的 map

显然，用这个对象有个要求，就是 key 和 value 都不能有重复



### API（DualHashBidiMap 为例）

```java
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.Map;

/**
 * {@link BidiMap}
 */
public class BidiMapDemo {
    public static void main(String[] args) {
        DualHashBidiMap<Integer, String> hashBidiMap = new DualHashBidiMap<>();
        hashBidiMap.put(1, "a");
        hashBidiMap.put(2, "b");
        hashBidiMap.put(3, "c");
		// 将 key 和 value 互换的 map
        BidiMap<String, Integer> inverseBidiMap = hashBidiMap.inverseBidiMap();
        for (Map.Entry<String, Integer> entry : inverseBidiMap.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}
```

