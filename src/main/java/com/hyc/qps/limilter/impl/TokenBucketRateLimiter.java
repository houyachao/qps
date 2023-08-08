package com.hyc.qps.limilter.impl;

import com.hyc.qps.enums.LimiterType;
import com.hyc.qps.limilter.AbsRateLimiter;
import com.hyc.qps.rule.RateLimitRule;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import java.util.Collections;
import java.util.Objects;

/**
 * 类<code>TokenBucketRateLimiter</code>说明：令牌桶
 *
 * @author houyachao
 * @since 2023/08/08
 */
public class TokenBucketRateLimiter extends AbsRateLimiter {

    /** lua 脚本 */
    private static final DefaultRedisScript<Long> QPS_TOKEN_BUCKET_SCRIPT = new DefaultRedisScript<>();

    static {
        QPS_TOKEN_BUCKET_SCRIPT.setResultType(Long.class);
        Resource resource = new UrlResource(Objects.requireNonNull(
                TokenBucketRateLimiter.class.getClassLoader().getResource("redis\\qps_limit_by_token_bucket.lua")));
        QPS_TOKEN_BUCKET_SCRIPT.setScriptSource(new ResourceScriptSource(resource));
    }

    public TokenBucketRateLimiter(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

    @Override
    protected boolean acquire(RateLimitRule rule, Object merchantIdOrShopIdOrNull) {
        // 有可能是merchantId或shopId维度限流，或不根据任何维度限流
        String realKey = getRealKey(rule.getKey().concat(":").concat(String.valueOf(merchantIdOrShopIdOrNull)));

        long now = System.currentTimeMillis();

        return redisTemplate.execute(QPS_TOKEN_BUCKET_SCRIPT, Collections.singletonList(realKey), rule.getLimit(), rule.getRate(), now) != null;
    }

    @Override
    protected LimiterType getType() {
        return LimiterType.TOKEN_BUCKET;
    }

    /**
     * 获取等待时间（仅供失败的时候获取）
     *
     * @param rule 规则
     * @return 时间
     */
    @Override
    public long getWaitTime(RateLimitRule rule, Object merchantIdOrShopIdOrNull) {
        // 获取真实KEY
        String realKey = getRealKey(rule.getKey().concat(":").concat(String.valueOf(merchantIdOrShopIdOrNull)));
        // 生成一个token多少毫秒
        long msPerToken = (long) (1000 / rule.getRate());
        // 当前时间
        long now = System.currentTimeMillis();
        // 获取上一次的时间
        Object lastTime = redisTemplate.opsForHash().get(realKey, "last_time");
        if (lastTime == null) {
            return 0;
        }

        Long lastTimeVal = (Long) lastTime;
        // (上次时间 + 生成一个token的时间) - 当前时间
        return lastTimeVal + msPerToken - now;
    }
}
