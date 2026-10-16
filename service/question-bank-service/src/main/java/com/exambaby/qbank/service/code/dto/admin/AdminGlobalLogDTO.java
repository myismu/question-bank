package com.exambaby.qbank.service.code.dto.admin;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
/**
 * 管理端-全局日志DTO类，对应全局日志表（global_log）。
 * 用于管理端展示和操作日志数据。
 */
public class AdminGlobalLogDTO {
    /** 主键ID，唯一标识一条日志记录。 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 请求的 URL */
    private String requestUrl;
    /** 请求方法 */
    private String requestMethod;
    /** 请求的 IP 地址 */
    private String ipAddress;
    /** 请求头信息（JSON字符串） */
    private String requestHeaders;
    /** 请求参数（JSON字符串） */
    private String requestParams;
    /** 响应状态码 */
    private Integer responseStatusCode;
    /** 响应时间（毫秒） */
    private Long responseTime;
    /** 响应头信息（JSON字符串） */
    private String responseHeaders;
    /** 响应体内容 */
    private String responseBody;
    /** 异常信息 */
    private String exceptionMessage;
    /** 异常堆栈信息 */
    private String exceptionStackTrace;
    /** 用户ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    /** 用户名 */
    private String userName;
    /** 请求头中的 User-Agent */
    private String userAgent;
    /** 日志创建时间 */
    private LocalDateTime createdAt;
    /** 日志更新时间 */
//    private LocalDateTime updateTime;
} 