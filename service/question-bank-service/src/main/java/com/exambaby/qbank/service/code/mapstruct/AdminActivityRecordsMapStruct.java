package com.exambaby.qbank.service.code.mapstruct;

import com.exambaby.qbank.service.code.dto.admin.AdminActivityRecordsDTO;
import com.exambaby.qbank.service.code.entity.ActivityRecords;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminActivityRecordsMapStruct {
    
    AdminActivityRecordsDTO toDTO(ActivityRecords entity);
    
    ActivityRecords toEntity(AdminActivityRecordsDTO dto);
    
    List<AdminActivityRecordsDTO> toDTOList(List<ActivityRecords> entities);
    List<ActivityRecords> toEntityList(List<AdminActivityRecordsDTO> dtos);
}