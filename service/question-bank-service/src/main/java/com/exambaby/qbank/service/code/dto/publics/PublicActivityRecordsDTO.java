package com.exambaby.qbank.service.code.dto.publics;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PublicActivityRecordsDTO {
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
     * 类型：practice/exam
     */
    private String type;
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime endTime;
    /**
     * 总分（考试用）
     */
    private int totalScore;
    /**
     * 用户得分（考试用）
     */
    private int userScore;
    /**
     * 状态：in_progress/completed/timeout/abandoned
     */
    private String status;

    /**
     * 本次练习选择的章节ID
     */
//    @JsonSerialize(using = ToStringSerializer.class)
    private List<String> chapterIds; // JSON格式，使用List<Long>来表示

    /**
     * 本次练习生成的题目ID列表（有顺序）
     */
//    @JsonSerialize(using = ToStringSerializer.class)
    private List<String> questionIds; // JSON格式，使用List<Long>来表示

    /**
     * 题型配置，如["single","multiple"]
     */
    private List<String> questionTypes; // JSON格式，使用List<String>来表示

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 当前答到第几题（从0开始）
     */
    private Integer currentQuestionIndex;
    /**
     * 答题记录列表（非数据库字段，仅用于传输）
     */
    private List<PublicActivityAnswersDTO> activityAnswersList;
} 