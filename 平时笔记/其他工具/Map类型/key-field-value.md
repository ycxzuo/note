# key-field-value

## guava 的 Table

### 实现原理

其底层是 Map<Map<>> 这样的数据结构



### API 使用（HashBasedTable 为例）

```java
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * {@link Table} 的示例
 * 第一个字段是：row
 * 第二个字段是：column
 * 第三个字段是：value
 */
public class TableDemo {
    public static void main(String[] args) {
        Table<Long, String, BigDecimal> table = HashBasedTable.create();
        table.put(1L, "a", BigDecimal.ZERO);
        table.put(1L, "b", BigDecimal.ZERO);
        table.put(2L, "b", BigDecimal.ONE);
        table.put(4L, "b", BigDecimal.ONE);
        table.put(3L, "c", BigDecimal.TEN);

        // 利用第一个字段的值获取第二个字段和第三个字段的合集
        Map<String, BigDecimal> row = table.row(1L);
        for (Map.Entry<String, BigDecimal> entry : row.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        System.out.println("----------------------");

        // 利用第二个字段的值获取第一个字段和第三个字段的合集
        Map<Long, BigDecimal> b = table.column("b");
        for (Map.Entry<Long, BigDecimal> entry : b.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        System.out.println("----------------------");

        // 获取类似 map.entry 类似的对象
        Set<Table.Cell<Long, String, BigDecimal>> cells = table.cellSet();
        for (Table.Cell<Long, String, BigDecimal> cell : cells) {
            System.out.println(cell.getRowKey() + " : " + cell.getColumnKey() + " : " + cell.getValue());
        }
        
        System.out.println("----------------------");

        // 利用 column 作为外层 map 的 key 值，row 作为内层 map 的 key 值返回
        Map<String, Map<Long, BigDecimal>> columnMap = table.columnMap();
        for (Map.Entry<String, Map<Long, BigDecimal>> entry : columnMap.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }
}

a : 0
b : 0
----------------------
1 : 0
2 : 1
4 : 1
----------------------
1 : a : 0
1 : b : 0
2 : b : 1
4 : b : 1
3 : c : 10
----------------------
a : {1=0}
b : {1=0, 2=1, 4=1}
c : {3=10}
```

