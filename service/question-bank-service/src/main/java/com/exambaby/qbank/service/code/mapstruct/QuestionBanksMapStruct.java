 package com.exambaby.qbank.service.code.mapstruct;

 import com.exambaby.qbank.service.code.dto.admin.AdminQuestionBanksDTO;
 import com.exambaby.qbank.service.code.dto.publics.PublicQuestionBanksDTO;
 import com.exambaby.qbank.service.code.entity.QuestionBanks;
 import org.mapstruct.Mapper;

 import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionBanksMapStruct {
    // Admin DTO
    AdminQuestionBanksDTO toAdminDTO(QuestionBanks entity);
    QuestionBanks toEntityFromAdmin(AdminQuestionBanksDTO dto);
    List<AdminQuestionBanksDTO> toAdminDTOList(List<QuestionBanks> entityList);
    List<QuestionBanks> toEntityListFromAdmin(List<AdminQuestionBanksDTO> dtoList);

    // Public DTO

    /**
     * 将实体类转为公共DTO类
     * @param entity 实体类
     * @return 公共DTO类
     */
    PublicQuestionBanksDTO toPublicDTO(QuestionBanks entity);

    /**
     * 将公共DTO类转为实体类
     * @param dto 公共DTO类
     * @return 实体类
     */
    QuestionBanks toEntityFromPublic(PublicQuestionBanksDTO dto);

    /**
     * 将实体类列表转为公共DTO类列表
     * @param entityList 实体类列表
     * @return 公共DTO类列表
     */
    List<PublicQuestionBanksDTO> toPublicDTOList(List<QuestionBanks> entityList);

    /**
     * 将公共DTO类列表转为实体类列表
     * @param dtoList 公共DTO类列表
     * @return 实体类列表
     */
    List<QuestionBanks> toEntityListFromPublic(List<PublicQuestionBanksDTO> dtoList);
}
