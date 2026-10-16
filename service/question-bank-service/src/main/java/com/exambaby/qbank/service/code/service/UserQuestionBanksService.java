package com.exambaby.qbank.service.code.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exambaby.qbank.service.code.entity.UserQuestionBanks;

import java.util.List;

public interface UserQuestionBanksService {
    int savea(UserQuestionBanks userQuestionBanks);

    void addUserQuestionBank(String userId, Long bankId);

    UserQuestionBanks selectOnePublicQuestionBank(LambdaQueryWrapper<UserQuestionBanks> uqw);

    List<UserQuestionBanks> selectListPublic(LambdaQueryWrapper<UserQuestionBanks> uqWrapper);

    void deleteByBankId(Long bankId);

    void deleteUserQuestionBank(String userId, Long bankId);
}
