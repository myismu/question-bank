package com.exambaby.qbank.service.code.dto.publics;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PublicQuestionsDTO {
    /**
     * ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 所属题库ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bankId;
    /**
     * 所属章节ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long chapterId;
    /**
     * 题目类型：single(单选)、multiple(多选)、fill(填空)、judge(判断)、short(简答)
     */
    private String type;
    /**
     * 题目标题
     */
    private String title;
    /**
     * 选项（JSON格式）
     */
    private Map<String, String> options;
    /**
     * 答案（JSON格式）
     */
    private List<List<String>> answer;
    /**
     * 解析
     */
    private String analysis;
    /**
     * 难度：1-5
     */
    private Integer difficulty;
    /**
     * 排序
     */
    private Integer sortOrder;
} 