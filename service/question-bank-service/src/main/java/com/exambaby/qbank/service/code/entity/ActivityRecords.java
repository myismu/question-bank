package com.exambaby.qbank.service.code.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 记录表
 */
@Data
@TableName(value = "activity_records", autoResultMap = true)
public class ActivityRecords {
    /**
     * 雪花ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 题库ID
     */
    private Long bankId;
    /**
     * 类型：practice/exam
     */
    private String type;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 总分（考试用）
     */
    private Integer totalScore;
    /**
     * 用户得分（考试用）
     */
    private Integer userScore;
    /**
     * 状态：in_progress/completed/timeout/abandoned
     */
    private String status;

    /**
     * 本次练习选择的章节ID
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> chapterIds; // JSON格式，使用List<Long>来表示

    /**
     * 本次练习生成的题目ID列表（有顺序）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> questionIds; // JSON格式，使用List<Long>来表示

    /**
     * 题型配置，如["single","multiple"]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> questionTypes; // JSON格式，使用List<String>来表示

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 当前答到第几题（从0开始）
     */
    private Integer currentQuestionIndex;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
} 