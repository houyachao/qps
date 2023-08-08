package com.hyc.qps.context;

import org.springframework.data.redis.core.RedisTemplate;
import java.util.Map;

/**
 * 类<code>RedisTemplateContext</code>说明： Redis上下文
 *
 * @author houyachao
 * @since 2023/08/07
 */
public class RedisTemplateContext {

    private Map<String, RedisTemplate<String, Object>> redisTemplateMap;

    public RedisTemplateContext(Map<String, RedisTemplate<String, Object>> redisTemplateMap) {
        this.redisTemplateMap = redisTemplateMap;
    }

    public Map<String, RedisTemplate<String, Object>> getRedisTemplateMap() {
        return this.redisTemplateMap;
    }

    public RedisTemplate getRedisTemplate(String name) {
        return (RedisTemplate)this.redisTemplateMap.get(name);
    }
}
