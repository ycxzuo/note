# Spring Data笔记

Spring Data 抽象了持久层，将关系型数据库和非关系型数据库统一当做仓储，将数据的持久化操作抽象出来，简化了代码并统一了对持久层的操作方式

## 查询接口

### 基础查询接口 `CrudRepository`

```java
public interface CrudRepository<T, ID> extends Repository<T, ID> {
    
	// 保存一个对象实体
	<S extends T> S save(S entity);

    // 保存所有对象实体
	<S extends T> Iterable<S> saveAll(Iterable<S> entities);

    // 通过 id 搜索对象实体
	Optional<T> findById(ID id);

    // 判断对应 id 的对象实体是否存在
	boolean existsById(ID id);

    // 查询所有的对象实体
	Iterable<T> findAll();

    // 根据 id 列表查询对应的对象实体列表
	Iterable<T> findAllById(Iterable<ID> ids);

    // 返回可用实体的数量
	long count();

    // 根据对象 id 删除对象实体
	void deleteById(ID id);

    // 删除与传入的对象相同的对象实体
	void delete(T entity);

    // 删除与传入的对象列表相同的对象实体列表
	void deleteAll(Iterable<? extends T> entities);

    // 删除所有对象实体
	void deleteAll();
}
```



### 分页和排序查询接口 `PagingAndSortingRepository`

```java
public interface PagingAndSortingRepository<T, ID> extends CrudRepository<T, ID> {

	// 将查询的结果进行排序
	Iterable<T> findAll(Sort sort);

	// 将查询的结果进行分页
	Page<T> findAll(Pageable pageable);
}
```

其中 `Pageable` 的实现类 `PageRequest`  的构造函数可以传入 Sort 排序的功能，用 `PageRequest.of()` 方法进行构造参数传递



### 自定义查询存储层

