# mount-component-parent
公共组件介绍
1. 分布式锁
2. redis限流器
3. 本地及redis二级缓存

# 组件使用
## 分布式锁
### Maven 引用
```xml
        <dependency>
            <groupId>icu.develop</groupId>
            <artifactId>mount-lock-starter</artifactId>
            <version>1.0.1</version>
        </dependency>
```
### 注解使用
```
    @RedisLock(lockKey = "testKey", waitTime = 2, lockedTime = 10)
    void testAnnotation() {
    
    }
```
lockKey: 分布式锁的Key
waitTime：获取锁时最大等待时间，默认2秒
lockedTime：最大持有锁时间，默认-1，表示等待系统自动释放锁，单位秒

### 程序使用
```
    @Resource
    RedisLockProceeding redisLockProceeding;

    void testLock() {
        long waitTime = 2;
        long leaseTime = 20;
        try {
            Object lockKey = redisLockProceeding.locked("lockKey", waitTime, leaseTime, () -> {
                // 执行业务逻辑代码
                return null;
            });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
```

## 限流器
### maven依赖
```xml
        <dependency>
            <groupId>icu.develop</groupId>
            <artifactId>mount-limiter-starter</artifactId>
            <version>1.0.1</version>
        </dependency>
```
### 注解使用
```
    @RedisRateLimiter(limiterKey="limiterKey", timeout = 100, limitCount = 10)
    void testAnnotation() {
        
    }
   
```
limiterKey: 限流器的key
timeout： 限流器限流的间隔时间，超过这个时间，将记录下一个限流周期
limitCount： 在限流周期内，允许令牌数量

### 程序使用
```
    @Resource
    RedisLimiterProceeding redisLimiterProceeding;

    void testLock() {
        long timeout = 2;
        long count = 20;
        try {
            Object limiter = redisLimiterProceeding.limiter("limitKey", timeout, count, () -> {
                // 执行业务逻辑代码
                return null;
            });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
```

## 二级缓存

### 注解使用
类似Spring 的Cache注解
其中包括：
L2Cacheable，L2CacheEvict，L2CachePut，L2Caching

**具体使用请参照mount-example项目中的例子**







