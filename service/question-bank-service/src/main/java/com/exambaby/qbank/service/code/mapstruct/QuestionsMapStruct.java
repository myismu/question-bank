 package com.exambaby.qbank.service.code.mapstruct;

 import com.exambaby.qbank.service.code.dto.admin.AdminQuestionsDTO;
 import com.exambaby.qbank.service.code.dto.publics.PublicQuestionsDTO;
 import com.exambaby.qbank.service.code.entity.Questions;
 import org.mapstruct.Mapper;

 import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionsMapStruct {
    // Admin DTO
    AdminQuestionsDTO toAdminDTO(Questions entity);
    Questions toEntityFromAdmin(AdminQuestionsDTO dto);
    List<AdminQuestionsDTO> toAdminDTOList(List<Questions> entityList);
    List<Questions> toEntityListFromAdmin(List<AdminQuestionsDTO> dtoList);

    // Public DTO
    PublicQuestionsDTO toPublicDTO(Questions entity);
    Questions toEntityFromPublic(PublicQuestionsDTO dto);

    /**
     * 将实体类列表转为公共DTO列表
     * @param entityList 实体类列表
     * @return 公共DTO列表
     */
    List<PublicQuestionsDTO> toPublicDTOList(List<Questions> entityList);
    List<Questions> toEntityListFromPublic(List<PublicQuestionsDTO> dtoList);
}
