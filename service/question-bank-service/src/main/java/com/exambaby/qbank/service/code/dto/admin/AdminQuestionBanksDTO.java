package com.exambaby.qbank.service.code.dto.admin;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminQuestionBanksDTO {
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
     * 题目数量（非数据表字段，后端查询出来后获取list的长度赋值）
     */
    private Integer questionCount;
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 