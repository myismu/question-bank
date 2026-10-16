package com.exambaby.qbank.service.code.dto.publics;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

@Data
public class PublicQuestionBanksDTO {
    /**
     * 雪花ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 题库名称
     */
    private String name;
    /**
     * 题库描述
     */
    private String description;
    /**
     * 封面图片URL
     */
    private String coverImage;
    /**
     * 题库类型：public(公开)、private(私有)
     */
    private String type;
    /**
     * 难度：1-5
     */
    private Integer difficulty;

//非数据库字段------------------------------：
    /**
     * 题目信息列表
     */
    private List<PublicQuestionsDTO> questionsDTOList;
    /**
     * 章节信息列表
     */
    private PublicQuestionChaptersDTO chaptersDTO;
    /**
     * 题目数量（非数据表字段，后端查询出来后获取list的长度赋值）
     */
    private Integer questionCount;
    //  是否已收藏（非数据表字段，后端查询出来后赋值）
    private Boolean isCollected;
} 