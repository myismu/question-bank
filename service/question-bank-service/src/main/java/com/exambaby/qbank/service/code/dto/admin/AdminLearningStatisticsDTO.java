package com.exambaby.qbank.service.code.dto.admin;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminLearningStatisticsDTO {
    /**
     * 雪花ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    /**
     * 题库ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bankId;
    /**
     * 练习次数
     */
    private Integer practiceCount;
    /**
     * 考试次数
     */
    private Integer examCount;
    /**
     * 总题目数
     */
    private Integer totalQuestions;
    /**
     * 正确题目数
     */
    private Integer correctCount;
    /**
     * 错误题目数
     */
    private Integer wrongCount;
    /**
     * 平均分数
     */
    private BigDecimal averageScore;
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 