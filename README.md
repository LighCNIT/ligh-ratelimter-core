# ligh-ratelimter-core
限流算法
基于时间窗口的限流算法

使用 redis+lua脚本实现
通过自定义注解配置限流规则，可配置在类上，方法上
规则范围查找 method - class - super class 所以会存在规则失效的情况

注意 ： 项目还未通过测试
