package com.hyc.qps.spring;

import com.hyc.qps.properties.RedisProperties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import java.util.Iterator;

/**
 * 类<code>RedisTemplateBeanRegistryPostProcessor</code>说明：
 *
 * @author houyachao
 * @since 2023/08/07
 */
public class RedisTemplateBeanRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, ApplicationContextAware {
    private Environment environment;
    private ApplicationContext applicationContext;

    public RedisTemplateBeanRegistryPostProcessor() {
    }

    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        BindResult<RedisProperties> restServiceBindResult = Binder.get(this.environment).bind("mdc.redis", RedisProperties.class);
        RedisProperties redisProperties = (RedisProperties)restServiceBindResult.get();
        boolean existRedisMap = registry.containsBeanDefinition("redisTemplateContext");
        if (redisProperties.isAutoWrite() && existRedisMap) {
            Iterator var5 = redisProperties.getServers().keySet().iterator();

            while(var5.hasNext()) {
                String beanName = (String)var5.next();
                if (!beanName.equals(redisProperties.getPrimary())) {
                    BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RedisTemplateFactoryBean.class);
                    beanDefinitionBuilder.setAutowireMode(2);
                    beanDefinitionBuilder.addPropertyReference("redisTemplateContext", "redisTemplateContext");
                    beanDefinitionBuilder.addPropertyValue("redisBeanName", beanName);
                    if (registry.containsBeanDefinition(beanName)) {
                        throw new RuntimeException("RedisTemplate bean [" + beanName + "] exists and cannot be registered ");
                    }

                    registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
                }
            }
        }

    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
