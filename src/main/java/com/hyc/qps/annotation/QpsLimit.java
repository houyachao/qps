package com.hyc.qps.annotation;

import com.hyc.qps.enums.DimensionType;
import com.hyc.qps.enums.Fallback;
import com.hyc.qps.enums.LimiterType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 方法<code>QpsLimit</code>说明: QPS限流注解
 *
 * @author houyachao
 * @since 2023/8/7
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface QpsLimit {

    /**
     * 方法<code>type</code>说明: 规则类型
     *
     * @return 规则类型
     */
    LimiterType type() default LimiterType.TOKEN_BUCKET;

    /**
     * 业务Key
     *
     * @return 业务Key
     */
    String key();

    /**
     * 限流维度，merchantId，shopId，null
     *
     * 如果merchantId，方法参数需要传 merchantId
     * 如果shopId，  方法参数需要传 shopId
     * 如果null，可以什么都不传
     *
     * @return 限流维度
     */
    DimensionType dimension() default DimensionType.NULL;

    /**
     * 限流时间
     *
     * @return 限流时间
     */
    long time() default 1L;

    /**
     * 限流时间单位
     *
     * @return 限流时间单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 限制数量
     *
     * @return 限制数量
     */
    long limit() default 1L;

    /**
     * 每秒添加令牌的速度（令牌桶模式）
     *
     * @return 速率
     */
    double rate() default 0;

    /**
     * 限流后的处理策略，默认抛出异常
     *
     * @return 处理策略
     */
    Fallback fallback() default Fallback.EXCEPTION;

    /**
     * 回调方法名（限流后的处理策略为 CALLBACK 的时候执行）
     *
     * @return 回调方法名
     */
    String callbackMethod() default "";
}
