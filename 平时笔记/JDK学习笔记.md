# 学习笔记

## JDK

### Integer

* JVM会自动维护八种基本类型的常量池，int常量池中初始化`-128~127`的范围，所以当`Integer i = 127`时，在自动装箱时是取得常量池中的数值，而`Integer = 128`时，自动装箱过程中new一个128。

### Arrays

* `java.util.Arrays`中有自己封装的私有的ArrayList静态内部类

  ```java
  private static class ArrayList<E> extends AbstractList<E>
          implements RandomAccess, java.io.Serializable{
      private static final long serialVersionUID = -2764017481108945198L;
      private final E[] a;
  
      ArrayList(E[] array) {
          a = Objects.requireNonNull(array);
      }
     ...
  }
  ```

  以上原因导致下面的代码不一致

  ```java
  public class demo1 {
      public static void main(String[] args) {
          Integer[] arr1 = {1,2,3,4,5,6};
          int[] arr2 = {1,2,3,4,5,6};
  
          System.out.println(Arrays.asList(arr1).size());//6
          System.out.println(Arrays.asList(arr2).size());//1
      }
  }
  ```

  为什么会出现这个问题呢？

  在`Arrays.asList(arr1)`-> `Arrays$ArrayList(Integer[]{arr1})`

  在`Arrays.asList(arr2)`-> `Arrays$ArrayList(new int[][]{arr2})`

