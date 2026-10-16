package com.exambaby.qbank.service.code.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.admin.AdminGlobalLogDTO;
import com.exambaby.qbank.service.code.mapstruct.GlobalLogMapStruct;
import com.exambaby.qbank.service.code.service.GlobalLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-全局日志管理接口
 */
@Tag(name = "全局日志管理", description = "管理端-全局日志的增删改查等操作")
@RestController
@RequestMapping("/admin/global-log")
public class AdminGlobalLogController {
    private final GlobalLogService globalLogService;
    private final GlobalLogMapStruct globalLogMapStruct;

    public AdminGlobalLogController(GlobalLogService globalLogService, GlobalLogMapStruct globalLogMapStruct) {
        this.globalLogService = globalLogService;
        this.globalLogMapStruct = globalLogMapStruct;
    }

    /**
     * 新增全局日志
     * @param dto 日志信息
     * @return 新增后的日志DTO
     */
    @Operation(summary = "新增全局日志", description = "创建一条新的全局日志记录")
    @PostMapping
    public AdminGlobalLogDTO create(@RequestBody AdminGlobalLogDTO dto) {
        return globalLogService.createGlobalLog(dto);
    }

    /**
     * 修改全局日志
     * @param id 日志ID
     * @param dto 日志信息
     * @return 修改后的日志DTO
     */
    @Operation(summary = "修改全局日志", description = "根据ID修改全局日志信息")
    @PutMapping("/{id}")
    public AdminGlobalLogDTO update(@PathVariable Long id, @RequestBody AdminGlobalLogDTO dto) {
        dto.setId(id);
        return globalLogService.updateGlobalLog(dto);
    }

    /**
     * 删除全局日志
     * @param id 日志ID
     */
    @Operation(summary = "删除全局日志", description = "根据ID删除全局日志记录")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        globalLogService.deleteGlobalLog(id);
    }

    /**
     * 根据ID获取全局日志
     * @param id 日志ID
     * @return 日志DTO
     */
    @Operation(summary = "获取全局日志", description = "根据ID获取全局日志信息")
    @GetMapping("/{id}")
    public AdminGlobalLogDTO getById(@PathVariable Long id) {
        return globalLogService.getGlobalLogById(id);
    }

    /**
     * 分页多条件模糊查询全局日志
     * @param current 当前页码
     * @param size 每页大小
     * @param requestUrl 请求URL（可选，模糊）
     * @param userName 用户名（可选，模糊）
     * @param responseStatusCode 响应状态码（可选，精确）
     * @return 分页结果
     */
    @Operation(summary = "分页查询全局日志", description = "分页+多条件模糊查询全局日志")
    @GetMapping("/page")
    public Page<AdminGlobalLogDTO> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String requestUrl,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Integer responseStatusCode) {
        Page<AdminGlobalLogDTO> page = new Page<>(current, size);
        return globalLogService.pageGlobalLogs(page, requestUrl, userName, responseStatusCode);
    }

    /**
     * 获取所有全局日志
     * @return 日志DTO列表
     */
    @Operation(summary = "获取所有全局日志", description = "获取全部全局日志列表")
    @GetMapping("/all")
    public List<AdminGlobalLogDTO> getAll() {
        return globalLogService.getAllGlobalLogs();
    }
} 