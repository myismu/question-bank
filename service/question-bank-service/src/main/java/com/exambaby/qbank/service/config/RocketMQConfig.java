package com.exambaby.qbank.service.config;

import com.exambaby.common.event.UserRegisterEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * RocketMQ 消息队列配置类
 * 采用 Spring Cloud Stream 函数式编程模型
 */
@Slf4j
@Configuration
public class RocketMQConfig {

    /**
     * 定义消费者函数 (默认监听 question-topic)
     * 函数名 "defaultIn" 对应 application-dev.yml 中的 bindings.defaultIn-in-0
     * @return 消息消费者
     */
    @Bean
    public Consumer<String> defaultIn() {
        return message -> {
            log.info("【RocketMQ】成功接收到本服务的测试消息: {}", message);
        };
    }

    /**
     * 接收来自【用户中心】的注册消息
     * 函数名 "onUserRegister" 对应 application-dev.yml 中的 bindings.onUserRegister-in-0
     */
    @Bean
    public Consumer<UserRegisterEvent> onUserRegister() {
        return event -> {
            log.info("【题库中心】监听到新用户注册，准备初始化数据: {}", event.getUsername());
            log.info("【题库中心】收到用户信息: 用户ID={}, 邮箱={}", event.getUserId(), event.getEmail());
            // 这里可以调用 Service 层代码，执行具体的业务逻辑
        };
    }
}
