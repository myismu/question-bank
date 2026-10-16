package com.exambaby.qbank.service.code.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.BankSimpleInfoDTO;
import com.exambaby.qbank.service.code.dto.WrongQuestionStatsVO;
import com.exambaby.qbank.service.code.dto.admin.AdminWrongQuestionsDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicWrongQuestionsDTO;
import com.exambaby.qbank.service.code.entity.WrongQuestions;

import java.util.List;

public interface WrongQuestionsService {
    AdminWrongQuestionsDTO createWrongQuestion(AdminWrongQuestionsDTO dto);

    AdminWrongQuestionsDTO updateWrongQuestion(AdminWrongQuestionsDTO dto);

    void deleteWrongQuestion(Long id);

    AdminWrongQuestionsDTO getWrongQuestionById(Long id);

    Page<AdminWrongQuestionsDTO> pageWrongQuestions(long current, long size, Long userId, Long questionId);

    List<AdminWrongQuestionsDTO> getAllWrongQuestions();

    List<WrongQuestions> list(LambdaQueryWrapper<WrongQuestions> in);

    void saveBatchWrongQuestions(List<WrongQuestions> newWrongQuestions);

    int batchUpdateErrorCount(List<WrongQuestions> updateList);

    Page<PublicWrongQuestionsDTO> getPageWrongQuestionsList(Integer pageNum, Integer pageSize, Long questionBankId, String questionType, String status, String userId);

    Page<PublicWrongQuestionsDTO> getWrongQuestionsPage(Integer pageNum, Integer pageSize, Long userId, Long bankId, String status, String type);

    WrongQuestionStatsVO getWrongQuestionStats(String userId);

    int removeWrongQuestion(Long wrongQuestionId);

    List<BankSimpleInfoDTO> getDistinctBanksByUserId(Long userId);
} 