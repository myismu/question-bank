/*
 Navicat Premium Dump SQL

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 50737 (5.7.37)
 Source Host           : localhost:3306
 Source Schema         : question_bank

 Target Server Type    : MySQL
 Target Server Version : 50737 (5.7.37)
 File Encoding         : 65001

 Date: 22/06/2025 17:15:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity_answers
-- ----------------------------
DROP TABLE IF EXISTS `activity_answers`;
CREATE TABLE `activity_answers`  (
  `id` bigint(20) NOT NULL COMMENT '雪花ID',
  `record_id` bigint(20) NULL DEFAULT NULL COMMENT '记录ID（对应activity_records.id）',
  `question_id` bigint(20) NULL DEFAULT NULL COMMENT '题目ID',
  `user_answer` json NULL COMMENT '用户答案',
  `is_correct` tinyint(1) NULL DEFAULT NULL COMMENT '是否正确',
  `score` int(11) NULL DEFAULT NULL COMMENT '得分（考试用）',
  `answer_time` datetime NULL DEFAULT NULL COMMENT '答题时间',
  `created_at` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_record_id`(`record_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '练习/考试答题记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for activity_records
-- ----------------------------
DROP TABLE IF EXISTS `activity_records`;
CREATE TABLE `activity_records`  (
  `id` bigint(20) NOT NULL COMMENT '雪花ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `bank_id` bigint(20) NULL DEFAULT NULL COMMENT '题库ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型：practice/exam',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `total_score` int(11) NULL DEFAULT NULL COMMENT '总分（考试用）',
  `user_score` int(11) NULL DEFAULT NULL COMMENT '用户得分（考试用）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态：in_progress/completed/timeout/abandoned',
  `chapter_ids` json NULL COMMENT '本次练习选择的章节ID',
  `question_ids` json NULL COMMENT '本次练习生成的题目ID列表（有顺序）',
  `question_types` json NULL COMMENT '题型配置，如[\"single\",\"multiple\"]',
  `question_count` int(11) NULL DEFAULT 0 COMMENT '题目数量',
  `current_question_index` int(11) NULL DEFAULT 0 COMMENT '当前答到第几题（从0开始）',
  `created_at` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_bank_id`(`bank_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '练习/考试记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for bank_tags
-- ----------------------------
DROP TABLE IF EXISTS `bank_tags`;
CREATE TABLE `bank_tags`  (
  `id` bigint(20) NOT NULL COMMENT '雪花ID',
  `bank_id` bigint(20) NULL DEFAULT NULL COMMENT '题库ID',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签名称',
  `created_at` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_bank_id`(`bank_id`) USING BTREE COMMENT '题库ID索引：用于查询特定题库的标签'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '题库标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for global_log
-- ----------------------------
DROP TABLE IF EXISTS `global_log`;
CREATE TABLE `global_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `request_url` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求的 URL',
  `request_method` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求方法',
  `ip_address` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'IP 地址',
  `user_agent` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'User-Agent 信息',
  `request_headers` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '请求头信息',
  `request_params` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '请求参数',
  `response_status_code` int(11) NULL DEFAULT NULL COMMENT '响应状态码',
  `response_time` bigint(20) NULL DEFAULT NULL COMMENT '响应时间（毫秒）',
  `response_headers` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '响应头信息',
  `response_body` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '响应体内容',
  `exception_message` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '异常信息',
  `exception_stack_trace` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '异常堆栈信息',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户 ID',
  `user_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `created_at` datetime NULL DEFAULT NULL COMMENT '日志创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1936699496081723395 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '全局请求日志记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for learning_statistics
-- ----------------------------
DROP TABLE IF EXISTS `learning_statistics`;
CREATE TABLE `learning_statistics`  (
  `id` bigint(20) NOT NULL COMMENT '雪花ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `bank_id` bigint(20) NULL DEFAULT NULL COMMENT '题库ID',
  `practice_count` int(11) NULL DEFAULT NULL COMMENT '练习次数',
  `exam_count` int(11) NULL DEFAULT NULL COMMENT '考试次数',
  `total_questions` int(11) NULL DEFAULT NULL COMMENT '总题目数',
  `correct_count` int(11) NULL DEFAULT NULL COMMENT '正确题目数',
  `wrong_count` int(11) NULL DEFAULT NULL COMMENT '错误题目数',
  `average_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '平均分数',
  `created_at` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE COMMENT '用户ID索引：用于查询用户的学习统计',
  INDEX `idx_bank_id`(`bank_id`) USING BTREE COMMENT '题库ID索引：用于查询特定题库的学习统计'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学习统计表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question_banks
-- ----------------------------
DROP TABLE IF EXISTS `question_banks`;
CREATE TABLE `question_banks`  (
  `id` bigint(20) NOT NULL COMMENT '雪花ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户id',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '题库名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题库描述',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '题库类型：public(公开)、private(私有)',
  `difficulty` tinyint(4) NULL DEFAULT NULL COMMENT '难度：1-5',
  `created_at` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '题库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question_chapters
-- ----------------------------
DROP TABLE IF EXISTS `question_chapters`;
CREATE TABLE `question_chapters`  (
  `id` bigint(20) NOT NULL COMMENT '章节ID，雪花算法生成',
  `bank_id` bigint(20) NOT NULL COMMENT '所属题库ID，对应 question_banks.id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '章节标题，如“第一章 基础语法”',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '章节排序字段，数值越小越靠前',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '题库章节表：用于对题库内题目进行章节分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for questions
-- ----------------------------
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions`  (
  `id` bigint(20) NOT NULL COMMENT '雪花ID',
  `bank_id` bigint(20) NULL DEFAULT NULL COMMENT '所属题库ID',
  `chapter_id` bigint(20) NULL DEFAULT NULL COMMENT '所属章节 ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '题目类型：single(单选)、multiple(多选)、fill(填空)、judge(判断)、short(简答)',
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '题目标题',
  `options` json NULL COMMENT '选项（JSON格式）',
  `answer` json NULL COMMENT '答案（JSON格式）',
  `analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '解析',
  `difficulty` tinyint(4) NULL DEFAULT 1 COMMENT '难度：1-5',
  `sort_order` int(11) NULL DEFAULT NULL COMMENT '排序',
  `created_at` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_bank_id`(`bank_id`) USING BTREE COMMENT '题库ID索引：用于查询特定题库的题目'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '题目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_question_banks
-- ----------------------------
DROP TABLE IF EXISTS `user_question_banks`;
CREATE TABLE `user_question_banks`  (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `question_banks_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表关联题库表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for wrong_questions
-- ----------------------------
DROP TABLE IF EXISTS `wrong_questions`;
CREATE TABLE `wrong_questions`  (
  `id` bigint(20) NOT NULL COMMENT '雪花ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `question_id` bigint(20) NULL DEFAULT NULL COMMENT '题目ID',
  `error_count` int(11) NULL DEFAULT NULL COMMENT '错误次数',
  `last_error_time` datetime NULL DEFAULT NULL COMMENT '最近错误时间',
  `created_at` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE COMMENT '用户ID索引：用于查询用户的错题',
  INDEX `idx_question_id`(`question_id`) USING BTREE COMMENT '题目ID索引：用于查询特定题目的错误记录'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '错题本表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
