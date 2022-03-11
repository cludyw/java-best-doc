import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class DistributedLockAspect {
    @Autowired
    RedissonClient redissonClient;

    @Pointcut("@annotation(lock.DistributedLock) && within(com..*)")
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
