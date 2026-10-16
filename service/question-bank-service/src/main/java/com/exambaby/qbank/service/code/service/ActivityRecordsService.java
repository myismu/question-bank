package com.exambaby.qbank.service.code.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.AnswerDTO;
import com.exambaby.qbank.service.code.dto.admin.AdminActivityRecordsDTO;
import com.exambaby.qbank.service.code.dto.publics.*;

import java.util.List;

public interface ActivityRecordsService {
    PublicActivityRecordsDTO createActivityRecord(PublicActivityRecordsDTO publicActivityRecordsDTO);

    void finishActivityRecord(PublicActivityRecordsDTO publicActivityRecordsDTO);

    PublicActivityRecordsDTO ActivityRecords(String userId, Long bankId, String type);

    void reviewActivityRecord(Long recordId, String status);

    String registerAutoSubmitTask(Long recordId, Long durationInSeconds);

    List<AnswerDTO> selectUserAnswerAndQuestionInfoByRecordId(Long recordId);

    void updateActivityRecord(PublicActivityRecordsDTO publicActivityRecordsDTO);

    AdminActivityRecordsDTO createAdminActivityRecords(AdminActivityRecordsDTO dto);

    AdminActivityRecordsDTO updateAdminActivityRecords(AdminActivityRecordsDTO dto);

    void deleteAdminActivityRecords(Long id);

    AdminActivityRecordsDTO getAdminActivityRecordsById(Long id);

    Page<AdminActivityRecordsDTO> pageAdminActivityRecords(long current, long size, String userId, Long bankId, String type);

    List<AdminActivityRecordsDTO> getAllAdminActivityRecords();

    /**
     * 获取用户学习统计概览
     * 
     * @param userId 用户ID
     * @return 学习统计概览数据
     */
    StudyStatisticsDTO getStudyStatistics(String userId);

    /**
     * 获取用户学习趋势数据
     * 
     * @param userId 用户ID
     * @param days 查询天数（默认7天）
     * @return 学习趋势数据
     */
    List<StudyTrendDTO> getStudyTrend(String userId, Integer days);

    /**
     * 获取用户题库学习进度
     * 
     * @param userId 用户ID
     * @return 题库学习进度列表
     */
    List<BankProgressDTO> getBankProgress(String userId);
} 