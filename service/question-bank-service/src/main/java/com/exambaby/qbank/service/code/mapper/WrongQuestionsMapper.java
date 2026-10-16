package com.exambaby.qbank.service.code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.BankSimpleInfoDTO;
import com.exambaby.qbank.service.code.dto.WrongQuestionStatsVO;
import com.exambaby.qbank.service.code.entity.WrongQuestions;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WrongQuestionsMapper extends BaseMapper<WrongQuestions> {
    int batchUpdateErrorCount(@Param("list") List<WrongQuestions> list);

    WrongQuestionStatsVO getStatsByUserId(@Param("userId") Long userId);

    Page<WrongQuestions> selectWrongQuestionsWithQuestions(
            Page<WrongQuestions> page,
            @Param("userId") Long userId,
            @Param("bankId") Long bankId,
            @Param("status") String status,
            @Param("type") String type
    );

    List<BankSimpleInfoDTO> getDistinctBanksByUserId(@Param("userId") Long userId);

} 