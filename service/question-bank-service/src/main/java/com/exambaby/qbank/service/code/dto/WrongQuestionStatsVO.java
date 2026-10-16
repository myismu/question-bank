package com.exambaby.qbank.service.code.dto;

import lombok.Data;

@Data
public class WrongQuestionStatsVO {
    private Integer total;//总错题数
    private Integer today;//今日错题数
    private Integer reviewed;//已复习错题数
    private Integer unreviewed;//未复习错题数
}
