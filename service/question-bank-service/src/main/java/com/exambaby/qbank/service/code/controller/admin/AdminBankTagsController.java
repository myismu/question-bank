package com.exambaby.qbank.service.code.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.admin.AdminBankTagsDTO;
import com.exambaby.qbank.service.code.service.BankTagsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-题库标签管理接口
 */
@Tag(name = "题库标签管理", description = "管理端-题库标签的增删改查等操作")
@RestController
@RequestMapping("/admin/bank-tags")

public class AdminBankTagsController {
    private final BankTagsService bankTagsService;

    public AdminBankTagsController(BankTagsService bankTagsService) {
        this.bankTagsService = bankTagsService;
    }

    /**
     * 新增题库标签
     *
     * @param dto 标签信息
     * @return 新增后的标签DTO
     */
    @Operation(summary = "新增题库标签", description = "创建一个新的题库标签")
    @PostMapping
    public AdminBankTagsDTO create(@RequestBody AdminBankTagsDTO dto) {
        return bankTagsService.createBankTag(dto);
    }

    /**
     * 修改题库标签
     *
     * @param id  标签ID
     * @param dto 标签信息
     * @return 修改后的标签DTO
     */
    @Operation(summary = "修改题库标签", description = "根据ID修改题库标签信息")
    @PutMapping("/{id}")
    public AdminBankTagsDTO update(@PathVariable Long id, @RequestBody AdminBankTagsDTO dto) {
        dto.setId(id);
        return bankTagsService.updateBankTag(dto);
    }

    /**
     * 删除题库标签
     *
     * @param id 标签ID
     */
    @Operation(summary = "删除题库标签", description = "根据ID删除题库标签")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        bankTagsService.deleteBankTag(id);
    }

    /**
     * 根据ID获取题库标签
     *
     * @param id 标签ID
     * @return 标签DTO
     */
    @Operation(summary = "获取题库标签", description = "根据ID获取题库标签信息")
    @GetMapping("/{id}")
    public AdminBankTagsDTO getById(@PathVariable Long id) {
        return bankTagsService.getBankTagById(id);
    }

    /**
     * 分页多条件模糊查询题库标签
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param tagName 标签名称（模糊）
     * @param bankId  题库ID（可选）
     * @return 分页结果
     */
    @Operation(summary = "分页查询题库标签", description = "分页+多条件模糊查询题库标签")
    @GetMapping("/page")
    public Page<AdminBankTagsDTO> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String tagName,
            @RequestParam(required = false) Long bankId) {
        return bankTagsService.pageBankTags(current, size, tagName, bankId);
    }

    /**
     * 获取所有题库标签
     *
     * @return 标签DTO列表
     */
    @Operation(summary = "获取所有题库标签", description = "获取全部题库标签列表")
    @GetMapping("/all")
    public List<AdminBankTagsDTO> getAll() {
        return bankTagsService.getAllBankTags();
    }
} 