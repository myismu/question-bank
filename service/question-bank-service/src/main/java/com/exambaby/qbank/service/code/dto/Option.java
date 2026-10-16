package com.exambaby.qbank.service.code.dto;

import lombok.Data;


@Data
public class Option {
    private String label;  // A, B, C, D
    private String text;   // 选项内容

    /**
     * 前端传过来的选项数据示例：
     * {
     *   "id": 1001,
     *   "type": "multiple",
     *   "title": "下列哪些属于前端框架？",
     *   "options": [
     *     { "label": "A", "text": "Vue" },
     *     { "label": "B", "text": "Spring Boot" },
     *     { "label": "C", "text": "React" }
     *   ],
     *   "answer": ["A", "C"]
     * }
     *
     * private void validateAnswerType(String type, Object answer) {
     *     switch (type) {
     *         case "single":
     *         case "short":
     *             if (!(answer instanceof String)) {
     *                 throw new IllegalArgumentException("该题型答案应为 String");
     *             }
     *             break;
     *
     *         case "multiple":
     *         case "fill":
     *             if (!(answer instanceof List)) {
     *                 throw new IllegalArgumentException("该题型答案应为 List");
     *             }
     *             break;
     *
     *         case "judge":
     *             if (!(answer instanceof Boolean)) {
     *                 throw new IllegalArgumentException("判断题答案应为 Boolean");
     *             }
     *             break;
     *
     *         default:
     *             throw new IllegalArgumentException("未知题型：" + type);
     *     }
     * }
     */

}
