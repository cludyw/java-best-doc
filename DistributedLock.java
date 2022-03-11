package com.hzins.travel.byt.starter.redis.lock;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 * @author hz21056680
 * @date 2021/11/12 11:38
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DistributedLock {
    /**
     * 锁的名称
     */
    String name() default "";

    /**
     * 锁的key
     */
    String key() default "";

    /**
     * 是否为尝试获取锁，获取锁失败返回业务异常
     *
     * @return 默认为不尝试获取锁，等待锁直到释放
     */
    boolean tryLock() default false;

    /**
     * {@link #tryLock} 为true时，等待获取锁的时间
     * @return 默认不等待
     */
    int waitTime() default -1;

    /**
     * 持有锁时间，到期后自动释放锁
     * @return 默认自动续锁
     */
    int leaseTime() default -1;

    /**
     * 时间单位
     * @return 默认秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
