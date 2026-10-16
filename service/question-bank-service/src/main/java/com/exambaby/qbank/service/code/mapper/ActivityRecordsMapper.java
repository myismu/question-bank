package com.exambaby.qbank.service.code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exambaby.qbank.service.code.dto.AnswerDTO;
import com.exambaby.qbank.service.code.entity.ActivityRecords;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ActivityRecordsMapper extends BaseMapper<ActivityRecords> {
    /**
     * 根据用户ID和活动类型查询活动记录
     * @param userId 用户ID
     * @param type 活动类型（practice/exam）
     * @return 活动记录列表
     */
    List<ActivityRecords> findByUserIdAndType(@Param("userId") String userId, @Param("type") String type);

    /**
     * 根据recordId查询用户回答和问题信息
     * @param recordId 用户回答记录id
     * @return 用户回答和问题信息
     */
    List<AnswerDTO> selectUserAnswerAndQuestionInfoByRecordId(@Param("recordId") Long recordId);

    /**
     * 批量修改 不同 的值
     * @param list 回答列表
     * @return 影响行数
     */
    int batchUpdateIAnswer(@Param("list") List<AnswerDTO> list);

    /**
     * 批量修改 相同 的值
     * @param isCorrect 是否正确
     * @param ids 表主键id列表
     * @return 影响行数
     */
    int batchUpdateTypeByIds(@Param("isCorrect") int isCorrect, @Param("ids") List<Long> ids);
} 