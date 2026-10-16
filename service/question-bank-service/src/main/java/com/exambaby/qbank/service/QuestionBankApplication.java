package com.exambaby.qbank.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 题库服务启动类
 * 
 * 功能说明：
 * 题库管理微服务，提供题库、题目、试卷等核心业务功能
 * 
 * 主要功能：
 * - 题库管理（创建、编辑、删除、查询）
 * - 题目管理（单选、多选、判断、填空等题型）
 * - 试卷管理（组卷、发布、批改）
 * - 公开题库浏览
 * 
 * 注解说明：
 * @MapperScan - 扫描 MyBatis Mapper 接口
 * @SpringBootApplication - Spring Boot 启动类注解
 * @EnableDiscoveryClient - 启用 Nacos 服务发现
 * @EnableFeignClients - 启用 OpenFeign 远程调用
 * @EnableTransactionManagement - 启用事务管理
 * @EnableScheduling - 启用定时任务
 */
@MapperScan("com.exambaby.qbank.service.*.mapper")
@SpringBootApplication(scanBasePackages = {"com.exambaby.qbank.service", "com.exambaby.common", "com.exambaby.api"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.exambaby.api")
@EnableTransactionManagement
@EnableScheduling
public class QuestionBankApplication {
    
    /**
     * 应用程序入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(QuestionBankApplication.class, args);

    }

}
