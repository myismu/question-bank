package com.exambaby.qbank.service.code.dto.publics;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PublicActivityAnswersDTO {
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
    private List<String> userAnswer;
    /** 是否正确 */
    private Integer isCorrect;

    /** 得分（考试用） */
    private Integer score;
    /** 答题时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime answerTime;
} 