package com.exambaby.qbank.service.code.controller;

import com.exambaby.common.core.result.Result;
import com.exambaby.qbank.service.code.dto.admin.AdminQuestionBanksDTO;
import com.exambaby.qbank.service.code.service.QuestionBanksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Seata 分布式事务测试 (题库侧) - 修正版
 */
@Slf4j
@RestController
@RequestMapping("/internal/seata/test")
@RequiredArgsConstructor
public class SeataBankTestController {

    private final QuestionBanksService questionBanksService;

    /**
     * 被远程调用的接口：插入一条测试题库
     * 改用 QuestionBanksService 中现有的 createQuestionBank 方法
     */
    @PostMapping("/addBank")
    public Result<Boolean> addBank(@RequestParam Long userId, @RequestParam String bankName) {
        log.info("收到远程调用：用户 {} 新增测试题库 {}", userId, bankName);
        
        AdminQuestionBanksDTO dto = new AdminQuestionBanksDTO();
        dto.setUserId(userId);
        dto.setName(bankName);
        dto.setDescription("Seata 分布式事务测试产生的题库");
        dto.setType("private");
        dto.setDifficulty(3);
        
        try {
            questionBanksService.createQuestionBank(dto);
            log.info("【远程操作】题库保存成功");
            return Result.success(true);
        } catch (Exception e) {
            log.error("【远程操作】题库保存失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
