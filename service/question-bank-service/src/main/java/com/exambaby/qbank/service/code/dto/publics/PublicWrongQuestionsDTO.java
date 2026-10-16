package com.exambaby.qbank.service.code.dto.publics;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublicWrongQuestionsDTO {
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
     * 答题记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long activityAnswerId;
    /**
     * 复习状态（string类型）
     */
    private String status;//为复习：review，已复习：reviewed
    /**
     * 题目ID
     */
    private Long questionId;
    /**
     * 错误次数
     */
    private Integer errorCount;
    /**
     * 最近错误时间
     */
    private LocalDateTime lastErrorTime;

    /**
     * 非数据库字段，用于接收前端传来的题目选项
     */
    private PublicQuestionsDTO questions;
} 