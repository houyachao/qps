package com.hyc.qps.rule;

import com.hyc.qps.enums.DimensionType;
import com.hyc.qps.enums.LimiterType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;
import java.util.concurrent.TimeUnit;

/**
 * 类<code>RateLimitRule</code>说明：限流规则类
 *
 * @author houyachao
 * @since 2023/08/07
 */
@Getter
@Setter
public class RateLimitRule {

    /**
     * 业务Key
     */
    private String key;

    /**
     * 限流维度，merchantId，shopId，null
     * <p>
     * 如果merchantId，方法参数需要传 merchantId
     * 如果shopId，  方法参数需要传 shopId
     * 如果null，可以什么都不传
     */
    private DimensionType dimension;

    /**
     * 限流时间
     */
    private long time = 1L;

    /**
     * 时间单位
     */
    private TimeUnit unit = TimeUnit.SECONDS;

    /**
     * 限制数量
     */
    private long limit;

    /**
     * 添加令牌的速度（令牌桶模式），单位秒
     */
    private double rate;

    /**
     * 构造方法
     */
    public RateLimitRule() {
    }

    /**
     * builder构造方法
     */
    private RateLimitRule(Builder builder) {
        this.key = builder.key;
        this.time = builder.time;
        this.unit = builder.unit;
        this.limit = builder.limit;
        this.rate = builder.rate;
    }

    /**
     * 校验方法
     *
     * @return 错误消息，null表示校验通过
     */
    public String validate(LimiterType type) {
        if (StringUtils.isEmpty(key)) {
            return " 限流key不能为空";
        }

        if (LimiterType.TOKEN_BUCKET != type && time < 1) {
            return " 限流时间time不能小于1";
        }

        if (unit.ordinal() > TimeUnit.MINUTES.ordinal()) {
            return " 时间单位不支持HOURS、DAYS ";
        }

        if (limit < 1) {
            return " 限制数量limit不能小于1";
        }

        if (LimiterType.TOKEN_BUCKET == type && rate <= 0) {
            return " 令牌速率rate必须大于0";
        }
        return null;
    }

    /**
     * 获取一个通用规则
     *
     * @param key   业务key
     * @param limit 限流数量
     * @return builder实例
     */
    public static RateLimitRule.Builder commonRule(String key, long limit) {
        return new Builder().withKey(key).withLimit(limit);
    }

    /**
     * 获取一个令牌桶计数规则
     *
     * @param key   业务key
     * @param limit 限流数量
     * @param rate  添加令牌的速度（令牌桶模式），单位秒
     * @return builder实例
     */
    public static RateLimitRule.Builder tokenBucketRule(String key, long limit, double rate) {
        return new Builder().withKey(key).withLimit(limit).withRate(rate);
    }

    public static class Builder {

        private String key;

        private long time = 1L;

        private TimeUnit unit = TimeUnit.SECONDS;

        private long limit;

        private double rate;

        private DimensionType dimension;

        public Builder withKey(String key) {
            this.key = key;
            return this;
        }

        public Builder dimension(DimensionType dimension) {
            this.dimension = dimension;
            return this;
        }

        public Builder withTime(long time) {
            this.time = time;
            return this;
        }

        public Builder withUnit(TimeUnit unit) {
            this.unit = unit;
            return this;
        }

        public Builder withLimit(long limit) {
            this.limit = limit;
            return this;
        }

        public Builder withRate(double rate) {
            this.rate = rate;
            return this;
        }

        public RateLimitRule build() {
            return new RateLimitRule(this);
        }
    }
}