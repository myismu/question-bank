package com.exambaby.qbank.service.code.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.admin.AdminQuestionBanksDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionBanksDTO;
import com.exambaby.qbank.service.code.entity.QuestionBanks;

import java.util.List;

public interface QuestionBanksService {
    AdminQuestionBanksDTO createQuestionBank(AdminQuestionBanksDTO dto);
    AdminQuestionBanksDTO updateQuestionBank(AdminQuestionBanksDTO dto);
    void deleteQuestionBank(Long id);
    AdminQuestionBanksDTO getQuestionBankById(Long id);
    Page<AdminQuestionBanksDTO> pageQuestionBanks(long current, long size, String name, Long userId, String type, Integer difficulty);
    List<AdminQuestionBanksDTO> getAllQuestionBanks();
    List<PublicQuestionBanksDTO> getAllPublicQuestionBanks();
    PublicQuestionBanksDTO getPublicQuestionBankById(Long questionBankId);

    List<PublicQuestionBanksDTO> getQuestionBanksByUserId(Long userId);

    String publicCreateQuestionBank(PublicQuestionBanksDTO publicQuestionBanksDTO);

    Page<PublicQuestionBanksDTO> getQuestionBankByIdAndAllQuestions(Page<PublicQuestionBanksDTO> page, Long bankId, String title, String type, String analysis, Integer difficulty);


    boolean isExistQuestionBank(LambdaQueryWrapper<QuestionBanks> wrapper);

    boolean deleteQuestionBankById(Long bankId);

    List<PublicQuestionBanksDTO> getQuestionBanksByCreatorId(Long userId);

    void PublicUpdateQuestionBank(PublicQuestionBanksDTO publicQuestionBanksDTO);
}