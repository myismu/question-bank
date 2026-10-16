package com.exambaby.qbank.service.code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exambaby.qbank.service.code.entity.Questions;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuestionsMapper extends BaseMapper<Questions> {
    int insertBatch(@Param("list") List<Questions> list);

    List<Map<String, Object>> countByBankIds(@Param("bankIds") List<Long> bankIds);
} 