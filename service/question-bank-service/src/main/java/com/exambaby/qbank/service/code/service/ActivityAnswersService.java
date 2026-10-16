package com.exambaby.qbank.service.code.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.admin.AdminActivityAnswersDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicActivityAnswersDTO;
import com.exambaby.qbank.service.code.entity.ActivityAnswers;

import java.util.List;

public interface ActivityAnswersService {
    void saveOrUpdateActivityAnswers(List<PublicActivityAnswersDTO> publicActivityAnswersDTO);

    List<PublicActivityAnswersDTO> listActivityAnswers(String recordId);

    List<PublicActivityAnswersDTO> getPublicActivityRecordsDTOS(Long id);

    void deleteActivityAnswersByRecordId(String recordId);

    List<ActivityAnswers> getActivityAnswersList(LambdaQueryWrapper<ActivityAnswers> activityAnswersLambdaQueryWrapper);

    AdminActivityAnswersDTO createAdminActivityAnswers(AdminActivityAnswersDTO dto);

    AdminActivityAnswersDTO updateAdminActivityAnswers(AdminActivityAnswersDTO dto);

    void deleteAdminActivityAnswers(Long id);

    AdminActivityAnswersDTO getAdminActivityAnswersById(Long id);

    Page<AdminActivityAnswersDTO> pageAdminActivityAnswers(long current, long size, String recordId, Long questionId);

    List<AdminActivityAnswersDTO> getAllAdminActivityAnswers();

    //    根据questionIds 数组批量查询答题记录列表
//    List<PublicActivityAnswersDTO> getPublicActivityAnswersDTOListByRecordIds(List<Long> questionIds);
} 