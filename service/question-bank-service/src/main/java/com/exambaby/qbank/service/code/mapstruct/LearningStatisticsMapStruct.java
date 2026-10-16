package com.exambaby.qbank.service.code.mapstruct;

import com.exambaby.qbank.service.code.dto.admin.AdminLearningStatisticsDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicLearningStatisticsDTO;
import com.exambaby.qbank.service.code.entity.LearningStatistics;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LearningStatisticsMapStruct {
    // Admin DTO
    AdminLearningStatisticsDTO toAdminDTO(LearningStatistics entity);
    LearningStatistics toEntityFromAdmin(AdminLearningStatisticsDTO dto);
    List<AdminLearningStatisticsDTO> toAdminDTOList(List<LearningStatistics> entityList);
    List<LearningStatistics> toEntityListFromAdmin(List<AdminLearningStatisticsDTO> dtoList);

    // Public DTO
    PublicLearningStatisticsDTO toPublicDTO(LearningStatistics entity);
    LearningStatistics toEntityFromPublic(PublicLearningStatisticsDTO dto);
    List<PublicLearningStatisticsDTO> toPublicDTOList(List<LearningStatistics> entityList);
    List<LearningStatistics> toEntityListFromPublic(List<PublicLearningStatisticsDTO> dtoList);
} 