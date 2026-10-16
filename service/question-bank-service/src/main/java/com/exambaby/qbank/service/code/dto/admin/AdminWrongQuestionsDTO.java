package com.exambaby.qbank.service.code.dto.admin;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminWrongQuestionsDTO {
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
     * 题目ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long questionId;
    /**
     * 答题记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityAnswerId;
    /**
     * 复习状态（string类型）
     */
    private String status;//为复习：review，已复习：reviewed
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
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 