package com.exambaby.qbank.service.code.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.admin.AdminActivityRecordsDTO;
import com.exambaby.qbank.service.code.service.ActivityRecordsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-活动记录管理接口
 */
@Tag(name = "活动记录管理", description = "管理端-活动记录的增删改查等操作")
@RestController
@RequestMapping("/admin/activity-records")
public class AdminActivityRecordsController {
    private final ActivityRecordsService activityRecordsService;

    public AdminActivityRecordsController(ActivityRecordsService activityRecordsService) {
        this.activityRecordsService = activityRecordsService;
    }

    /**
     * 新增活动记录
     * @param dto 活动记录信息
     * @return 新增后的活动记录DTO
     */
    @Operation(summary = "新增活动记录", description = "创建一个新的活动记录")
    @PostMapping
    public AdminActivityRecordsDTO create(@RequestBody AdminActivityRecordsDTO dto) {
        return activityRecordsService.createAdminActivityRecords(dto);
    }

    /**
     * 修改活动记录
     * @param id 活动记录ID
     * @param dto 活动记录信息
     * @return 修改后的活动记录DTO
     */
    @Operation(summary = "修改活动记录", description = "根据ID修改活动记录信息")
    @PutMapping("/{id}")
    public AdminActivityRecordsDTO update(@PathVariable Long id, @RequestBody AdminActivityRecordsDTO dto) {
        dto.setId(id);
        return activityRecordsService.updateAdminActivityRecords(dto);
    }

    /**
     * 删除活动记录
     * @param id 活动记录ID
     */
    @Operation(summary = "删除活动记录", description = "根据ID删除活动记录")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        activityRecordsService.deleteAdminActivityRecords(id);
    }

    /**
     * 根据ID获取活动记录
     * @param id 活动记录ID
     * @return 活动记录DTO
     */
    @Operation(summary = "获取活动记录", description = "根据ID获取活动记录信息")
    @GetMapping("/{id}")
    public AdminActivityRecordsDTO getById(@PathVariable Long id) {
        return activityRecordsService.getAdminActivityRecordsById(id);
    }

    /**
     * 分页查询活动记录
     * @param current 当前页码
     * @param size 每页大小
     * @param userId 用户ID
     * @param bankId 题库ID
     * @param type 活动类型
     * @return 分页结果
     */
    @Operation(summary = "分页查询活动记录", description = "分页多条件查询活动记录")
    @GetMapping("/page")
    public Page<AdminActivityRecordsDTO> page(@RequestParam(defaultValue = "1") long current,
                                              @RequestParam(defaultValue = "10") long size,
                                              @RequestParam(required = false) String userId,
                                              @RequestParam(required = false) Long bankId,
                                              @RequestParam(required = false) String type) {
        return activityRecordsService.pageAdminActivityRecords(current, size, userId, bankId, type);
    }

    /**
     * 获取所有活动记录
     * @return 所有活动记录列表
     */
    @Operation(summary = "获取所有活动记录", description = "获取所有活动记录信息")
    @GetMapping
    public List<AdminActivityRecordsDTO> getAll() {
        return activityRecordsService.getAllAdminActivityRecords();
    }
}
