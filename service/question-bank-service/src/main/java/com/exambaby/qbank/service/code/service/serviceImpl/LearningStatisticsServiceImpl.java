package com.exambaby.qbank.service.code.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exambaby.qbank.service.code.dto.admin.AdminLearningStatisticsDTO;
import com.exambaby.qbank.service.code.entity.LearningStatistics;
import com.exambaby.qbank.service.code.mapper.LearningStatisticsMapper;
import com.exambaby.qbank.service.code.mapstruct.LearningStatisticsMapStruct;
import com.exambaby.qbank.service.code.service.LearningStatisticsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LearningStatisticsServiceImpl extends ServiceImpl<LearningStatisticsMapper, LearningStatistics> implements LearningStatisticsService {
    private final LearningStatisticsMapper learningStatisticsMapper;
    private final LearningStatisticsMapStruct learningStatisticsMapStruct;

    public LearningStatisticsServiceImpl(LearningStatisticsMapper learningStatisticsMapper, LearningStatisticsMapStruct learningStatisticsMapStruct) {
        this.learningStatisticsMapper = learningStatisticsMapper;
        this.learningStatisticsMapStruct = learningStatisticsMapStruct;
    }

    @Override
    public AdminLearningStatisticsDTO createLearningStatistics(AdminLearningStatisticsDTO dto) {
        LearningStatistics entity = learningStatisticsMapStruct.toEntityFromAdmin(dto);
        save(entity);
        return learningStatisticsMapStruct.toAdminDTO(entity);
    }

    @Override
    public AdminLearningStatisticsDTO updateLearningStatistics(AdminLearningStatisticsDTO dto) {
        LearningStatistics entity = learningStatisticsMapStruct.toEntityFromAdmin(dto);
        updateById(entity);
        return learningStatisticsMapStruct.toAdminDTO(entity);
    }

    @Override
    public void deleteLearningStatistics(Long id) {
        removeById(id);
    }

    @Override
    public AdminLearningStatisticsDTO getLearningStatisticsById(Long id) {
        LearningStatistics entity = getById(id);
        return learningStatisticsMapStruct.toAdminDTO(entity);
    }

    @Override
    public Page<AdminLearningStatisticsDTO> pageLearningStatistics(long current, long size, Long userId, Long bankId) {
        LambdaQueryWrapper<LearningStatistics> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(LearningStatistics::getUserId, userId);
        }
        if (bankId != null) {
            wrapper.eq(LearningStatistics::getBankId, bankId);
        }
        Page<LearningStatistics> page = new Page<>(current, size);
        Page<LearningStatistics> entityPage = learningStatisticsMapper.selectPage(page, wrapper);
        Page<AdminLearningStatisticsDTO> dtoPage = new Page<>(current, size, entityPage.getTotal());
        dtoPage.setRecords(learningStatisticsMapStruct.toAdminDTOList(entityPage.getRecords()));
        return dtoPage;
    }

    @Override
    public List<AdminLearningStatisticsDTO> getAllLearningStatistics() {
        List<LearningStatistics> list = list();
        return learningStatisticsMapStruct.toAdminDTOList(list);
    }
} 