package com.exambaby.qbank.service.code.mapstruct;

import com.exambaby.qbank.service.code.dto.admin.AdminActivityAnswersDTO;
import com.exambaby.qbank.service.code.entity.ActivityAnswers;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminActivityAnswersMapStruct {
    
    @Mapping(target = "userAnswer", source = "userAnswer", qualifiedByName = "listToString")
    AdminActivityAnswersDTO toDTO(ActivityAnswers entity);
    
    @Mapping(target = "userAnswer", source = "userAnswer", qualifiedByName = "stringToList")
    ActivityAnswers toEntity(AdminActivityAnswersDTO dto);
    
    List<AdminActivityAnswersDTO> toDTOList(List<ActivityAnswers> entities);
    List<ActivityAnswers> toEntityList(List<AdminActivityAnswersDTO> dtos);
    
    @Named("listToString")
    default String listToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list);
    }
    
    @Named("stringToList")
    default List<String> stringToList(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        return List.of(str.split(","));
    }
}