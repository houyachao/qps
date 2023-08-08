package com.hyc.qps.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * 类<code>RedisItemProperties</code>说明：多个Redis配置
 *
 * @author houyachao
 * @since 2023/8/7
 */
@Getter
@Setter
public class RedisItemProperties {

    private String host;
    private Integer port;
    private String password;
    private Integer database;
    private Long timeout = 3000L;
    private Integer maxActive = 11;
    private Integer maxIdle = 3;
    private Integer maxWait = 5;
    private Integer minIdle = 0;

    public RedisItemProperties() {
    }
}
