package com.exambaby.qbank.service.code.mapstruct;

import com.exambaby.common.core.entity.GlobalLog;
import com.exambaby.qbank.service.code.dto.admin.AdminGlobalLogDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * GlobalLogMapStruct 是全局日志实体类（GlobalLog）与管理端DTO（AdminGlobalLogDTO）之间的映射器接口。
 * <p>
 * 此接口使用 MapStruct 自动生成实现类，支持 Entity 与 DTO 之间的双向转换。
 * 用于解耦数据库模型与前端交互模型，增强代码的可维护性与安全性。
 */
@Mapper(componentModel = "spring")
public interface GlobalLogMapStruct {
    /**
     * 将 GlobalLog 实体类转换为 AdminGlobalLogDTO。
     * @param entity 全局日志实体
     * @return 管理端DTO
     */
    AdminGlobalLogDTO toAdminDTO(GlobalLog entity);

    /**
     * 将 AdminGlobalLogDTO 转换为 GlobalLog 实体类。
     * @param dto 管理端DTO
     * @return 实体类
     */
    GlobalLog toEntityFromAdmin(AdminGlobalLogDTO dto);

    /**
     * 批量转换：实体列表转 DTO 列表
     */
    List<AdminGlobalLogDTO> toAdminDTOList(List<GlobalLog> entityList);

    /**
     * 批量转换：DTO 列表转实体列表
     */
    List<GlobalLog> toEntityListFromAdmin(List<AdminGlobalLogDTO> dtoList);
} 