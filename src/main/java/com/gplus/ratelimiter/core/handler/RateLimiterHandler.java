package com.gplus.ratelimiter.core.handler;

import com.gplus.ratelimiter.core.annonation.RateLimiter;
import com.gplus.ratelimiter.core.contants.LimitingTypeEnum;
import com.gplus.ratelimiter.core.contants.RedisKeyMagic;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 限流处理器
 */
@Aspect
@Component
public class RateLimiterHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiter.class);

    @Resource
    private RedisTemplate redisTemplate;

    private DefaultRedisScript<Long> redisScript;

    @PostConstruct
    public void init(){
        redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Long.class);
    }

    @Pointcut("@within(com.gplus.ratelimiter.core.annonation.RateLimiter)")
    public void rateLimiterClass(){}

    @Pointcut("@annotation(com.gplus.ratelimiter.core.annonation.RateLimiter)")
    public void rateLimiterMethod(){}

    /**
     * 注解核心逻辑处理
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around(" rateLimiterClass() || rateLimiterMethod()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{
        if (LOGGER.isDebugEnabled()){
            LOGGER.debug("RateLimiterHandler[限流处理器]开始执行限流操作");
        }
        List<String> keyList = new ArrayList<>(4);
        Signature signature = proceedingJoinPoint.getSignature();
        Class<?> targetClass = proceedingJoinPoint.getTarget().getClass();
        RateLimiter rateLimiter;
        if (signature instanceof MethodSignature){
            MethodSignature methodSignature = (MethodSignature) signature;
            Method method = targetClass.getMethod(methodSignature.getName(),methodSignature.getParameterTypes());
            String limitKey = RedisKeyMagic.WINDOW_RATE_LIMITER_PREFIX+":"+targetClass.getName()+":"+ method.getName();
            keyList.add(limitKey);
            if (method.isAnnotationPresent(RateLimiter.class)){
                //如果方法里有该注解
                rateLimiter = method.getAnnotation(RateLimiter.class);
                return executeMaxTimeWindow(proceedingJoinPoint,rateLimiter,keyList);
            }else {
                //类上是否有该注解
                rateLimiter = targetClass.getAnnotation(RateLimiter.class);
                if(rateLimiter == null){
                    //父类是否有该注解
                    if (targetClass.isAnnotationPresent(RateLimiter.class)){
                        rateLimiter = targetClass.getAnnotatedSuperclass().getAnnotation(RateLimiter.class);
                        if (rateLimiter != null){
                            return executeMaxTimeWindow(proceedingJoinPoint,rateLimiter,keyList);
                        }
                    }
                }else {
                   return executeMaxTimeWindow(proceedingJoinPoint,rateLimiter,keyList);
                }
            }
        }
        return proceedingJoinPoint.proceed();
    }
    public Object executeMaxTimeWindow(ProceedingJoinPoint proceedingJoinPoint,RateLimiter rateLimiter,List<String> keyList)throws Throwable{
        if (rateLimiter.limitType() == LimitingTypeEnum.MAX_TIME_WINDOW){
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("max-time-window.lua")));
        }else {
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("token-bucket.lua")));
        }
        // 限流阈值
        long limitTimes = rateLimiter.limit();
        // 限流超时时间
        long expireTime = rateLimiter.expire();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("RateLimiterHandler[分布式限流处理器]参数值为-limitTimes={},limitTimeout={}", limitTimes, expireTime);
        }
        String message = rateLimiter.message();
        Long result = (Long) redisTemplate.execute(redisScript, keyList, expireTime, limitTimes);
        if (result == 0) {
            String msg = "由于超过单位时间=" + expireTime + "-允许的请求次数=" + limitTimes + "[触发限流]";
            LOGGER.debug(msg);
            return message;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("RateLimiterHandler[分布式限流处理器]限流执行结果-result={},请求[正常]响应", result);
        }
        return proceedingJoinPoint.proceed();
    }

}
