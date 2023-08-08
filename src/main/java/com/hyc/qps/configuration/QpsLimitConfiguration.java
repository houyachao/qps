package com.hyc.qps.configuration;

import com.hyc.qps.aop.QpsLimitAop;
import com.hyc.qps.constant.QpsConstant;
import com.hyc.qps.context.RedisTemplateContext;
import com.hyc.qps.factory.RateLimiterFactory;
import com.hyc.qps.properties.QpsLimitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 类<code>QpsLimitConfiguration</code>说明： Qps限流配置类
 *
 * @author houyachao
 * @since 2023/8/7
 */
@Configuration
@EnableAspectJAutoProxy
@ConditionalOnProperty(prefix = QpsConstant.QPS_LIMIT_PREF, name = "enable", havingValue = "true")
@EnableConfigurationProperties({QpsLimitProperties.class})
public class QpsLimitConfiguration {

    @Bean
    public RateLimiterFactory rateLimiterFactory(RedisTemplateContext redisTemplateContext,
                                                 QpsLimitProperties qpsLimitProperties) {
        String redisTemplateBeanKey = qpsLimitProperties.getRedisTemplateBean();
        if (!redisTemplateContext.getRedisTemplateMap().containsKey(redisTemplateBeanKey)) {
            throw new RuntimeException(" redisTemplateBean配置不正确, 请检查mdc.qps-limit.redis-template-bean");
        }
        return new RateLimiterFactory(redisTemplateContext.getRedisTemplate(redisTemplateBeanKey));
    }

    @Bean
    public QpsLimitAop qpsLimitAop(RateLimiterFactory rateLimiterFactory) {
        return new QpsLimitAop(rateLimiterFactory);
    }
}
