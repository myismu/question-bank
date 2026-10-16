package com.exambaby.qbank.service.code.controller.publics;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.common.core.domain.BusinessException;
import com.exambaby.common.core.utils.ThreadLocalContext;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionBanksDTO;
import com.exambaby.qbank.service.code.service.QuestionBanksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公共题库接口", description = "公关端题库接口")
@RestController
@RequestMapping("/public/questionBank")
public class PublicQuestionBankController {

    private final QuestionBanksService questionBanksService;

    public PublicQuestionBankController(QuestionBanksService questionBanksService) {
        this.questionBanksService = questionBanksService;
    }

    @Operation(summary = "创建题库", description = "根据传入的题库DTO创建新的题库")
    @PostMapping("/publicCreateQuestionBank")
    public String publicCreateQuestionBank(@RequestBody PublicQuestionBanksDTO publicQuestionBanksDTO) {
        String userId = ThreadLocalContext.getUserId();
        publicQuestionBanksDTO.setUserId(Long.valueOf(userId));
        return questionBanksService.publicCreateQuestionBank(publicQuestionBanksDTO);
    }

    @Operation(summary = "删除题库", description = "根据题库ID删除题库")
    @DeleteMapping("/deleteQuestionBankById")
    public boolean deleteQuestionBankById(@RequestParam Long bankId) {
        if (bankId == null) {
            throw new BusinessException("该题库信息为空", 444);
        }
        return questionBanksService.deleteQuestionBankById(bankId);
    }

    @Operation(summary = "获取所有公开题库", description = "获取全部公开题库列表")
    @GetMapping("/getAllPublicQuestionBanks")
    public List<PublicQuestionBanksDTO> getAllPublicQuestionBanks() {
        return questionBanksService.getAllPublicQuestionBanks();
    }

    @Operation(summary = "根据题库ID获取题库信息", description = "根据题库ID获取公开题库的详细信息")
    @GetMapping("/getPublicQuestionBankById")
    public PublicQuestionBanksDTO getPublicQuestionBankById(@RequestParam Long id) {
        return questionBanksService.getPublicQuestionBankById(id);
    }

    @Operation(summary = "根据用户ID获取关联收藏题库列表", description = "根据当前登录用户ID获取其关联的题库列表")
    @GetMapping("/getQuestionBanksByUserId")
    public List<PublicQuestionBanksDTO> getQuestionBanksByUserId() {
        String userId = ThreadLocalContext.getUserId();
        return questionBanksService.getQuestionBanksByUserId(Long.valueOf(userId));
    }

    @Operation(summary = "分页获取题库及所有题目信息", description = "根据题库ID及筛选条件分页获取题库及其所有题目信息")
    @GetMapping("/getQuestionBankByIdAndAllQuestions")
    public Page<PublicQuestionBanksDTO> getQuestionBankByIdAndAllQuestions(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam Long bankId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String analysis,
            @RequestParam(required = false) Integer difficulty
    ) {
        if (bankId == null) {
            throw new BusinessException("该题库信息为空", 444);
        }
        Page<PublicQuestionBanksDTO> page = new Page<>(current, size);
        return questionBanksService.getQuestionBankByIdAndAllQuestions(page, bankId, title, type, analysis, difficulty);
    }

    @Operation(summary = "根据用户ID获取用户创建的题库列表", description = "根据当前登录用户ID获取其创建的题库列表")
    @GetMapping("/getQuestionBanksByCreatorId")
    public List<PublicQuestionBanksDTO> getQuestionBanksByCreatorId() {
        String userId = ThreadLocalContext.getUserId();
        return questionBanksService.getQuestionBanksByCreatorId(Long.valueOf(userId));
    }

    @Operation(summary = "修改题库信息", description = "根据传入的题库DTO修改题库信息")
    @PutMapping("/PublicUpdateQuestionBank")
    public void PublicUpdateQuestionBank(@RequestBody PublicQuestionBanksDTO publicQuestionBanksDTO) {
        questionBanksService.PublicUpdateQuestionBank(publicQuestionBanksDTO);
    }
}
