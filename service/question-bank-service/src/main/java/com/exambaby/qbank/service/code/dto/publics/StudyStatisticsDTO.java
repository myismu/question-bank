package com.exambaby.qbank.service.code.dto.publics;

import lombok.Data;

/**
 * 学习统计概览数据传输对象
 */
@Data
public class StudyStatisticsDTO {
    /**
     * 总练习次数
     */
    private Integer totalPracticeCount;
    
    /**
     * 总考试次数
     */
    private Integer totalExamCount;
    
    /**
     * 总题目数
     */
    private Integer totalQuestions;
    
    /**
     * 正确题目数
     */
    private Integer correctCount;
    
    /**
     * 错误题目数
     */
    private Integer wrongCount;
    
    /**
     * 正确率（百分比）
     */
    private Double accuracyRate;
}