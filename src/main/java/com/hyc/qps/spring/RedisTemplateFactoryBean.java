package com.hyc.qps.spring;

import com.hyc.qps.context.RedisTemplateContext;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 类<code>RedisTemplateFactoryBean</code>说明：
 *
 * @author houyachao
 * @since 2023/08/07
 */
public class RedisTemplateFactoryBean implements FactoryBean<RedisTemplate<String, Object>> {

    private String redisBeanName;

    private RedisTemplateContext redisTemplateContext;

    public static final String REDIS_BEAN_PROPERTY_NAME = "redisBeanName";

    public static final String REDIS_MAP_PROPERTY_NAME = "redisTemplateContext";

    public RedisTemplateFactoryBean() {
    }

    public RedisTemplate<String, Object> getObject() throws Exception {
        return this.redisTemplateContext.getRedisTemplate(this.redisBeanName);
    }

    public Class<?> getObjectType() {
        return RedisTemplate.class;
    }

    public String getRedisBeanName() {
        return this.redisBeanName;
    }

    public RedisTemplateContext getRedisTemplateContext() {
        return this.redisTemplateContext;
    }

    public void setRedisBeanName(String redisBeanName) {
        this.redisBeanName = redisBeanName;
    }

    public void setRedisTemplateContext(RedisTemplateContext redisTemplateContext) {
        this.redisTemplateContext = redisTemplateContext;
    }
}
