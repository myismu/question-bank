package com.exambaby.qbank.service.code.dto.publics;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 题库学习进度数据传输对象
 */
@Data
public class BankProgressDTO {
    /**
     * 题库ID
     */
    @JsonSerialize(using = ToStringSerializer.class) // 解决Long精度丢失问题
    private Long bankId;
    
    /**
     * 题库名称
     */
    private String bankName;
    
    /**
     * 进度百分比
     */
    private Double progressPercentage;
    
    /**
     * 已完成题目数
     */
    private Integer completedQuestions;
    
    /**
     * 总题目数
     */
    private Integer totalQuestions;
    
    /**
     * 进度条颜色
     */
    private String progressColor;
}