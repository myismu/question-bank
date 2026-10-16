package com.exambaby.qbank.service.code.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.admin.AdminActivityAnswersDTO;
import com.exambaby.qbank.service.code.service.ActivityAnswersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-活动答题记录管理接口
 */
@Tag(name = "活动答题记录管理", description = "管理端-活动答题记录的增删改查等操作")
@RestController
@RequestMapping("/admin/activity-answers")
public class AdminActivityAnswersController {
    private final ActivityAnswersService activityAnswersService;

    public AdminActivityAnswersController(ActivityAnswersService activityAnswersService) {
        this.activityAnswersService = activityAnswersService;
    }

    /**
     * 新增活动答题记录
     * @param dto 答题记录信息
     * @return 新增后的答题记录DTO
     */
    @Operation(summary = "新增活动答题记录", description = "创建一个新的活动答题记录")
    @PostMapping
    public AdminActivityAnswersDTO create(@RequestBody AdminActivityAnswersDTO dto) {
        return activityAnswersService.createAdminActivityAnswers(dto);
    }

    /**
     * 修改活动答题记录
     * @param id 答题记录ID
     * @param dto 答题记录信息
     * @return 修改后的答题记录DTO
     */
    @Operation(summary = "修改活动答题记录", description = "根据ID修改活动答题记录信息")
    @PutMapping("/{id}")
    public AdminActivityAnswersDTO update(@PathVariable Long id, @RequestBody AdminActivityAnswersDTO dto) {
        dto.setId(id);
        return activityAnswersService.updateAdminActivityAnswers(dto);
    }

    /**
     * 删除活动答题记录
     * @param id 答题记录ID
     */
    @Operation(summary = "删除活动答题记录", description = "根据ID删除活动答题记录")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        activityAnswersService.deleteAdminActivityAnswers(id);
    }

    /**
     * 根据ID获取活动答题记录
     * @param id 答题记录ID
     * @return 答题记录DTO
     */
    @Operation(summary = "获取活动答题记录", description = "根据ID获取活动答题记录信息")
    @GetMapping("/{id}")
    public AdminActivityAnswersDTO getById(@PathVariable Long id) {
        return activityAnswersService.getAdminActivityAnswersById(id);
    }

    /**
     * 分页查询活动答题记录
     * @param current 当前页码
     * @param size 每页大小
     * @param recordId 活动记录ID
     * @param questionId 题目ID
     * @return 分页结果
     */
    @Operation(summary = "分页查询活动答题记录", description = "分页多条件查询活动答题记录")
    @GetMapping("/page")
    public Page<AdminActivityAnswersDTO> page(@RequestParam(defaultValue = "1") long current,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) String recordId,
                                              @RequestParam(required = false) Long questionId) {
        return activityAnswersService.pageAdminActivityAnswers(current, size, recordId, questionId);
    }

    /**
     * 获取所有活动答题记录
     * @return 所有活动答题记录列表
     */
    @Operation(summary = "获取所有活动答题记录", description = "获取所有活动答题记录信息")
    @GetMapping
    public List<AdminActivityAnswersDTO> getAll() {
        return activityAnswersService.getAllAdminActivityAnswers();
    }
}
