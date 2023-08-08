package com.hyc.qps.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.hyc.qps.context.RedisTemplateContext;
import com.hyc.qps.constant.RedisConstant;
import com.hyc.qps.properties.RedisItemProperties;
import com.hyc.qps.properties.RedisProperties;
import com.hyc.qps.spring.RedisTemplateBeanRegistryPostProcessor;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类<code>RedisAutoConfiguration</code>说明： redis 配置类， 这个模块可以单独抽离出去的
 *
 * @author houyachao
 * @since 2023/8/7
 */
@Configuration
@ConditionalOnProperty(
        prefix = RedisConstant.REDIS_PREF,
        name = {"enable"},
        havingValue = "true"
)
@EnableConfigurationProperties({RedisProperties.class})
public class RedisAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RedisAutoConfiguration.class);

    public RedisAutoConfiguration() {
    }

    @Primary
    @Bean
    public RedisTemplate redisTemplate(RedisProperties redisProperties, RedisTemplateContext redisTemplateContext) {
        if (redisTemplateContext.getRedisTemplateMap().containsKey(redisProperties.getPrimary())) {
            return redisTemplateContext.getRedisTemplate(redisProperties.getPrimary());
        } else {
            log.error("RedisTemplate primary name [{}]  not exist ", redisProperties.getPrimary());
            return null;
        }
    }

    @Bean
    public RedisTemplateContext redisTemplateContext(@Qualifier("lettuceConnectionFactoryMap") Map<String, LettuceConnectionFactory> lettuceConnectionFactoryMap, RedisProperties redisProperties) {
        Map<String, RedisTemplate<String, Object>> redisTemplateMap = new ConcurrentHashMap();
        Iterator var4 = redisProperties.getServers().keySet().iterator();

        while (var4.hasNext()) {
            String name = (String) var4.next();
            RedisTemplate<String, Object> template = new RedisTemplate();
            ((LettuceConnectionFactory) lettuceConnectionFactoryMap.get(name)).afterPropertiesSet();
            template.setConnectionFactory((RedisConnectionFactory) lettuceConnectionFactoryMap.get(name));
            template.setDefaultSerializer(new StringRedisSerializer());
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(this.buildJackson2JsonRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());
            template.setHashValueSerializer(this.buildJackson2JsonRedisSerializer());
            template.afterPropertiesSet();
            redisTemplateMap.put(name, template);
        }

        return new RedisTemplateContext(redisTemplateMap);
    }

    @Primary
    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisTemplateContext redisTemplateContext, RedisProperties redisProperties) {
        if (redisTemplateContext.getRedisTemplateMap().containsKey(redisProperties.getPrimary())) {
            return redisTemplateContext.getRedisTemplate(redisProperties.getPrimary()).getConnectionFactory();
        } else {
            log.error("RedisConnectionFactory primary name [{}]  not exist ", redisProperties.getPrimary());
            return null;
        }
    }

    @Bean(
            destroyMethod = "shutdown"
    )
    @ConditionalOnMissingBean({ClientResources.class})
    public DefaultClientResources lettuceClientResources() {
        return DefaultClientResources.create();
    }

    @Bean(
            name = {"lettuceConnectionFactoryMap"}
    )
    public Map<String, LettuceConnectionFactory> lettuceConnectionFactoryMap(RedisProperties redisProperties, DefaultClientResources clientResources) {
        Map<String, LettuceConnectionFactory> lettuceConnectionFactoryMap = new ConcurrentHashMap();
        Iterator var4 = redisProperties.getServers().keySet().iterator();

        while (var4.hasNext()) {
            String name = (String) var4.next();
            RedisItemProperties redisItemProperties = (RedisItemProperties) redisProperties.getServers().get(name);
            RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
            redisStandaloneConfiguration.setHostName(redisItemProperties.getHost());
            redisStandaloneConfiguration.setPassword(RedisPassword.of(redisItemProperties.getPassword()));
            redisStandaloneConfiguration.setPort(redisItemProperties.getPort());
            redisStandaloneConfiguration.setDatabase(redisItemProperties.getDatabase());
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMaxTotal(redisItemProperties.getMaxActive());
            poolConfig.setMaxIdle(redisItemProperties.getMaxIdle());
            poolConfig.setMinIdle(redisItemProperties.getMinIdle());
            poolConfig.setMaxWaitMillis((long) redisItemProperties.getMaxWait());
            LettuceClientConfiguration lettuceClientConfiguration = LettucePoolingClientConfiguration.builder().commandTimeout(Duration.ofMillis(redisItemProperties.getTimeout())).clientResources(clientResources).poolConfig(poolConfig).build();
            lettuceConnectionFactoryMap.put(name, new LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration));
        }

        return lettuceConnectionFactoryMap;
    }

    @Bean
    @ConditionalOnMissingBean
    public Jackson2JsonRedisSerializer buildJackson2JsonRedisSerializer() {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build();
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        return jackson2JsonRedisSerializer;
    }

    @Bean
    public RedisTemplateBeanRegistryPostProcessor redisTemplateBeanRegistryPostProcessor() {
        return new RedisTemplateBeanRegistryPostProcessor();
    }
}
