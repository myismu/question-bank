 package com.exambaby.qbank.service.code.mapstruct;

 import com.exambaby.qbank.service.code.dto.admin.AdminUserQuestionBanksDTO;
 import com.exambaby.qbank.service.code.dto.publics.PublicUserQuestionBanksDTO;
 import com.exambaby.qbank.service.code.entity.UserQuestionBanks;
 import org.mapstruct.Mapper;

 import java.util.List;

@Mapper(componentModel = "spring")
public interface UserQuestionBanksMapStruct {
    // Admin DTO
    AdminUserQuestionBanksDTO toAdminDTO(UserQuestionBanks entity);
    UserQuestionBanks toEntityFromAdmin(AdminUserQuestionBanksDTO dto);
    List<AdminUserQuestionBanksDTO> toAdminDTOList(List<UserQuestionBanks> entityList);
    List<UserQuestionBanks> toEntityListFromAdmin(List<AdminUserQuestionBanksDTO> dtoList);

    // Public DTO
    PublicUserQuestionBanksDTO toPublicDTO(UserQuestionBanks entity);
    UserQuestionBanks toEntityFromPublic(PublicUserQuestionBanksDTO dto);
    List<PublicUserQuestionBanksDTO> toPublicDTOList(List<UserQuestionBanks> entityList);
    List<UserQuestionBanks> toEntityListFromPublic(List<PublicUserQuestionBanksDTO> dtoList);
}
