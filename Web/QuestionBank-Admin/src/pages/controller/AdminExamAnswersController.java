package com.questionBank.code.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.questionBank.code.dto.admin.AdminExamAnswersDTO;
import com.questionBank.code.service.ExamAnswersService;
import com.questionBank.code.mapstruct.ExamAnswersMapStruct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 管理端-考试答题记录管理接口
 */
@Tag(name = "考试答题记录管理", description = "管理端-考试答题记录的增删改查等操作")
@RestController
@RequestMapping("/admin/exam-answers")
public class AdminExamAnswersController {
    private final ExamAnswersService examAnswersService;
    private final ExamAnswersMapStruct examAnswersMapStruct;

    public AdminExamAnswersController(ExamAnswersService examAnswersService, ExamAnswersMapStruct examAnswersMapStruct) {
        this.examAnswersService = examAnswersService;
        this.examAnswersMapStruct = examAnswersMapStruct;
    }

    /**
     * 新增考试答题记录
     * @param dto 答题记录信息
     * @return 新增后的答题记录DTO
     */
    @Operation(summary = "新增考试答题记录", description = "创建一个新的考试答题记录")
    @PostMapping
    public AdminExamAnswersDTO create(@RequestBody AdminExamAnswersDTO dto) {
        return examAnswersService.createExamAnswer(dto);
    }

    /**
     * 修改考试答题记录
     * @param id 答题记录ID
     * @param dto 答题记录信息
     * @return 修改后的答题记录DTO
     */
    @Operation(summary = "修改考试答题记录", description = "根据ID修改考试答题记录信息")
    @PutMapping("/{id}")
    public AdminExamAnswersDTO update(@PathVariable Long id, @RequestBody AdminExamAnswersDTO dto) {
        dto.setId(id);
        return examAnswersService.updateExamAnswer(dto);
    }

    /**
     * 删除考试答题记录
     * @param id 答题记录ID
     */
    @Operation(summary = "删除考试答题记录", description = "根据ID删除考试答题记录")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        examAnswersService.deleteExamAnswer(id);
    }

    /**
     * 根据ID获取考试答题记录
     * @param id 答题记录ID
     * @return 答题记录DTO
     */
    @Operation(summary = "获取考试答题记录", description = "根据ID获取考试答题记录信息")
    @GetMapping("/{id}")
    public AdminExamAnswersDTO getById(@PathVariable Long id) {
        return examAnswersService.getExamAnswerById(id);
    }

    /**
     * 分页多条件模糊查询考试答题记录
     * @param current 当前页码
     * @param size 每页大小
     * @param examId 考试记录ID（可选）
     * @param questionId 题目ID（可选）
     * @param isCorrect 是否正确（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询考试答题记录", description = "分页+多条件模糊查询考试答题记录")
    @GetMapping("/page")
    public Page<AdminExamAnswersDTO> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long questionId,
            @RequestParam(required = false) Integer isCorrect) {
        return examAnswersService.pageExamAnswers(current, size, examId, questionId, isCorrect);
    }

    /**
     * 获取所有考试答题记录
     * @return 答题记录DTO列表
     */
    @Operation(summary = "获取所有考试答题记录", description = "获取全部考试答题记录列表")
    @GetMapping("/all")
    public List<AdminExamAnswersDTO> getAll() {
        return examAnswersService.getAllExamAnswers();
    }
} 