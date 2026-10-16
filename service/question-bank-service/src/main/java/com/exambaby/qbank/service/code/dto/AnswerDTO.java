package com.exambaby.qbank.service.code.dto;

import lombok.Data;

import java.util.List;

/**
 * 非数据库表，仅用于传输数据
 */
@Data
public class AnswerDTO {
    private Long id;
    private List<String> userAnswer;
    private List<String> answer;
    private String type;
    private int isCorrect;
}