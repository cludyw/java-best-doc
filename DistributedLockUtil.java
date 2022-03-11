import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class DistributedLockUtil {
    /**
     * 默认最多等待锁10秒
     */
    private static final int DEFAULT_WAIT_TIME_SECOND = 10;
    /**
     * 最多默认持有锁30秒
     */
    private static final int DEFAULT_LEASE_TIME_SECOND = 30;

    private static RedissonClient client;

    /**
     * 尝试获取锁，如果到达最大等待时间时，都未获取到锁，则不再等待锁
     * @param key 锁的key
     * @param waitTime 最多等待锁时间
     * @param unit 时间单位
     * @param runnable 执行函数
     * @return 是否加锁成功
     */
    public static boolean tryLock(String key, long waitTime, TimeUnit unit, Runnable runnable) {
        RLock lock = client.getLock(key);
        try {
            boolean locked = lock.tryLock(waitTime, unit);
            if (!locked) {
                return false;
            }
            runnable.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            lock.unlock();
        }
        return true;
    }

    public static boolean tryLock(String key, Runnable runnable) {
        return tryLock(key, DEFAULT_WAIT_TIME_SECOND, TimeUnit.SECONDS, runnable);
    }

    /**
     * 阻塞获取锁，直到锁释放，并且自动锁续租
     * @param key 锁的key
     * @param runnable 执行函数
     */
    public static void lock(String key, Runnable runnable) {
        RLock lock = client.getLock(key);
        try {
            lock.lock();
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    public static <T> T lock(String key, Supplier<T> supplier) {
        RLock lock = client.getLock(key);
        try {
            lock.lock();
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    public static void setClient(RedissonClient client) {
        DistributedLockUtil.client = client;
    }
}
