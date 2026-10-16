package com.exambaby.qbank.service.code.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.qbank.service.code.dto.admin.AdminQuestionChaptersDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionChaptersDTO;

import java.util.List;

public interface QuestionChaptersService {
    Long createQuestionChapters(PublicQuestionChaptersDTO chaptersDTOList);

    void deleteByBankId(Long bankId);

    // 管理端-新增章节
    AdminQuestionChaptersDTO createAdminQuestionChapters(AdminQuestionChaptersDTO dto);

    // 管理端-修改章节
    AdminQuestionChaptersDTO updateAdminQuestionChapters(AdminQuestionChaptersDTO dto);

    // 管理端-删除章节
    void deleteAdminQuestionChapters(Long id);

    // 管理端-根据ID获取章节
    AdminQuestionChaptersDTO getAdminQuestionChaptersById(Long id);

    // 管理端-分页查询章节
    Page<AdminQuestionChaptersDTO> pageAdminQuestionChapters(long current, long size, String title, Long bankId);

    // 管理端-获取所有章节
    List<AdminQuestionChaptersDTO> getAllAdminQuestionChapters();
}
