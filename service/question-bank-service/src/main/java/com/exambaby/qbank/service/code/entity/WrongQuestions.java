package com.exambaby.qbank.service.code.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wrong_questions")
public class WrongQuestions {
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
     * 题目ID
     */
    private Long questionId;
    /**
     * 答题记录ID
     */
    private Long activityAnswerId;
    /**
     * 复习状态（string类型）
     */
    private String status;//待复习：pending，已复习：reviewed，忽略：ignored
    /**
     * 错误次数
     */
    private Integer errorCount;
    /**
     * 最近错误时间
     */
    private LocalDateTime lastErrorTime;

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

    /**
     * 题目信息
     * 关联查询
     */
    @TableField(exist = false)// 不生成字段
    private Questions questions;
} 