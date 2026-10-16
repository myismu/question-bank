package com.exambaby.qbank.service.code.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.admin.AdminLearningStatisticsDTO;

import java.util.List;

public interface LearningStatisticsService {
    AdminLearningStatisticsDTO createLearningStatistics(AdminLearningStatisticsDTO dto);
    AdminLearningStatisticsDTO updateLearningStatistics(AdminLearningStatisticsDTO dto);
    void deleteLearningStatistics(Long id);
    AdminLearningStatisticsDTO getLearningStatisticsById(Long id);
    Page<AdminLearningStatisticsDTO> pageLearningStatistics(long current, long size, Long userId, Long bankId);
    List<AdminLearningStatisticsDTO> getAllLearningStatistics();
} 