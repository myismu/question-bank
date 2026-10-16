package com.questionBank.code.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.questionBank.code.dto.admin.AdminPracticeRecordsDTO;
import com.questionBank.code.service.PracticeRecordsService;
import com.questionBank.code.mapstruct.PracticeRecordsMapStruct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 管理端-练习记录管理接口
 */
@Tag(name = "练习记录管理", description = "管理端-练习记录的增删改查等操作")
@RestController
@RequestMapping("/admin/practice-records")
public class AdminPracticeRecordsController {
    private final PracticeRecordsService practiceRecordsService;
    private final PracticeRecordsMapStruct practiceRecordsMapStruct;

    public AdminPracticeRecordsController(PracticeRecordsService practiceRecordsService, PracticeRecordsMapStruct practiceRecordsMapStruct) {
        this.practiceRecordsService = practiceRecordsService;
        this.practiceRecordsMapStruct = practiceRecordsMapStruct;
    }

    /**
     * 新增练习记录
     * @param dto 练习记录信息
     * @return 新增后的练习记录DTO
     */
    @Operation(summary = "新增练习记录", description = "创建一个新的练习记录")
    @PostMapping
    public AdminPracticeRecordsDTO create(@RequestBody AdminPracticeRecordsDTO dto) {
        return practiceRecordsService.createPracticeRecord(dto);
    }

    /**
     * 修改练习记录
     * @param id 练习记录ID
     * @param dto 练习记录信息
     * @return 修改后的练习记录DTO
     */
    @Operation(summary = "修改练习记录", description = "根据ID修改练习记录信息")
    @PutMapping("/{id}")
    public AdminPracticeRecordsDTO update(@PathVariable Long id, @RequestBody AdminPracticeRecordsDTO dto) {
        dto.setId(id);
        return practiceRecordsService.updatePracticeRecord(dto);
    }

    /**
     * 删除练习记录
     * @param id 练习记录ID
     */
    @Operation(summary = "删除练习记录", description = "根据ID删除练习记录")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        practiceRecordsService.deletePracticeRecord(id);
    }

    /**
     * 根据ID获取练习记录
     * @param id 练习记录ID
     * @return 练习记录DTO
     */
    @Operation(summary = "获取练习记录", description = "根据ID获取练习记录信息")
    @GetMapping("/{id}")
    public AdminPracticeRecordsDTO getById(@PathVariable Long id) {
        return practiceRecordsService.getPracticeRecordById(id);
    }

    /**
     * 分页多条件模糊查询练习记录
     * @param current 当前页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @param bankId 题库ID（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询练习记录", description = "分页+多条件模糊查询练习记录")
    @GetMapping("/page")
    public Page<AdminPracticeRecordsDTO> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bankId,
            @RequestParam(required = false) String status) {
        return practiceRecordsService.pagePracticeRecords(current, size, userId, bankId, status);
    }

    /**
     * 获取所有练习记录
     * @return 练习记录DTO列表
     */
    @Operation(summary = "获取所有练习记录", description = "获取全部练习记录列表")
    @GetMapping("/all")
    public List<AdminPracticeRecordsDTO> getAll() {
        return practiceRecordsService.getAllPracticeRecords();
    }
} 