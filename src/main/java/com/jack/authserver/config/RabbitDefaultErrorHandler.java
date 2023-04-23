package com.jack.authserver.config;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.AmqpHeaders;

@Slf4j
public class RabbitDefaultErrorHandler implements RabbitListenerErrorHandler {

    @Override
    public Object handleError(Message amqpMessage,
                              org.springframework.messaging.Message<?> message,
                              ListenerExecutionFailedException exception) throws Exception {
        log.error(exception.getMessage(), exception);

        // 拒绝消息, 并且设置不再requeue, 如果消息所在的队列有设置死信队列参数, 就转发到死信队列. 否则丢弃消息.
        message.getHeaders().get(AmqpHeaders.CHANNEL, Channel.class)
                .basicReject(message.getHeaders().get(AmqpHeaders.DELIVERY_TAG, Long.class), false);

        // 返回null, 不需要SendTo应答队列
        return null;
    }
}
