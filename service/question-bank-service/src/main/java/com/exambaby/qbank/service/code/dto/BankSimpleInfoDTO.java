package com.exambaby.qbank.service.code.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class BankSimpleInfoDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bankId;
    private String bankName;
}
