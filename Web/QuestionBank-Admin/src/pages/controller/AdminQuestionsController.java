package com.questionBank.code.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.questionBank.code.dto.admin.AdminQuestionsDTO;
import com.questionBank.code.service.QuestionsService;
import com.questionBank.code.mapstruct.QuestionsMapStruct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 管理端-题目管理接口
 */
@Tag(name = "题目管理", description = "管理端-题目的增删改查等操作")
@RestController
@RequestMapping("/admin/questions")
public class AdminQuestionsController {
    private final QuestionsService questionsService;
    private final QuestionsMapStruct questionsMapStruct;

    public AdminQuestionsController(QuestionsService questionsService, QuestionsMapStruct questionsMapStruct) {
        this.questionsService = questionsService;
        this.questionsMapStruct = questionsMapStruct;
    }

    /**
     * 新增题目
     * @param dto 题目信息
     * @return 新增后的题目DTO
     */
    @Operation(summary = "新增题目", description = "创建一个新的题目")
    @PostMapping
    public AdminQuestionsDTO create(@RequestBody AdminQuestionsDTO dto) {
        return questionsService.createQuestion(dto);
    }

    /**
     * 修改题目
     * @param id 题目ID
     * @param dto 题目信息
     * @return 修改后的题目DTO
     */
    @Operation(summary = "修改题目", description = "根据ID修改题目信息")
    @PutMapping("/{id}")
    public AdminQuestionsDTO update(@PathVariable Long id, @RequestBody AdminQuestionsDTO dto) {
        dto.setId(id);
        return questionsService.updateQuestion(dto);
    }

    /**
     * 删除题目
     * @param id 题目ID
     */
    @Operation(summary = "删除题目", description = "根据ID删除题目")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        questionsService.deleteQuestion(id);
    }

    /**
     * 根据ID获取题目
     * @param id 题目ID
     * @return 题目DTO
     */
    @Operation(summary = "获取题目", description = "根据ID获取题目信息")
    @GetMapping("/{id}")
    public AdminQuestionsDTO getById(@PathVariable Long id) {
        return questionsService.getQuestionById(id);
    }

    /**
     * 分页多条件模糊查询题目
     * @param current 当前页码
     * @param size 每页大小
     * @param title 题目标题（模糊）
     * @param bankId 题库ID（可选）
     * @param type 题目类型（可选）
     * @param difficulty 难度（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询题目", description = "分页+多条件模糊查询题目")
    @GetMapping("/page")
    public Page<AdminQuestionsDTO> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long bankId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer difficulty) {
        return questionsService.pageQuestions(current, size, title, bankId, type, difficulty);
    }

    /**
     * 获取所有题目
     * @return 题目DTO列表
     */
    @Operation(summary = "获取所有题目", description = "获取全部题目列表")
    @GetMapping("/all")
    public List<AdminQuestionsDTO> getAll() {
        return questionsService.getAllQuestions();
    }
} 