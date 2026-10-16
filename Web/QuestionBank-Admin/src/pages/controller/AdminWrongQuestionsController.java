package com.questionBank.code.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.questionBank.code.dto.admin.AdminWrongQuestionsDTO;
import com.questionBank.code.service.WrongQuestionsService;
import com.questionBank.code.mapstruct.WrongQuestionsMapStruct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 管理端-错题本管理接口
 */
@Tag(name = "错题本管理", description = "管理端-错题本的增删改查等操作")
@RestController
@RequestMapping("/admin/wrong-questions")
public class AdminWrongQuestionsController {
    private final WrongQuestionsService wrongQuestionsService;
    private final WrongQuestionsMapStruct wrongQuestionsMapStruct;

    public AdminWrongQuestionsController(WrongQuestionsService wrongQuestionsService, WrongQuestionsMapStruct wrongQuestionsMapStruct) {
        this.wrongQuestionsService = wrongQuestionsService;
        this.wrongQuestionsMapStruct = wrongQuestionsMapStruct;
    }

    /**
     * 新增错题
     * @param dto 错题信息
     * @return 新增后的错题DTO
     */
    @Operation(summary = "新增错题", description = "创建一个新的错题记录")
    @PostMapping
    public AdminWrongQuestionsDTO create(@RequestBody AdminWrongQuestionsDTO dto) {
        return wrongQuestionsService.createWrongQuestion(dto);
    }

    /**
     * 修改错题
     * @param id 错题ID
     * @param dto 错题信息
     * @return 修改后的错题DTO
     */
    @Operation(summary = "修改错题", description = "根据ID修改错题信息")
    @PutMapping("/{id}")
    public AdminWrongQuestionsDTO update(@PathVariable Long id, @RequestBody AdminWrongQuestionsDTO dto) {
        dto.setId(id);
        return wrongQuestionsService.updateWrongQuestion(dto);
    }

    /**
     * 删除错题
     * @param id 错题ID
     */
    @Operation(summary = "删除错题", description = "根据ID删除错题")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        wrongQuestionsService.deleteWrongQuestion(id);
    }

    /**
     * 根据ID获取错题
     * @param id 错题ID
     * @return 错题DTO
     */
    @Operation(summary = "获取错题", description = "根据ID获取错题信息")
    @GetMapping("/{id}")
    public AdminWrongQuestionsDTO getById(@PathVariable Long id) {
        return wrongQuestionsService.getWrongQuestionById(id);
    }

    /**
     * 分页多条件模糊查询错题
     * @param current 当前页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @param questionId 题目ID（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询错题", description = "分页+多条件模糊查询错题")
    @GetMapping("/page")
    public Page<AdminWrongQuestionsDTO> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long questionId) {
        return wrongQuestionsService.pageWrongQuestions(current, size, userId, questionId);
    }

    /**
     * 获取所有错题
     * @return 错题DTO列表
     */
    @Operation(summary = "获取所有错题", description = "获取全部错题列表")
    @GetMapping("/all")
    public List<AdminWrongQuestionsDTO> getAll() {
        return wrongQuestionsService.getAllWrongQuestions();
    }
} 