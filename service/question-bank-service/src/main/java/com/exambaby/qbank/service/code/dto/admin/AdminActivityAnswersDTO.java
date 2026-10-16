package com.exambaby.qbank.service.code.dto.admin;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminActivityAnswersDTO {
    /** 雪花ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 记录ID（对应activity_records.id） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;
    /** 题目ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long questionId;
    /** 用户答案 */
    private String userAnswer;
    /** 是否正确 */
    private Integer isCorrect;
    /** 得分（考试用） */
    private Integer score;
    /** 答题时间 */
    private LocalDateTime answerTime;
    /** 创建时间 */
    private LocalDateTime createdAt;
} 