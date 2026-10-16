package com.exambaby.qbank.service.code.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 章节实体类
 */
@Data
@TableName(value = "question_chapters", autoResultMap = true)
public class QuestionChapters {
    /**
     * 章节ID，雪花算法生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属题库ID，对应 question_banks.id
     */
    private Long bankId;

    /**
     * 章节标题，如"第一章 基础语法"
     */
    private String title;

    /**
     * 章节排序字段，数值越小越靠前
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime updatedAt;
}
