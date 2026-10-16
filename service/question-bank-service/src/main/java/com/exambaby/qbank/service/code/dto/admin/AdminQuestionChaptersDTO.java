package com.exambaby.qbank.service.code.dto.admin;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class AdminQuestionChaptersDTO {
    /**
     * 章节ID，雪花算法生成
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 所属题库ID，对应 question_banks.id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bankId;

    /**
     * 章节标题，如"第一章 基础语法"
     */
    private String title;

    /**
     * 章节排序字段，数值越小越靠前
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
