package com.exambaby.qbank.service.code.mapstruct;

import com.exambaby.qbank.service.code.dto.publics.PublicActivityRecordsDTO;
import com.exambaby.qbank.service.code.entity.ActivityRecords;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityRecordsMapStruct {
    ActivityRecordsMapStruct INSTANCE = Mappers.getMapper(ActivityRecordsMapStruct.class);

    ActivityRecords toEntity(PublicActivityRecordsDTO dto);
    PublicActivityRecordsDTO toDTO(ActivityRecords entity);
    List<PublicActivityRecordsDTO> toDTOList(List<ActivityRecords> entities);

} 