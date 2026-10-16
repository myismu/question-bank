package com.exambaby.qbank.service.code.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exambaby.common.core.domain.BusinessException;
import com.exambaby.qbank.service.code.entity.UserQuestionBanks;
import com.exambaby.qbank.service.code.mapper.UserQuestionBanksMapper;
import com.exambaby.qbank.service.code.service.UserQuestionBanksService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserQuestionBanksServiceImpl extends ServiceImpl<UserQuestionBanksMapper, UserQuestionBanks> implements UserQuestionBanksService {

    private final UserQuestionBanksMapper userQuestionBanksMapper;

    public UserQuestionBanksServiceImpl(UserQuestionBanksMapper userQuestionBanksMapper) {
        this.userQuestionBanksMapper = userQuestionBanksMapper;
    }


    @Override
    public int savea(UserQuestionBanks entity) {
        return userQuestionBanksMapper.insert(entity);
    }

    @Override
    public void addUserQuestionBank(String userId, Long bankId) {
//        1. 先判断用户是否已经收藏过该题库
        QueryWrapper<UserQuestionBanks> userQuestionBanksQueryWrapper = new QueryWrapper<>();
        userQuestionBanksQueryWrapper.eq("user_id", userId).eq("question_banks_id", bankId);
        UserQuestionBanks userQuestionBanks1 = userQuestionBanksMapper.selectOne(userQuestionBanksQueryWrapper);
        if (userQuestionBanks1!= null) {
            throw new BusinessException("该题库已经收藏过", 444);
        }
        // 2. 收藏题库
        UserQuestionBanks userQuestionBanks = new UserQuestionBanks();
        userQuestionBanks.setUserId(Long.valueOf(userId));
        userQuestionBanks.setQuestionBanksId(bankId);
        int i = userQuestionBanksMapper.insert(userQuestionBanks);
//        如果收藏失败，则抛出异常
        if (i!= 1) {
            throw new BusinessException("收藏失败", 444);
        }
    }

    @Override
    public UserQuestionBanks selectOnePublicQuestionBank(LambdaQueryWrapper<UserQuestionBanks> uqw) {
        return userQuestionBanksMapper.selectOne(uqw);
    }

    @Override
    public List<UserQuestionBanks> selectListPublic(LambdaQueryWrapper<UserQuestionBanks> uqWrapper) {
        return userQuestionBanksMapper.selectList(uqWrapper);
    }

    @Override
    public void deleteByBankId(Long bankId) {
            userQuestionBanksMapper.delete(new LambdaQueryWrapper<UserQuestionBanks>().eq(UserQuestionBanks::getQuestionBanksId, bankId));
    }

    @Override
    public void deleteUserQuestionBank(String userId, Long bankId) {
        int i = userQuestionBanksMapper.delete(new LambdaQueryWrapper<UserQuestionBanks>()
                .eq(UserQuestionBanks::getUserId, userId)
                .eq(UserQuestionBanks::getQuestionBanksId, bankId));
        if (i!= 1) {
            throw new BusinessException("取消收藏失败", 444);
        }

    }
}
