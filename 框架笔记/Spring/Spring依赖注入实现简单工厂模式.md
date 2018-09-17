# Spring依赖注入实现简单工厂模式

## 背景

想写一个简单的[策略模式](https://blog.csdn.net/ycxzuoxin/article/details/80469048)+简单工厂模式的抽奖算法.

下面写的是一个利用传入的抽奖类型调用不同的增加抽奖次数的方法.

首先写一个抽象工厂`AbstractLotteryType类`:

```java
public abstract class AbstractLotteryType {
    /**
        增加抽奖次数的方法
        {@see userId} 用户Id
     */
    public abstract void addLotteryNumber(String userId);
}
```

然后写几个抽奖类型

`DrawLotteryType`

```java
public class DrawLotteryType extends AbstractLotteryType {
    @Override
    public void addLotteryNumber(String userId) {
        System.out.println("执行了DrawLotteryType的新增方法");
    }
}
```

`GoldenLotteryType`

```java
public class GoldenLotteryType extends AbstractLotteryType {
    @Override
    public void addLotteryNumber(String userId) {
        System.out.println("执行了GoldenLotteryType的新增方法");
    }
}
```

`SingleLotteryType`

```java
public class SingleLotteryType extends AbstractLotteryType {
    @Override
    public void addLotteryNumber(String userId) {
        System.out.println("执行了SingleLotteryType的新增方法");
    }
}
```

然后写出抽奖类型的简单工厂`LotteryTypeFactory`

```java
public class LotteryTypeFactory {
    private AbstractLotteryType lotteryType;

    public LotteryTypeFactory(String type){
        switch (type){
            case "SINGLE":
                lotteryType = new SingleLotteryType();
                break;
            case "DRAW":
                lotteryType = new DrawLotteryType();
                break;
            case "GOLDEN":
                lotteryType = new GoldenLotteryType();
                break;
            default:
                throw new RuntimeException("没有该类型活动");
        }
    }

    public AbstractLotteryType newInstence(){
        return lotteryType;
    }
}
```

然后是抽奖类型的枚举类:

```java
public enum LotteryTypeEnum {
    SINGLE,
    GOLDEN,
    DRAW
}
```

测试代码:

```java
public class demo {
    public static void main(String[] args) {
        LotteryTypeFactory singleFactory = 
                new LotteryTypeFactory(LotteryTypeEnum.SINGLE.name());
        singleFactory.newInstence().addLotteryNumber("3");
        LotteryTypeFactory goldenFactory = 
                new LotteryTypeFactory(LotteryTypeEnum.GOLDEN.name());
        goldenFactory.newInstence().addLotteryNumber("3");
    }
}
```

运行结果:

```properties
执行了SingleLotteryType的新增方法
执行了GoldenLotteryType的新增方法
```

## 问题

刚准备写入工作中的代码时,想起来IOC本来就是一个容器,何必还要自己写一个工厂去生产.于是转换了思路,利用Spring当做一个工厂(当然其实Spring中一般用到的是单例),去管理我们的几个抽奖类型.

## 实现

首先写一个抽奖类型的接口`ILottery`

```java
public interface ILottery {
    void addLotteryTimes();
}
```

然后写两个抽奖类型事件这个接口

`SingleLottery`

```java
@Component("SINGLE")
public class SingleLottery implements ILottery {

    @Autowired
    private LotteryService lotteryService;

    @Override
    public void addLotteryTimes() {
        System.out.println("调用了SingleLottery的addLotteryTimes方法");
        lotteryService.addLotteryTimes();
    }
}
```

`GoldenLottery`

```java
@Component("GOLDEN")
public class GoldenLottery implements ILottery {

    @Autowired
    private LotteryService lotteryService;

    @Override
    public void addLotteryTimes() {
        System.out.println("调用了GoldenLottery的addLotteryTimes方法");
        lotteryService.addLotteryTimes();
    }
}
```

还有一个工厂`LotteryFactory`去获取这两个抽奖类型的bean

```java
@Component
public class LotteryFactory {

    @Autowired
    private Map<String, ILottery> map;

    public ILottery getLotteryFunction(String type){
        return map.get(type);
    }
}
```

为了看是否能注入到Spring的factoryBean,我增加了Service层来测试

```java
public interface LotteryService {
    void addLotteryTimes();
}
```

```java
@Service
public class LotteryServiceImpl implements LotteryService {
    @Override
    public void addLotteryTimes() {
        System.out.println("调用了LotteryServiceImpl的addLotteryTimes方法");
    }
}
```

测试代码:

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class DemoTests {

    @Autowired
    private LotteryFactory lotteryFactory;

    @Test
    public void factoryTest() {
        ILottery goldenLottery = lotteryFactory.getLotteryFunction(LotteryTypeEnum.GOLDEN.name());
        goldenLottery.addLotteryTimes();
        ILottery singleLottery = lotteryFactory.getLotteryFunction(LotteryTypeEnum.SINGLE.name());
        singleLottery.addLotteryTimes();
    }
}
```

运行结果:

```properties
调用了GoldenLottery的addLotteryTimes方法
调用了LotteryServiceImpl的addLotteryTimes方法
调用了SingleLottery的addLotteryTimes方法
调用了LotteryServiceImpl的addLotteryTimes方法
```

说明注入成功.

## 总结

如果没有Spring,我们也可以利用工厂模式配上策略模式达到效果,但是性能和解耦方面可能就没有Spring做的那么好,但是这里学到了如果用Spring容器达到工厂模式的效果.

