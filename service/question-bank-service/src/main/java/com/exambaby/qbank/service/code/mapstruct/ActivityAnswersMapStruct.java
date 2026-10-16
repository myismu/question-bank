package com.exambaby.qbank.service.code.mapstruct;

import com.exambaby.qbank.service.code.dto.publics.PublicActivityAnswersDTO;
import com.exambaby.qbank.service.code.entity.ActivityAnswers;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityAnswersMapStruct {
    ActivityAnswersMapStruct INSTANCE = Mappers.getMapper(ActivityAnswersMapStruct.class);

    ActivityAnswers toEntity(PublicActivityAnswersDTO dto);
    PublicActivityAnswersDTO toDTO(ActivityAnswers entity);
    List<PublicActivityAnswersDTO> toDTOList(List<ActivityAnswers> entities);
    List<ActivityAnswers> toEntityList(List<PublicActivityAnswersDTO> dtos);
} 