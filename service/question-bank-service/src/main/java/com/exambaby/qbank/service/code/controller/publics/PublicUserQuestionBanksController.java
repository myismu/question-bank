package com.exambaby.qbank.service.code.controller.publics;

import com.exambaby.common.core.utils.ThreadLocalContext;
import com.exambaby.qbank.service.code.service.UserQuestionBanksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "公共用户关联题库接口", description = "公共用户关联题库接口")
@RestController
@RequestMapping("/public/userQuestionBanks")
public class PublicUserQuestionBanksController {
    private final UserQuestionBanksService userQuestionBanksService;

    public PublicUserQuestionBanksController(UserQuestionBanksService userQuestionBanksService) {
        this.userQuestionBanksService = userQuestionBanksService;
    }

    @Operation(summary = "新增用户关联题库", description = "接收题库ID，将当前登录用户与该题库进行关联")
    @PostMapping("/addUserQuestionBank")
    public String addUserQuestionBank(@RequestParam("bankId") Long bankId) {
        String userId = ThreadLocalContext.getUserId();
        userQuestionBanksService.addUserQuestionBank(userId, bankId);
        return "新增用户关联题库成功";
    }

    @Operation(summary = "删除用户关联题库", description = "接收题库ID，将当前登录用户与该题库进行解除关联")
    @PostMapping("/deleteUserQuestionBank")
    public String deleteUserQuestionBank(@RequestParam("bankId") Long bankId) {
        String userId = ThreadLocalContext.getUserId();
        userQuestionBanksService.deleteUserQuestionBank(userId, bankId);
        return "删除用户关联题库成功";
    }
}
