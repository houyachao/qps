package com.hyc.qps.limilter.impl;

import com.hyc.qps.enums.LimiterType;
import com.hyc.qps.limilter.AbsRateLimiter;
import com.hyc.qps.rule.RateLimitRule;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 类<code>SlidingWindowRateLimiter</code>说明： 滑动窗口
 *
 * @author houyachao
 * @since $
 */
public class SlidingWindowRateLimiter extends AbsRateLimiter {

    public SlidingWindowRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    protected boolean acquire(RateLimitRule rule, Object merchantIdOrShopIdOrNull) {
        // todo 待实现
        return false;
    }

    @Override
    protected LimiterType getType() {
        return LimiterType.SLIDING_WINDOW;
    }
}
