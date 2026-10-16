package com.exambaby.qbank.service.code.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.admin.AdminQuestionChaptersDTO;
import com.exambaby.qbank.service.code.service.QuestionChaptersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-章节管理接口
 */
@Tag(name = "章节管理", description = "管理端-章节的增删改查等操作")
@RestController
@RequestMapping("/admin/question-chapters")
public class AdminQuestionChaptersController {
    private final QuestionChaptersService questionChaptersService;

    public AdminQuestionChaptersController(QuestionChaptersService questionChaptersService) {
        this.questionChaptersService = questionChaptersService;
    }

    /**
     * 新增章节
     *
     * @param dto 章节信息
     * @return 新增后的章节DTO
     */
    @Operation(summary = "新增章节", description = "创建一个新的章节")
    @PostMapping
    public AdminQuestionChaptersDTO create(@RequestBody AdminQuestionChaptersDTO dto) {
        return questionChaptersService.createAdminQuestionChapters(dto);
    }

    /**
     * 修改章节
     *
     * @param id  章节ID
     * @param dto 章节信息
     * @return 修改后的章节DTO
     */
    @Operation(summary = "修改章节", description = "根据ID修改章节信息")
    @PutMapping("/{id}")
    public AdminQuestionChaptersDTO update(@PathVariable Long id, @RequestBody AdminQuestionChaptersDTO dto) {
        dto.setId(id);
        return questionChaptersService.updateAdminQuestionChapters(dto);
    }

    /**
     * 删除章节
     *
     * @param id 章节ID
     */
    @Operation(summary = "删除章节", description = "根据ID删除章节")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        questionChaptersService.deleteAdminQuestionChapters(id);
    }

    /**
     * 根据ID获取章节
     *
     * @param id 章节ID
     * @return 章节DTO
     */
    @Operation(summary = "获取章节", description = "根据ID获取章节信息")
    @GetMapping("/{id}")
    public AdminQuestionChaptersDTO getById(@PathVariable Long id) {
        return questionChaptersService.getAdminQuestionChaptersById(id);
    }

    /**
     * 分页多条件模糊查询章节
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param title   章节标题（模糊）
     * @param bankId  题库ID（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询章节", description = "分页+多条件模糊查询章节")
    @GetMapping("/page")
    public Page<AdminQuestionChaptersDTO> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long bankId) {
        return questionChaptersService.pageAdminQuestionChapters(current, size, title, bankId);
    }

    /**
     * 获取所有章节
     *
     * @return 章节DTO列表
     */
    @Operation(summary = "获取所有章节", description = "获取全部章节列表")
    @GetMapping("/all")
    public List<AdminQuestionChaptersDTO> getAll() {
        return questionChaptersService.getAllAdminQuestionChapters();
    }
}
