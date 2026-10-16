package com.exambaby.qbank.service.code.mapstruct;

import com.exambaby.qbank.service.code.dto.admin.AdminQuestionChaptersDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionChaptersDTO;
import com.exambaby.qbank.service.code.entity.QuestionChapters;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionChaptersMapStruct {
    QuestionChaptersMapStruct INSTANCE = Mappers.getMapper(QuestionChaptersMapStruct.class);

    // 实体转DTO
    PublicQuestionChaptersDTO toDto(QuestionChapters entity);

    // DTO转实体
    QuestionChapters toEntity(PublicQuestionChaptersDTO dto);

    // 实体List转DTO List
    List<PublicQuestionChaptersDTO> toDtoList(List<QuestionChapters> entityList);

    // DTO List转实体List
    List<QuestionChapters> toEntityList(List<PublicQuestionChaptersDTO> dtoList);


    // 实体转DTO
    AdminQuestionChaptersDTO adminToDto(QuestionChapters entity);

    // DTO转实体
    QuestionChapters toEntity(AdminQuestionChaptersDTO dto);

    // 实体List转DTO List
    List<AdminQuestionChaptersDTO> adminToDtoList(List<QuestionChapters> entityList);

    // DTO List转实体List
    List<QuestionChapters> adminToEntityList(List<AdminQuestionChaptersDTO> dtoList);
}
