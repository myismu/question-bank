package com.exambaby.qbank.service.code.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("learning_statistics")
public class LearningStatistics {
    /** 雪花ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 题库ID */
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
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
} 