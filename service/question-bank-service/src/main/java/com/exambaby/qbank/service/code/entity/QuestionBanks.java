package com.exambaby.qbank.service.code.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question_banks")
public class QuestionBanks {
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
     * 题库名称
     */
    private String name;
    /**
     * 题库描述
     */
    private String description;
    /**
     * 封面图片URL
     */
    private String coverImage;
    /**
     * 题库类型：public(公开)、private(私有)
     */
    private String type;
    /**
     * 难度：1-5
     */
    private Integer difficulty;
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
     * 题目数量（非数据表字段，后端查询出来后获取list的长度赋值）
     */
    @TableField(exist = false)
    private Integer questionCount;
    //    是否是收藏题库（非数据表字段，后端查询收藏表中是否有该题库记录赋值）
    @TableField(exist = false)
    private Boolean isCollected;
} 