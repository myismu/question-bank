package com.questionBank.code.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.questionBank.code.dto.admin.AdminQuestionBanksDTO;
import com.questionBank.code.service.QuestionBanksService;
import com.questionBank.code.mapstruct.QuestionBanksMapStruct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 管理端-题库管理接口
 */
@Tag(name = "题库管理", description = "管理端-题库的增删改查等操作")
@RestController
@RequestMapping("/admin/question-banks")
public class AdminQuestionBanksController {
    private final QuestionBanksService questionBanksService;
    private final QuestionBanksMapStruct questionBanksMapStruct;

    public AdminQuestionBanksController(QuestionBanksService questionBanksService, QuestionBanksMapStruct questionBanksMapStruct) {
        this.questionBanksService = questionBanksService;
        this.questionBanksMapStruct = questionBanksMapStruct;
    }

    /**
     * 新增题库
     * @param dto 题库信息
     * @return 新增后的题库DTO
     */
    @Operation(summary = "新增题库", description = "创建一个新的题库")
    @PostMapping
    public AdminQuestionBanksDTO create(@RequestBody AdminQuestionBanksDTO dto) {
        return questionBanksService.createQuestionBank(dto);
    }

    /**
     * 修改题库
     * @param id 题库ID
     * @param dto 题库信息
     * @return 修改后的题库DTO
     */
    @Operation(summary = "修改题库", description = "根据ID修改题库信息")
    @PutMapping("/{id}")
    public AdminQuestionBanksDTO update(@PathVariable Long id, @RequestBody AdminQuestionBanksDTO dto) {
        dto.setId(id);
        return questionBanksService.updateQuestionBank(dto);
    }

    /**
     * 删除题库
     * @param id 题库ID
     */
    @Operation(summary = "删除题库", description = "根据ID删除题库")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        questionBanksService.deleteQuestionBank(id);
    }

    /**
     * 根据ID获取题库
     * @param id 题库ID
     * @return 题库DTO
     */
    @Operation(summary = "获取题库", description = "根据ID获取题库信息")
    @GetMapping("/{id}")
    public AdminQuestionBanksDTO getById(@PathVariable Long id) {
        return questionBanksService.getQuestionBankById(id);
    }

    /**
     * 分页多条件模糊查询题库
     * @param current 当前页码
     * @param size 每页大小
     * @param name 题库名称（模糊）
     * @param userId 用户ID（可选）
     * @param type 题库类型（可选）
     * @param difficulty 难度（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询题库", description = "分页+多条件模糊查询题库")
    @GetMapping("/page")
    public Page<AdminQuestionBanksDTO> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer difficulty) {
        return questionBanksService.pageQuestionBanks(current, size, name, userId, type, difficulty);
    }

    /**
     * 获取所有题库
     * @return 题库DTO列表
     */
    @Operation(summary = "获取所有题库", description = "获取全部题库列表")
    @GetMapping("/all")
    public List<AdminQuestionBanksDTO> getAll() {
        return questionBanksService.getAllQuestionBanks();
    }
} 