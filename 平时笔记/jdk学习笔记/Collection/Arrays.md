# Arrays工具类

## asList()

`java.util.Arrays`中有自己封装的私有的ArrayList静态内部类

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



## sort() 

### sort(Object[] a)

Arrays.class

```java
public static void sort(Object[] a) {
    if (LegacyMergeSort.userRequested)
        // 递归排序
        legacyMergeSort(a);
    else
        // Timsort排序: 结合了合并排序（merge sort）和插入排序（insertion sort）而得出的排序算法
        ComparableTimSort.sort(a, 0, a.length, null, 0, 0);
}

private static void legacyMergeSort(Object[] a,
                                        int fromIndex, int toIndex) {
        Object[] aux = copyOfRange(a, fromIndex, toIndex);
        mergeSort(aux, a, fromIndex, toIndex, -fromIndex);
}
```

ComparableTimSort.class

```java
static void sort(Object[] a, int lo, int hi, Object[] work, int workBase, int workLen) {
        assert a != null && lo >= 0 && lo <= hi && hi <= a.length;
        int nRemaining  = hi - lo;
        if (nRemaining < 2)
            return;  // Arrays of size 0 and 1 are always sorted
        // If array is small, do a "mini-TimSort" with no merges
        if (nRemaining < MIN_MERGE) {
            int initRunLen = countRunAndMakeAscending(a, lo, hi);
            binarySort(a, lo, hi, lo + initRunLen);
            return;
        }
        /**
         * March over the array once, left to right, finding natural runs,
         * extending short natural runs to minRun elements, and merging runs
         * to maintain stack invariant.
         */
        ComparableTimSort ts = new ComparableTimSort(a, work, workBase, workLen);
        int minRun = minRunLength(nRemaining);
        do {
            // Identify next run
            int runLen = countRunAndMakeAscending(a, lo, hi);
            // If run is short, extend to min(minRun, nRemaining)
            if (runLen < minRun) {
                int force = nRemaining <= minRun ? nRemaining : minRun;
                binarySort(a, lo, lo + force, lo + runLen);
                runLen = force;
            }
            // Push run onto pending-run stack, and maybe merge
            ts.pushRun(lo, runLen);
            ts.mergeCollapse();
            // Advance to find next run
            lo += runLen;
            nRemaining -= runLen;
        } while (nRemaining != 0);
        // Merge all remaining runs to complete sort
        assert lo == hi;
        ts.mergeForceCollapse();
        assert ts.stackSize == 1;
    }
```

### sort(int[] a) 基本类型

Arrays.class

```java
public static void sort(int[] a) {
        DualPivotQuicksort.sort(a, 0, a.length - 1, null, 0, 0);
}
```

DuaPivotQuicksort.class

