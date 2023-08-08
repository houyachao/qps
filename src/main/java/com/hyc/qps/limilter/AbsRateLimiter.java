package com.hyc.qps.limilter;

import com.hyc.qps.enums.LimiterType;
import com.hyc.qps.rule.RateLimitRule;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

/**
 * 类<code>AbsRateLimiter</code>说明：默认实现，抽象类
 *
 * @author houyachao
 * @since 2023/08/08
 */
public abstract class AbsRateLimiter implements RateLimiter {

    protected static final String REDIS_PREFIX = "MPS:QPS:LIMIT:";

    protected RedisTemplate<String, Object> redisTemplate;

    public AbsRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    protected String getRealKey(String key) {
        return REDIS_PREFIX.concat(key);
    }

    @Override
    public boolean tryAcquire(RateLimitRule rule, Object merchantIdOrShopIdOrNull) {

        // 校验参数
        String validate = rule.validate(getType());
        if (!StringUtils.isEmpty(validate)) {
            throw new IllegalArgumentException("限流参数错误: " + validate);
        }
        // 获取访问
        if (acquire(rule, merchantIdOrShopIdOrNull)) {
            return true;
        }

        return false;
    }

    @Override
    public long getWaitTime(RateLimitRule rule, Object merchantIdOrShopIdOrNull) {
        return 0;
    }

    protected abstract boolean acquire(RateLimitRule rule, Object merchantIdOrShopIdOrNull);

    protected abstract LimiterType getType();
}
