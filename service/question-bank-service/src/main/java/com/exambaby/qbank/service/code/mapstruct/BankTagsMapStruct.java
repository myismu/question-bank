package com.exambaby.qbank.service.code.mapstruct;

import com.exambaby.qbank.service.code.dto.admin.AdminBankTagsDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicBankTagsDTO;
import com.exambaby.qbank.service.code.entity.BankTags;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankTagsMapStruct {
    // Admin DTO
    AdminBankTagsDTO toAdminDTO(BankTags entity);
    BankTags toEntityFromAdmin(AdminBankTagsDTO dto);
    List<AdminBankTagsDTO> toAdminDTOList(List<BankTags> entityList);
    List<BankTags> toEntityListFromAdmin(List<AdminBankTagsDTO> dtoList);

    // Public DTO
    PublicBankTagsDTO toPublicDTO(BankTags entity);
    BankTags toEntityFromPublic(PublicBankTagsDTO dto);
    List<PublicBankTagsDTO> toPublicDTOList(List<BankTags> entityList);
    List<BankTags> toEntityListFromPublic(List<PublicBankTagsDTO> dtoList);

}
