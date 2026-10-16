package com.exambaby.qbank.service.code.dto.admin;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminBankTagsDTO {
    /**
     * 雪花ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 题库ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bankId;
    /**
     * 标签名称
     */
    private String tagName;
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}