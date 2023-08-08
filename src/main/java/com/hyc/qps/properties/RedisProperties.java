package com.hyc.qps.properties;

import com.hyc.qps.constant.RedisConstant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.Map;

/**
 * 类<code>RedisProperties</code>说明：redis 配置类
 *
 * @author houyachao
 * @since 2023/8/7
 */
@Setter
@Getter
@ConfigurationProperties(prefix = RedisConstant.REDIS_PREF)
public class RedisProperties {

    private boolean enable;
    private boolean autoWrite = true;
    private String primary = "default";
    private Map<String, RedisItemProperties> servers;
}
