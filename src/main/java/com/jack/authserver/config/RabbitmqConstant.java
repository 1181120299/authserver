package com.jack.authserver.config;

/**
 * 定义RabbitMq相关的队列、交换、路由常量
 */
public enum RabbitmqConstant {
    /**
     * 自定义用户相关的队列等信息
     */
    CUSTOM_USER(
            "jack-custom-user-queue",
            "jack-custom-user-exchange",
            "jack-custom-user-routeKey"
    ),
    /**
     * 认证授权中心用户相关的队列等信息
     */
    SPRING_SECURITY_USER(
            "jack-spring-user-queue",
            "jack-spring-user-exchange",
            "jack-spring-user-routeKey"
    ),
    /**
     * 死信相关的队列等信息
     */
    DEAD_LETTER(
            "jack-dead-letter-queue",
            "jack-dead-letter-exchange",
            "jack-dead-letter-routing-key"
    );

    public final String queue;
    public final String exchange;
    public final String routeKey;

    RabbitmqConstant(String queue, String exchange, String routeKey) {
        this.queue = queue;
        this.exchange = exchange;
        this.routeKey = routeKey;
    }
}
