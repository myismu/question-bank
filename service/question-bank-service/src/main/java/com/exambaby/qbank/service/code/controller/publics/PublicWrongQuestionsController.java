package com.exambaby.qbank.service.code.controller.publics;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.common.core.utils.ThreadLocalContext;
import com.exambaby.qbank.service.code.dto.BankSimpleInfoDTO;
import com.exambaby.qbank.service.code.dto.WrongQuestionStatsVO;
import com.exambaby.qbank.service.code.dto.publics.PublicWrongQuestionsDTO;
import com.exambaby.qbank.service.code.service.WrongQuestionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "公共-错题集", description = "公共-错题集")
@RestController
@RequestMapping("/public/wrongQuestions")
public class PublicWrongQuestionsController {
    private final WrongQuestionsService wrongQuestionsService;

    public PublicWrongQuestionsController(WrongQuestionsService wrongQuestionsService) {
        this.wrongQuestionsService = wrongQuestionsService;
    }

    @Operation(summary = "获取错题列表", description = "获取错题列表，参数：分页参数，题库id，题目类型，题目状态，返回：分页错题列表")
    @RequestMapping("/getPageWrongQuestionsList")
    public Page<PublicWrongQuestionsDTO> getPageWrongQuestionsList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                                   @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                                                   @RequestParam(value = "bankId", required = false) Long bankId,
                                                                   @RequestParam(value = "type", required = false) String type,
                                                                   @RequestParam(value = "status", required = false) String status) {
        String userId = ThreadLocalContext.getUserId();
        return wrongQuestionsService.getWrongQuestionsPage(pageNum, pageSize, Long.valueOf(userId), bankId, status, type);
    }

    @Operation(summary = "获取错题统计信息", description = "获取错题统计信息，参数：用户id，返回：错题统计信息")
    @RequestMapping("/getWrongQuestionStats")
    public WrongQuestionStatsVO getWrongQuestionStats() {
        String userId = ThreadLocalContext.getUserId();
        return wrongQuestionsService.getWrongQuestionStats(userId);
    }

    @Operation(summary = "移除错题", description = "移除错题，参数：错题id，返回：成功或失败")
    @PutMapping("/removeWrongQuestion")
    public String removeWrongQuestion(@RequestParam(value = "wrongQuestionId") Long wrongQuestionId) {
        return wrongQuestionsService.removeWrongQuestion(wrongQuestionId) > 0 ? "success" : "failed";
    }

    @Operation(summary = "获取个人的错题题库列表", description = "获取个人的错题题库列表")
    @RequestMapping("/getDistinctBanksByUserId")
    public List<BankSimpleInfoDTO> getDistinctBanksByUserId() {
        String userId = ThreadLocalContext.getUserId();
        return wrongQuestionsService.getDistinctBanksByUserId(Long.valueOf(userId));
    }
}
