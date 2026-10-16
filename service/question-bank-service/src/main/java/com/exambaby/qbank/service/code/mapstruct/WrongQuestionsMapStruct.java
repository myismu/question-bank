 package com.exambaby.qbank.service.code.mapstruct;

 import com.exambaby.qbank.service.code.dto.admin.AdminWrongQuestionsDTO;
 import com.exambaby.qbank.service.code.dto.publics.PublicWrongQuestionsDTO;
 import com.exambaby.qbank.service.code.entity.WrongQuestions;
 import org.mapstruct.Mapper;

 import java.util.List;

@Mapper(componentModel = "spring")
public interface WrongQuestionsMapStruct {
    // Admin DTO
    AdminWrongQuestionsDTO toAdminDTO(WrongQuestions entity);
    WrongQuestions toEntityFromAdmin(AdminWrongQuestionsDTO dto);
    List<AdminWrongQuestionsDTO> toAdminDTOList(List<WrongQuestions> entityList);
    List<WrongQuestions> toEntityListFromAdmin(List<AdminWrongQuestionsDTO> dtoList);

    // Public DTO
    PublicWrongQuestionsDTO toPublicDTO(WrongQuestions entity);
    WrongQuestions toEntityFromPublic(PublicWrongQuestionsDTO dto);
    List<PublicWrongQuestionsDTO> toPublicDTOList(List<WrongQuestions> entityList);
    List<WrongQuestions> toEntityListFromPublic(List<PublicWrongQuestionsDTO> dtoList);
}
