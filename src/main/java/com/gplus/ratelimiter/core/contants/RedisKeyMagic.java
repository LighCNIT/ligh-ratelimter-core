package com.gplus.ratelimiter.core.contants;

/**
 * 常量配置
 */
public class RedisKeyMagic {

    /**
     *  窗口限流key前缀
     */
    public final static String WINDOW_RATE_LIMITER_PREFIX = "window:rate:limiter";

    /**
     * 令牌桶限流key前缀
     */
    public final static String TOKEN_RATE_LIMITER_PREFIX = "token:rate:limiter";
}
