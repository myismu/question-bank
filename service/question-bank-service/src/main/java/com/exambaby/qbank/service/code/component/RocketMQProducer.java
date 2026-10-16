package com.exambaby.qbank.service.code.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 消息生产者示例
 */
@Slf4j
@Component
public class RocketMQProducer {

    @Autowired
    private StreamBridge streamBridge;

    /**
     * 发送简单消息
     * @param message 消息内容
     */
    public void sendMessage(String message) {
        log.info("【RocketMQ】准备发送消息: {}", message);
        
        // 这里的 "default-out-0" 对应 application-dev.yml 中的 bindings.default-out-0
        boolean isSuccess = streamBridge.send("default-out-0", MessageBuilder.withPayload(message).build());
        
        if (isSuccess) {
            log.info("【RocketMQ】消息发送成功");
        } else {
            log.error("【RocketMQ】消息发送失败");
        }
    }
}
