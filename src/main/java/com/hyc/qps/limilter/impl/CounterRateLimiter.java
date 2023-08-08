package com.hyc.qps.limilter.impl;

import com.hyc.qps.enums.LimiterType;
import com.hyc.qps.limilter.AbsRateLimiter;
import com.hyc.qps.rule.RateLimitRule;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 类<code>CounterRateLimiter</code>说明：计数器
 *
 * @author houyachao
 * @since 2023/08/08
 */
public class CounterRateLimiter extends AbsRateLimiter {

    public CounterRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    protected boolean acquire(RateLimitRule rule, Object merchantIdOrShopIdOrNull) {
        // todo 待实现
        return false;
    }

    @Override
    protected LimiterType getType() {
        return LimiterType.COUNTER;
    }
}
