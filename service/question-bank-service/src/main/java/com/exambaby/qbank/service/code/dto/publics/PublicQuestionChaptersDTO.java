package com.exambaby.qbank.service.code.dto.publics;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class PublicQuestionChaptersDTO {
    /**
     * 章节ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 所属题库ID，对应 question_banks.id
     */
    private Long bankId;

    /**
     * 章节标题，如"第一章 基础语法"
     */
    private String title;

    /**
     * 章节排序字段，数值越小越靠前
     */
    private Integer sortOrder;

}
