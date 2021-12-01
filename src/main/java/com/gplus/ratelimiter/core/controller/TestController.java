package com.gplus.ratelimiter.core.controller;

import com.gplus.ratelimiter.core.annonation.RateLimiter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试接口
 */
@RestController
@RateLimiter
public class TestController {

    private static final String MESSAGE = "{\"code\":\"400\",\"msg\":\"FAIL\",\"desc\":\"触发限流\"}";

    @GetMapping("/normal-request")
//    @RateLimiter(limit = 2, expire = 3600,message = MESSAGE)
    public String normalRequest(){
        return "正常请求";
    }
}
