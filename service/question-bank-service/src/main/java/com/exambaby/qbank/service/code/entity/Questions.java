package com.exambaby.qbank.service.code.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 题目实体类
 */
@Data
@TableName(value = "questions", autoResultMap = true)
public class Questions {
    /**
     * 雪花ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 所属题库ID
     */
    private Long bankId;
    /**
     * 所属章节ID
     */
    private Long chapterId;
    /**
     * 题目类型：single(单选)、multiple(多选)、fill(填空)、judge(判断)、short(简答)
     */
    private String type;
    /**
     * 题目标题
     */
    private String title;
    /**
     * 选项（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> options;
    /**
     * 答案（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<List<String>> answer;
    /**
     * 解析
     */
    private String analysis;
    /**
     * 难度：1-5
     */
    private Integer difficulty;
    /**
     * 排序
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