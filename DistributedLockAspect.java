/*
 * Copyright (c) 2006-2021 Hzins Ltd. All Rights Reserved.
 * <p>
 * This code is the confidential and proprietary information of
 * Hzins.You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the agreements
 * you entered into with Hzins,https://www.huize.com.
 * </p>
 */
package com.hzins.travel.byt.starter.redis.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author hz21056680
 * @date 2021/11/12 12:32
 */
@Component
@Aspect
public class DistributedLockAspect {
    @Autowired
    RedissonClient redissonClient;

    @Pointcut("@annotation(com.hzins.travel.byt.starter.redis.lock.DistributedLock) && within(com.hzins.travel..*)")
    public void pointcut() {}

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {
        DistributedLock distributedLock = getAnnotation(joinPoint);
        return null;
    }

    private DistributedLock getAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getAnnotation(DistributedLock.class);
    }

    String getLockKey(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        return null;
    }
}
