package com.exambaby.qbank.service.code.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.common.core.entity.GlobalLog;
import com.exambaby.qbank.service.code.dto.admin.AdminGlobalLogDTO;

import java.util.List;


public interface GlobalLogService {
    boolean saveLog(GlobalLog log);  // 保存日志
    boolean removeLogById(Long logId);  // 根据 ID 删除日志
    boolean updateLog(GlobalLog log);  // 更新日志
    GlobalLog getLogById(Long logId);  // 根据 ID 查询日志
    List<GlobalLog> getAllLogs();  // 获取所有日志

    // ========== 管理端DTO相关方法 ========== //
    AdminGlobalLogDTO createGlobalLog(AdminGlobalLogDTO dto);
    AdminGlobalLogDTO updateGlobalLog(AdminGlobalLogDTO dto);
    void deleteGlobalLog(Long id);
    AdminGlobalLogDTO getGlobalLogById(Long id);
    List<AdminGlobalLogDTO> getAllGlobalLogs();
    Page<AdminGlobalLogDTO> pageGlobalLogs(Page<AdminGlobalLogDTO> page, String requestUrl, String userName, Integer responseStatusCode);
}