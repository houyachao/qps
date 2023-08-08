package com.hyc.qps.limilter;

import com.hyc.qps.rule.RateLimitRule;

public interface RateLimiter {

    boolean tryAcquire(RateLimitRule rule, Object merchantIdOrShopIdOrNull);

    long getWaitTime(RateLimitRule rule, Object merchantIdOrShopIdOrNull);
}
