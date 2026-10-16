package com.questionBank.code.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.questionBank.code.dto.admin.AdminPracticeAnswersDTO;
import com.questionBank.code.service.PracticeAnswersService;
import com.questionBank.code.mapstruct.PracticeAnswersMapStruct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 管理端-练习答题记录管理接口
 */
@Tag(name = "练习答题记录管理", description = "管理端-练习答题记录的增删改查等操作")
@RestController
@RequestMapping("/admin/practice-answers")
public class AdminPracticeAnswersController {
    private final PracticeAnswersService practiceAnswersService;
    private final PracticeAnswersMapStruct practiceAnswersMapStruct;

    public AdminPracticeAnswersController(PracticeAnswersService practiceAnswersService, PracticeAnswersMapStruct practiceAnswersMapStruct) {
        this.practiceAnswersService = practiceAnswersService;
        this.practiceAnswersMapStruct = practiceAnswersMapStruct;
    }

    /**
     * 新增练习答题记录
     * @param dto 答题记录信息
     * @return 新增后的答题记录DTO
     */
    @Operation(summary = "新增练习答题记录", description = "创建一个新的练习答题记录")
    @PostMapping
    public AdminPracticeAnswersDTO create(@RequestBody AdminPracticeAnswersDTO dto) {
        return practiceAnswersService.createPracticeAnswer(dto);
    }

    /**
     * 修改练习答题记录
     * @param id 答题记录ID
     * @param dto 答题记录信息
     * @return 修改后的答题记录DTO
     */
    @Operation(summary = "修改练习答题记录", description = "根据ID修改练习答题记录信息")
    @PutMapping("/{id}")
    public AdminPracticeAnswersDTO update(@PathVariable Long id, @RequestBody AdminPracticeAnswersDTO dto) {
        dto.setId(id);
        return practiceAnswersService.updatePracticeAnswer(dto);
    }

    /**
     * 删除练习答题记录
     * @param id 答题记录ID
     */
    @Operation(summary = "删除练习答题记录", description = "根据ID删除练习答题记录")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        practiceAnswersService.deletePracticeAnswer(id);
    }

    /**
     * 根据ID获取练习答题记录
     * @param id 答题记录ID
     * @return 答题记录DTO
     */
    @Operation(summary = "获取练习答题记录", description = "根据ID获取练习答题记录信息")
    @GetMapping("/{id}")
    public AdminPracticeAnswersDTO getById(@PathVariable Long id) {
        return practiceAnswersService.getPracticeAnswerById(id);
    }

    /**
     * 分页多条件模糊查询练习答题记录
     * @param current 当前页码
     * @param size 每页大小
     * @param practiceId 练习记录ID（可选）
     * @param questionId 题目ID（可选）
     * @param isCorrect 是否正确（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询练习答题记录", description = "分页+多条件模糊查询练习答题记录")
    @GetMapping("/page")
    public Page<AdminPracticeAnswersDTO> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long practiceId,
            @RequestParam(required = false) Long questionId,
            @RequestParam(required = false) Integer isCorrect) {
        return practiceAnswersService.pagePracticeAnswers(current, size, practiceId, questionId, isCorrect);
    }

    /**
     * 获取所有练习答题记录
     * @return 答题记录DTO列表
     */
    @Operation(summary = "获取所有练习答题记录", description = "获取全部练习答题记录列表")
    @GetMapping("/all")
    public List<AdminPracticeAnswersDTO> getAll() {
        return practiceAnswersService.getAllPracticeAnswers();
    }
} 