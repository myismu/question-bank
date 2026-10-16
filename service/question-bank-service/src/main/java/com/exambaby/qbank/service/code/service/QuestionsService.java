package com.exambaby.qbank.service.code.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.admin.AdminQuestionsDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionBanksDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionsDTO;
import com.exambaby.qbank.service.code.entity.Questions;

import java.util.List;
import java.util.Map;

public interface QuestionsService {
    AdminQuestionsDTO createQuestion(AdminQuestionsDTO dto);

    boolean updateQuestion(AdminQuestionsDTO dto);

    boolean deleteQuestion(Long id);

    AdminQuestionsDTO getQuestionById(Long id);

    Page<AdminQuestionsDTO> pageQuestions(long current, long size, String title, Long bankId, String type, Integer difficulty);

    List<AdminQuestionsDTO> getAllQuestions();

    List<PublicQuestionsDTO> getQuestionsByBankId(Long bankId);

    Boolean addQuestion(PublicQuestionsDTO publicQuestionsDTO);

    boolean PublicUpdateQuestion(PublicQuestionsDTO publicQuestionsDTO);

    Long selectQustionsCount(LambdaQueryWrapper<Questions> qw);

    void insertQuestions(Questions question);

    Page<Questions> selectPageQustions(Page<PublicQuestionBanksDTO> objectPage, LambdaQueryWrapper<Questions> qw);

    void deleteByBankId(Long bankId);

    boolean BatchAddQuestion(List<PublicQuestionsDTO> publicQuestionsDTOList);

    Page<PublicQuestionsDTO> getQuestionsByPage(Integer pageNum, Integer pageSize, Long bankId);

    PublicQuestionsDTO getPublicQuestionById(Long id);

    //根据题库id和题目类型查询题目
    List<PublicQuestionsDTO> getQuestionsByBankIdAndType(List<Long> bankId, String type);

    long count(LambdaQueryWrapper<Questions> questionWrapper);

    Map<Long, Long> countGroupByBankIds(List<Long> bankIds);
}