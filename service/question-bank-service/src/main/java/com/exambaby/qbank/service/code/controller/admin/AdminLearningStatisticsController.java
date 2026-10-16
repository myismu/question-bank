package com.exambaby.qbank.service.code.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.admin.AdminLearningStatisticsDTO;
import com.exambaby.qbank.service.code.mapstruct.LearningStatisticsMapStruct;
import com.exambaby.qbank.service.code.service.LearningStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-学习统计管理接口
 */
@Tag(name = "学习统计管理", description = "管理端-学习统计的增删改查等操作")
@RestController
@RequestMapping("/admin/learning-statistics")
public class AdminLearningStatisticsController {
    private final LearningStatisticsService learningStatisticsService;
    private final LearningStatisticsMapStruct learningStatisticsMapStruct;

    public AdminLearningStatisticsController(LearningStatisticsService learningStatisticsService, LearningStatisticsMapStruct learningStatisticsMapStruct) {
        this.learningStatisticsService = learningStatisticsService;
        this.learningStatisticsMapStruct = learningStatisticsMapStruct;
    }

    /**
     * 新增学习统计
     * @param dto 学习统计信息
     * @return 新增后的学习统计DTO
     */
    @Operation(summary = "新增学习统计", description = "创建一个新的学习统计记录")
    @PostMapping
    public AdminLearningStatisticsDTO create(@RequestBody AdminLearningStatisticsDTO dto) {
        return learningStatisticsService.createLearningStatistics(dto);
    }

    /**
     * 修改学习统计
     * @param id 学习统计ID
     * @param dto 学习统计信息
     * @return 修改后的学习统计DTO
     */
    @Operation(summary = "修改学习统计", description = "根据ID修改学习统计信息")
    @PutMapping("/{id}")
    public AdminLearningStatisticsDTO update(@PathVariable Long id, @RequestBody AdminLearningStatisticsDTO dto) {
        dto.setId(id);
        return learningStatisticsService.updateLearningStatistics(dto);
    }

    /**
     * 删除学习统计
     * @param id 学习统计ID
     */
    @Operation(summary = "删除学习统计", description = "根据ID删除学习统计记录")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        learningStatisticsService.deleteLearningStatistics(id);
    }

    /**
     * 根据ID获取学习统计
     * @param id 学习统计ID
     * @return 学习统计DTO
     */
    @Operation(summary = "获取学习统计", description = "根据ID获取学习统计信息")
    @GetMapping("/{id}")
    public AdminLearningStatisticsDTO getById(@PathVariable Long id) {
        return learningStatisticsService.getLearningStatisticsById(id);
    }

    /**
     * 分页多条件模糊查询学习统计
     * @param current 当前页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @param bankId 题库ID（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询学习统计", description = "分页+多条件模糊查询学习统计")
    @GetMapping("/page")
    public Page<AdminLearningStatisticsDTO> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bankId) {
        return learningStatisticsService.pageLearningStatistics(current, size, userId, bankId);
    }

    /**
     * 获取所有学习统计
     * @return 学习统计DTO列表
     */
    @Operation(summary = "获取所有学习统计", description = "获取全部学习统计列表")
    @GetMapping("/all")
    public List<AdminLearningStatisticsDTO> getAll() {
        return learningStatisticsService.getAllLearningStatistics();
    }
} 