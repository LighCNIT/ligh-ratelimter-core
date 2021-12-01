package com.gplus.ratelimiter.core.contants;

/**
 * 限流类型枚举
 */
public enum LimitingTypeEnum {
    /**
     * 计数器
     */
    COUNTER,
    /**
     * 最大时间窗
     */
    MAX_TIME_WINDOW,
    /**
     * 令牌桶
     */
    TOKEN_BUCKET,
    ;

    LimitingTypeEnum(){}
}
