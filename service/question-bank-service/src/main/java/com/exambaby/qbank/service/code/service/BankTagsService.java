package com.exambaby.qbank.service.code.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.admin.AdminBankTagsDTO;

import java.util.List;

public interface BankTagsService {
    AdminBankTagsDTO createBankTag(AdminBankTagsDTO dto);
    AdminBankTagsDTO updateBankTag(AdminBankTagsDTO dto);
    void deleteBankTag(Long id);
    AdminBankTagsDTO getBankTagById(Long id);
    Page<AdminBankTagsDTO> pageBankTags(long current, long size, String tagName, Long bankId);
    List<AdminBankTagsDTO> getAllBankTags();
} 