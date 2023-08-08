package com.hyc.qps.enums;

/**
 * 方法<code>LimiterType</code>说明: 限流规则
 *
 * @author houyachao
 * @since 2023/8/7
 */
public enum LimiterType {

    // 计数器
    COUNTER,

    // 滑动窗口
    SLIDING_WINDOW,

    // 令牌桶
    TOKEN_BUCKET;
}
