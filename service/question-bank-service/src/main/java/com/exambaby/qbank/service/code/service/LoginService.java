package com.exambaby.qbank.service.code.service;

import com.exambaby.common.core.dto.UsersDTO;


public interface LoginService {

    public String login(UsersDTO usersDTO);
    /**
     * 重置密码
     * @param usersDTO 用户信息
     * @return 重置结果
     */
    String resetPassword(UsersDTO usersDTO);

    /**
     * 修改密码
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    String changePassword(String oldPassword, String newPassword);
}
