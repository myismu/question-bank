package com.questionBank.code.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.questionBank.code.dto.admin.AdminExamRecordsDTO;
import com.questionBank.code.service.ExamRecordsService;
import com.questionBank.code.mapstruct.ExamRecordsMapStruct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 管理端-考试记录管理接口
 */
@Tag(name = "考试记录管理", description = "管理端-考试记录的增删改查等操作")
@RestController
@RequestMapping("/admin/exam-records")
public class AdminExamRecordsController {
    private final ExamRecordsService examRecordsService;
    private final ExamRecordsMapStruct examRecordsMapStruct;

    public AdminExamRecordsController(ExamRecordsService examRecordsService, ExamRecordsMapStruct examRecordsMapStruct) {
        this.examRecordsService = examRecordsService;
        this.examRecordsMapStruct = examRecordsMapStruct;
    }

    /**
     * 新增考试记录
     * @param dto 考试记录信息
     * @return 新增后的考试记录DTO
     */
    @Operation(summary = "新增考试记录", description = "创建一个新的考试记录")
    @PostMapping
    public AdminExamRecordsDTO create(@RequestBody AdminExamRecordsDTO dto) {
        return examRecordsService.createExamRecord(dto);
    }

    /**
     * 修改考试记录
     * @param id 考试记录ID
     * @param dto 考试记录信息
     * @return 修改后的考试记录DTO
     */
    @Operation(summary = "修改考试记录", description = "根据ID修改考试记录信息")
    @PutMapping("/{id}")
    public AdminExamRecordsDTO update(@PathVariable Long id, @RequestBody AdminExamRecordsDTO dto) {
        dto.setId(id);
        return examRecordsService.updateExamRecord(dto);
    }

    /**
     * 删除考试记录
     * @param id 考试记录ID
     */
    @Operation(summary = "删除考试记录", description = "根据ID删除考试记录")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        examRecordsService.deleteExamRecord(id);
    }

    /**
     * 根据ID获取考试记录
     * @param id 考试记录ID
     * @return 考试记录DTO
     */
    @Operation(summary = "获取考试记录", description = "根据ID获取考试记录信息")
    @GetMapping("/{id}")
    public AdminExamRecordsDTO getById(@PathVariable Long id) {
        return examRecordsService.getExamRecordById(id);
    }

    /**
     * 分页多条件模糊查询考试记录
     * @param current 当前页码
     * @param size 每页大小
     * @param userId 用户ID（可选）
     * @param bankId 题库ID（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询考试记录", description = "分页+多条件模糊查询考试记录")
    @GetMapping("/page")
    public Page<AdminExamRecordsDTO> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long bankId,
            @RequestParam(required = false) String status) {
        return examRecordsService.pageExamRecords(current, size, userId, bankId, status);
    }

    /**
     * 获取所有考试记录
     * @return 考试记录DTO列表
     */
    @Operation(summary = "获取所有考试记录", description = "获取全部考试记录列表")
    @GetMapping("/all")
    public List<AdminExamRecordsDTO> getAll() {
        return examRecordsService.getAllExamRecords();
    }
} 