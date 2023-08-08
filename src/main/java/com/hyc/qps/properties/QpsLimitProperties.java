package com.hyc.qps.properties;

import com.hyc.qps.constant.QpsConstant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 类<code>QpsLimitProperties</code>说明： Qps限流配置文件
 *
 * @author houyachao
 * @since 2023/8/7
 */
@Setter
@Getter
@ConfigurationProperties(prefix = QpsConstant.QPS_LIMIT_PREF)
public class QpsLimitProperties {

    /**
     * 是否启用
     */
    private boolean enable;

    /**
     * 指定使用的RedisTemplate
     */
    private String redisTemplateBean;
}
