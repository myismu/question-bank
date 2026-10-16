package com.exambaby.qbank.service.code.dto.publics;

import lombok.Data;

/**
 * 学习趋势数据传输对象
 */
@Data
public class StudyTrendDTO {
    /**
     * 日期（格式：yyyy-MM-dd）
     */
    private String date;
    
    /**
     * 练习次数
     */
    private int practiceCount;
    
    /**
     * 考试次数
     */
    private int examCount;
    
    /**
     * 题目数量
     */
    private int questionCount;
    
    /**
     * 正确题目数
     */
    private int correctCount;
}