package com.exambaby.qbank.service.code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exambaby.qbank.service.code.entity.UserQuestionBanks;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserQuestionBanksMapper extends BaseMapper<UserQuestionBanks> {
    void deleteByBankId(Long bankId);
} 