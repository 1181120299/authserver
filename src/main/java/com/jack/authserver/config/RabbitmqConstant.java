package com.jack.authserver.config;

/**
 * 定义RabbitMq相关的队列、交换、路由常量
 */
public enum RabbitmqConstant {
    /**
    * 新增自定义用户相关的队列等信息
    */
     CUSTOM_USER_ADD(
            "jack-custom-addUser-queue",
            "jack-custom-addUser-exchange",
            "jack-custom-addUser-routeKey"
    ),
    /**
     * 删除自定义用户相关的队列等信息
     */
    CUSTOM_USER_DELETE(
            "jack-custom-deleteUser-queue",
            "jack-custom-deleteUser-exchange",
            "jack-custom-deleteUser-routeKey"
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
