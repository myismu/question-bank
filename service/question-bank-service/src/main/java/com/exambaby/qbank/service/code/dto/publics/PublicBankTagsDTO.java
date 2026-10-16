package com.exambaby.qbank.service.code.dto.publics;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class PublicBankTagsDTO {
    /** 雪花ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 题库ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bankId;
    /** 标签名称 */
    private String tagName;
} 