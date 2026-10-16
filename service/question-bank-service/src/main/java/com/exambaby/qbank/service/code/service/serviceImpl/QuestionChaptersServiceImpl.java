package com.exambaby.qbank.service.code.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exambaby.qbank.service.code.dto.admin.AdminQuestionChaptersDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionChaptersDTO;
import com.exambaby.qbank.service.code.entity.QuestionChapters;
import com.exambaby.qbank.service.code.mapper.QuestionChaptersMapper;
import com.exambaby.qbank.service.code.mapstruct.QuestionChaptersMapStruct;
import com.exambaby.qbank.service.code.service.QuestionChaptersService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionChaptersServiceImpl extends ServiceImpl<QuestionChaptersMapper, QuestionChapters> implements QuestionChaptersService {
    private final QuestionChaptersMapper questionChaptersMapper;
    private final QuestionChaptersMapStruct questionChaptersMapStruct;
    private final QuestionChaptersMapStruct adminQuestionChaptersMapStruct;

    public QuestionChaptersServiceImpl(QuestionChaptersMapper questionChaptersMapper, 
                                      QuestionChaptersMapStruct questionChaptersMapStruct,
                                       QuestionChaptersMapStruct adminQuestionChaptersMapStruct) {
        this.questionChaptersMapper = questionChaptersMapper;
        this.questionChaptersMapStruct = questionChaptersMapStruct;
        this.adminQuestionChaptersMapStruct = adminQuestionChaptersMapStruct;
    }

    @Override
    public Long createQuestionChapters(PublicQuestionChaptersDTO chaptersDTO) {
        QuestionChapters entity = questionChaptersMapStruct.toEntity(chaptersDTO);
        questionChaptersMapper.insert(entity);
//        返回id
        return entity.getId();
    }

    @Override
    public void deleteByBankId(Long bankId) {
        LambdaQueryWrapper<QuestionChapters> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionChapters::getBankId, bankId);
        this.remove(wrapper);
    }

    @Override
    public AdminQuestionChaptersDTO createAdminQuestionChapters(AdminQuestionChaptersDTO dto) {
        QuestionChapters entity = adminQuestionChaptersMapStruct.toEntity(dto);
        questionChaptersMapper.insert(entity);
        return adminQuestionChaptersMapStruct.adminToDto(entity);
    }

    @Override
    public AdminQuestionChaptersDTO updateAdminQuestionChapters(AdminQuestionChaptersDTO dto) {
        QuestionChapters entity = adminQuestionChaptersMapStruct.toEntity(dto);
        questionChaptersMapper.updateById(entity);
        return adminQuestionChaptersMapStruct.adminToDto(entity);
    }

    @Override
    public void deleteAdminQuestionChapters(Long id) {
        questionChaptersMapper.deleteById(id);
    }

    @Override
    public AdminQuestionChaptersDTO getAdminQuestionChaptersById(Long id) {
        QuestionChapters entity = questionChaptersMapper.selectById(id);
        return adminQuestionChaptersMapStruct.adminToDto(entity);
    }

    @Override
    public Page<AdminQuestionChaptersDTO> pageAdminQuestionChapters(long current, long size, String title, Long bankId) {
        LambdaQueryWrapper<QuestionChapters> wrapper = new LambdaQueryWrapper<>();
        
        if (title != null && !title.trim().isEmpty()) {
            wrapper.like(QuestionChapters::getTitle, title);
        }
        
        if (bankId != null) {
            wrapper.eq(QuestionChapters::getBankId, bankId);
        }
        
        wrapper.orderByAsc(QuestionChapters::getSortOrder);
        
        Page<QuestionChapters> entityPage = questionChaptersMapper.selectPage(new Page<>(current, size), wrapper);
        
        Page<AdminQuestionChaptersDTO> dtoPage = new Page<>(current, size);
        dtoPage.setTotal(entityPage.getTotal());
        dtoPage.setRecords(adminQuestionChaptersMapStruct.adminToDtoList(entityPage.getRecords()));
        
        return dtoPage;
    }

    @Override
    public List<AdminQuestionChaptersDTO> getAllAdminQuestionChapters() {
        LambdaQueryWrapper<QuestionChapters> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(QuestionChapters::getSortOrder);
        List<QuestionChapters> entities = questionChaptersMapper.selectList(wrapper);
        return adminQuestionChaptersMapStruct.adminToDtoList(entities);
    }
}
