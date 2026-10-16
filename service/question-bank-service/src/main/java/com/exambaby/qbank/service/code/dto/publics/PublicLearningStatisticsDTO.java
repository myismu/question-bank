package com.exambaby.qbank.service.code.dto.publics;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PublicLearningStatisticsDTO {
    /** 雪花ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 用户ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    /** 题库ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bankId;
    /** 练习次数 */
    private Integer practiceCount;
    /** 考试次数 */
    private Integer examCount;
    /** 总题目数 */
    private Integer totalQuestions;
    /** 正确题目数 */
    private Integer correctCount;
    /** 错误题目数 */
    private Integer wrongCount;
    /** 平均分数 */
    private BigDecimal averageScore;
} 