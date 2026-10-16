package com.exambaby.qbank.service.code.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.common.core.entity.GlobalLog;
import com.exambaby.qbank.service.code.dto.admin.AdminGlobalLogDTO;
import com.exambaby.qbank.service.code.mapper.GlobalLogMapper;
import com.exambaby.qbank.service.code.mapstruct.GlobalLogMapStruct;
import com.exambaby.qbank.service.code.service.GlobalLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * description: 全局日志服务实现类
 */
@Service
public class GlobalLogServiceImpl implements GlobalLogService {

    private final GlobalLogMapper globalLogMapper;
    private final GlobalLogMapStruct globalLogMapStruct;

    public GlobalLogServiceImpl(GlobalLogMapper globalLogMapper, GlobalLogMapStruct globalLogMapStruct) {
        this.globalLogMapper = globalLogMapper;
        this.globalLogMapStruct = globalLogMapStruct;
    }

    @Override
    public boolean saveLog(GlobalLog log) {
        return globalLogMapper.insert(log) > 0;
    }

    @Override
    public boolean removeLogById(Long logId) {
        return globalLogMapper.deleteById(logId) > 0;
    }

    @Override
    public boolean updateLog(GlobalLog log) {
        return globalLogMapper.updateById(log) > 0;
    }

    @Override
    public GlobalLog getLogById(Long logId) {
        return globalLogMapper.selectById(logId);
    }

    @Override
    public List<GlobalLog> getAllLogs() {
        return globalLogMapper.selectList(null);  // 查询所有记录
    }

    // ========== 管理端DTO相关方法 ========== //
    @Override
    public AdminGlobalLogDTO createGlobalLog(AdminGlobalLogDTO dto) {
        GlobalLog entity = globalLogMapStruct.toEntityFromAdmin(dto);
        globalLogMapper.insert(entity);
        return globalLogMapStruct.toAdminDTO(entity);
    }

    @Override
    public AdminGlobalLogDTO updateGlobalLog(AdminGlobalLogDTO dto) {
        GlobalLog entity = globalLogMapStruct.toEntityFromAdmin(dto);
        globalLogMapper.updateById(entity);
        return globalLogMapStruct.toAdminDTO(entity);
    }

    @Override
    public void deleteGlobalLog(Long id) {
        globalLogMapper.deleteById(id);
    }

    @Override
    public AdminGlobalLogDTO getGlobalLogById(Long id) {
        GlobalLog entity = globalLogMapper.selectById(id);
        return globalLogMapStruct.toAdminDTO(entity);
    }

    @Override
    public List<AdminGlobalLogDTO> getAllGlobalLogs() {
        List<GlobalLog> list = globalLogMapper.selectList(null);
        return globalLogMapStruct.toAdminDTOList(list);
    }

    @Override
    public Page<AdminGlobalLogDTO> pageGlobalLogs(Page<AdminGlobalLogDTO> page, String requestUrl, String userName, Integer responseStatusCode) {
        LambdaQueryWrapper<GlobalLog> wrapper = new LambdaQueryWrapper<>();
        if (requestUrl != null && !requestUrl.isEmpty()) {
            wrapper.like(GlobalLog::getRequestUrl, requestUrl);
        }
        if (userName != null && !userName.isEmpty()) {
            wrapper.like(GlobalLog::getUserName, userName);
        }
        if (responseStatusCode != null) {
            wrapper.eq(GlobalLog::getResponseStatusCode, responseStatusCode);
        }
//        Page<GlobalLog> page = new Page<>(current, size);
        Page<GlobalLog> entityPage = globalLogMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        // 替换 records 为 DTO
        List<AdminGlobalLogDTO> dtoList = globalLogMapStruct.toAdminDTOList(entityPage.getRecords());
        Page<AdminGlobalLogDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
}
