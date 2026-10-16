package com.exambaby.qbank.service.code.dto.publics;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class PublicUserQuestionBanksDTO {
    /** 雪花ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 用户ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    /** 题库ID */
    private Long questionBanksId;
} 