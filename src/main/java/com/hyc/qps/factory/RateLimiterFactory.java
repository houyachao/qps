package com.hyc.qps.factory;

import com.hyc.qps.enums.LimiterType;
import com.hyc.qps.exception.QpsLimitException;
import com.hyc.qps.limilter.RateLimiter;
import com.hyc.qps.limilter.impl.CounterRateLimiter;
import com.hyc.qps.limilter.impl.SlidingWindowRateLimiter;
import com.hyc.qps.limilter.impl.TokenBucketRateLimiter;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 类<code>RateLimiterFactory</code>说明： 此类功能是限流器工厂类
 *
 * @author houyachao
 * @since 2023/08/08
 */
public class RateLimiterFactory {

    /**
     * 限流器Map（避免后续可能导致的线程安全问题，这里使用ConcurrentMap）
     */
    private static final ConcurrentMap<LimiterType, RateLimiter> RATE_LIMITER_MAP = new ConcurrentHashMap<>();

    public RateLimiterFactory(RedisTemplate<String, Object> redisTemplate) {
        RATE_LIMITER_MAP.put(LimiterType.COUNTER, new CounterRateLimiter(redisTemplate));
        RATE_LIMITER_MAP.put(LimiterType.SLIDING_WINDOW, new SlidingWindowRateLimiter(redisTemplate));
        RATE_LIMITER_MAP.put(LimiterType.TOKEN_BUCKET, new TokenBucketRateLimiter(redisTemplate));
    }

    public RateLimiter create(LimiterType limiterType) {
        RateLimiter rateLimiter = RATE_LIMITER_MAP.get(limiterType);
        if (rateLimiter == null) {
            throw new QpsLimitException(500, " RuleType: " + limiterType.name() + " 没有对应的RateLimiter实现类");
        }
        return rateLimiter;
    }

    public RateLimiter getCounterRateLimiter() {
        return create(LimiterType.COUNTER);
    }

    public RateLimiter getSlidingWindowRateLimiter() {
        return create(LimiterType.SLIDING_WINDOW);
    }

    public RateLimiter getTokenBucket() {
        return create(LimiterType.TOKEN_BUCKET);
    }
}
