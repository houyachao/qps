package com.hyc.qps.enums;

/**
* 方法<code>Fallback</code>说明: 限流后的处理策略
* @author houyachao
* @since 2023/8/7
*
*/
public enum Fallback {

    // 异常
    EXCEPTION,

    // 回调方法
    CALLBACK;
}
