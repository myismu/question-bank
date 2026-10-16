package com.exambaby.qbank.service.code.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exambaby.qbank.service.code.dto.admin.AdminBankTagsDTO;
import com.exambaby.qbank.service.code.entity.BankTags;
import com.exambaby.qbank.service.code.mapper.BankTagsMapper;
import com.exambaby.qbank.service.code.mapstruct.BankTagsMapStruct;
import com.exambaby.qbank.service.code.service.BankTagsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class BankTagsServiceImpl extends ServiceImpl<BankTagsMapper, BankTags> implements BankTagsService {
    private final BankTagsMapper bankTagsMapper;
    private final BankTagsMapStruct bankTagsMapStruct;

    public BankTagsServiceImpl(BankTagsMapper bankTagsMapper, BankTagsMapStruct bankTagsMapStruct) {
        this.bankTagsMapper = bankTagsMapper;
        this.bankTagsMapStruct = bankTagsMapStruct;
    }

    @Override
    public AdminBankTagsDTO createBankTag(AdminBankTagsDTO dto) {
        BankTags entity = bankTagsMapStruct.toEntityFromAdmin(dto);
        save(entity);
        return bankTagsMapStruct.toAdminDTO(entity);
    }

    @Override
    public AdminBankTagsDTO updateBankTag(AdminBankTagsDTO dto) {
        BankTags entity = bankTagsMapStruct.toEntityFromAdmin(dto);
        updateById(entity);
        return bankTagsMapStruct.toAdminDTO(entity);
    }

    @Override
    public void deleteBankTag(Long id) {
        removeById(id);
    }

    @Override
    public AdminBankTagsDTO getBankTagById(Long id) {
        BankTags entity = getById(id);
        return bankTagsMapStruct.toAdminDTO(entity);
    }

    @Override
    public Page<AdminBankTagsDTO> pageBankTags(long current, long size, String tagName, Long bankId) {
        LambdaQueryWrapper<BankTags> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(tagName)) {
            wrapper.like(BankTags::getTagName, tagName);
        }
        if (bankId != null) {
            wrapper.eq(BankTags::getBankId, bankId);
        }
        Page<BankTags> page = new Page<>(current, size);
        Page<BankTags> entityPage = bankTagsMapper.selectPage(page, wrapper);
        Page<AdminBankTagsDTO> dtoPage = new Page<>(current, size, entityPage.getTotal());
        dtoPage.setRecords(bankTagsMapStruct.toAdminDTOList(entityPage.getRecords()));
        return dtoPage;
    }

    @Override
    public List<AdminBankTagsDTO> getAllBankTags() {
        List<BankTags> list = list();
        return bankTagsMapStruct.toAdminDTOList(list);
    }
} 