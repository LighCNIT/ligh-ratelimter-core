package com.gplus.ratelimiter.core.annonation;

import com.gplus.ratelimiter.core.contants.LimitingTypeEnum;

import java.lang.annotation.*;

/**
 * 限流注解
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限流类型 默认最大时间窗
     */
    LimitingTypeEnum limitType() default LimitingTypeEnum.MAX_TIME_WINDOW;

    /**
     * 单位时间限制通过请求数
     * @return
     */
    long limit() default 10;

    /**
     * 过期时间，单位秒(最大时间窗口)
     * @return
     */
    long expire() default 1;

    /**
     * 限流提示语
     * @return
     */
    String message() default "false";

}
