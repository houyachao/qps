package com.hyc.qps.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * 类<code>RedisPoolProperties</code>说明： Redis线程池配置
 *
 * @author houyachao
 * @since 2023/8/7
 */
@Setter
@Getter
public class RedisPoolProperties {

    private Integer timeout = 3000;

    private Integer maxActive = 11;

    private Integer maxIdle = 3;

    private Integer maxWait = 5;

    private Integer minIdle = 0;

    private Integer size = 64;

    public RedisPoolProperties() {
    }
}
