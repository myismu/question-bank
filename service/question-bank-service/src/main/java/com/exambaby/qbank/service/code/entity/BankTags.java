package com.exambaby.qbank.service.code.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bank_tags")
public class BankTags {
    /** 雪花ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 题库ID */
    private Long bankId;
    /** 标签名称 */
    private String tagName;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
} 