```java
static void sort(int[] a, int left, int right,
                     int[] work, int workBase, int workLen) {
        // Use Quicksort on small arrays
        if (right - left < QUICKSORT_THRESHOLD) {
            sort(a, left, right, true);
            return;
        }

        /*
         * Index run[i] is the start of i-th run
         * (ascending or descending sequence).
         */
        int[] run = new int[MAX_RUN_COUNT + 1];
        int count = 0; run[0] = left;
        // Check if the array is nearly sorted
        for (int k = left; k < right; run[count] = k) {
            // Equal items in the beginning of the sequence
            while (k < right && a[k] == a[k + 1])
                k++;
            if (k == right) break;  // Sequence finishes with equal items
            if (a[k] < a[k + 1]) { // ascending
                while (++k <= right && a[k - 1] <= a[k]);
            } else if (a[k] > a[k + 1]) { // descending
                while (++k <= right && a[k - 1] >= a[k]);
                // Transform into an ascending sequence
                for (int lo = run[count] - 1, hi = k; ++lo < --hi; ) {
                    int t = a[lo]; a[lo] = a[hi]; a[hi] = t;
                }
            }
            // Merge a transformed descending sequence followed by an
            // ascending sequence
            if (run[count] > left && a[run[count]] >= a[run[count] - 1]) {
                count--;
            }
            /*
             * The array is not highly structured,
             * use Quicksort instead of merge sort.
             */
            if (++count == MAX_RUN_COUNT) {
                sort(a, left, right, true);
                return;
            }
        }
    if (count == 0) {
            // A single equal run
            return;
        } else if (count == 1 && run[count] > right) {
            // Either a single ascending or a transformed descending run.
            // Always check that a final run is a proper terminator, otherwise
            // we have an unterminated trailing run, to handle downstream.
            return;
        }
        right++;
        if (run[count] < right) {
            // Corner case: the final run is not a terminator. This may happen
            // if a final run is an equals run, or there is a single-element run
            // at the end. Fix up by adding a proper terminator at the end.
            // Note that we terminate with (right + 1), incremented earlier.
            run[++count] = right;
        }
        // Determine alternation base for merge
        byte odd = 0;
        for (int n = 1; (n <<= 1) < count; odd ^= 1);

        // Use or create temporary array b for merging
        int[] b;                 // temp array; alternates with a
        int ao, bo;              // array offsets from 'left'
        int blen = right - left; // space needed for b
        if (work == null || workLen < blen || workBase + blen > work.length) {
            work = new int[blen];
            workBase = 0;
        }
        if (odd == 0) {
            System.arraycopy(a, left, work, workBase, blen);
            b = a;
            bo = 0;
            a = work;
            ao = workBase - left;
        } else {
            b = work;
            ao = 0;
            bo = workBase - left;
        }
        // Merging
        for (int last; count > 1; count = last) {
            for (int k = (last = 0) + 2; k <= count; k += 2) {
                int hi = run[k], mi = run[k - 1];
                for (int i = run[k - 2], p = i, q = mi; i < hi; ++i) {
                    if (q >= hi || p < mi && a[p + ao] <= a[q + ao]) {
                        b[i + bo] = a[p++ + ao];
                    } else {
                        b[i + bo] = a[q++ + ao];
                    }
                }
                run[++last] = hi;
            }
            if ((count & 1) != 0) {
                for (int i = right, lo = run[count - 1]; --i >= lo;
                    b[i + bo] = a[i + ao]
                );
                run[++last] = right;
            }
            int[] t = a; a = b; b = t;
            int o = ao; ao = bo; bo = o;
        }
    }
```

* 数组长度是小于 286（QUICKSORT_THRESHOLD）

  * 数组长度小于 47 （INSERTION_SORT_THRESHOLD）

    * 使用插入排序，并且在插入排序使用`leftmost`控制使用传统的插入排序还是改进的插入排序，改进后的插入排序
      * 先跳过前面排好序的元素
      * 将后面的两个元素两两往前面插入排序

    ```java
    if (leftmost) {
                    for (int i = left, j = i; i < right; j = ++i) {
                        int ai = a[i + 1];
                        while (ai < a[j]) {
                            a[j + 1] = a[j];
                            if (j-- == left) {
                                break;
                            }
                        }
                        a[j + 1] = ai;
                    }
                } else {
                    do {
                        if (left >= right) {
                            return;
                        }
                    } while (a[++left] >= a[left - 1]);
                    for (int k = left; ++left <= right; k = ++left) {
                        int a1 = a[k], a2 = a[left];
    
                        if (a1 < a2) {
                            a2 = a1; a1 = a[left];
                        }
                        while (a1 < a[--k]) {
                            a[k + 2] = a[k];
                        }
                        a[++k + 1] = a1;
    
                        while (a2 < a[--k]) {
                            a[k + 1] = a[k];
                        }
                        a[k + 1] = a2;
                    }
                    int last = a[right];
    
                    while (last < a[--right]) {
                        a[right + 1] = a[right];
                    }
                    a[right + 1] = last;
                }
                return;
            }
    ```

  * 数组长度大于等于 47 （INSERTION_SORT_THRESHOLD）

* 数组长度是大于等于 286（QUICKSORT_THRESHOLD）