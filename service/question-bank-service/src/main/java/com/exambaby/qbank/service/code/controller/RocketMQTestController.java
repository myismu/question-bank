package com.exambaby.qbank.service.code.controller;

import com.exambaby.qbank.service.code.component.RocketMQProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RocketMQ 测试接口
 */
@Tag(name = "RocketMQ 测试")
@RestController
@RequestMapping("/mq")
public class RocketMQTestController {

    @Autowired
    private RocketMQProducer rocketMQProducer;

    @Operation(summary = "发送测试消息")
    @GetMapping("/send")
    public String send(@RequestParam String msg) {
        rocketMQProducer.sendMessage(msg);
        return "消息已发送: " + msg;
    }
}
