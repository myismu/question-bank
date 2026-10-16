package com.exambaby.qbank.service.code.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 答题记录表
 */
@Data
@TableName(value = "activity_answers", autoResultMap = true)
public class ActivityAnswers {
    /**
     * 雪花ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 记录ID（对应activity_records.id）
     */
    private Long recordId;//
    /**
     * 题目ID
     */
    private Long questionId;
    /**
     * 用户答案
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> userAnswer;
    /**
     * 是否正确
     */
    private Integer isCorrect;
    /**
     * 得分（考试用）
     */
    private Integer score;
    /**
     * 答题时间
     */
    private LocalDateTime answerTime;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

} 