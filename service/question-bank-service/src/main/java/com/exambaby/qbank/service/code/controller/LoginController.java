package com.exambaby.qbank.service.code.controller;

import com.exambaby.api.userhub.client.UserHubClient;
import com.exambaby.common.core.domain.ApiResponse;
import com.exambaby.common.core.domain.BusinessException;
import com.exambaby.common.core.dto.RedisOnlineUser;
import com.exambaby.common.core.dto.UsersDTO;
import com.exambaby.common.core.utils.ClientTypeUtil;
import com.exambaby.common.core.utils.RedisKeyUtils;
import com.exambaby.common.core.utils.ThreadLocalContext;
import com.exambaby.qbank.service.code.service.serviceImpl.LoginServiceImpl;
import com.exambaby.qbank.service.code.service.serviceImpl.OnlineUserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private final LoginServiceImpl loginService;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserHubClient userHubClient;
    private final OnlineUserServiceImpl onlineUserService;

    public LoginController(LoginServiceImpl loginService, StringRedisTemplate redisTemplate, UserHubClient userHubClient, OnlineUserServiceImpl onlineUserService) {
        this.loginService = loginService;
        this.stringRedisTemplate = redisTemplate;
        this.userHubClient = userHubClient;
        this.onlineUserService = onlineUserService;
    }

    @Operation(summary = "获取验证码", description = "获取登录验证码图片或链接")
    @GetMapping("/captcha")
    public Object captcha() {
        ThreadLocalContext.setSystemCode("userHubTest");
        return "http://dummyimage.com/100x40/dcdfe6/000000.png&text=V3Admin";
    }

    @Operation(summary = "用户登录", description = "用户登录接口，传入用户DTO和请求信息，返回登录结果")
    @PostMapping("/login")
    public Object login(@Valid @RequestBody UsersDTO usersDTO, HttpServletRequest request) {
        if (usersDTO.getSystemCode() == null) {
            throw new BusinessException("网站信息缺失，请联系管理员修复", 444);
        }
        String userAgent = request.getHeader("User-Agent");
        String clientIp = ClientTypeUtil.getClientIp(request);
        return userHubClient.login(usersDTO, userAgent, clientIp);
    }

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/getUserInfo")
    public ApiResponse<Map<String, Object>> getUser() {
        String userId = ThreadLocalContext.getUserId();
        return userHubClient.getUserInfo(userId);
    }

    @Operation(summary = "获取个人信息", description = "获取当前登录用户的个人信息")
    @GetMapping("/getPersonalInfo")
    public ApiResponse<UsersDTO> getPersonalInfo() {
        String userId = ThreadLocalContext.getUserId();
        return userHubClient.getPersonalInfo(userId);
    }

    @Operation(summary = "用户注册", description = "用户注册接口，传入用户DTO，返回注册结果")
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody UsersDTO usersDTO) {
        return userHubClient.register(usersDTO);
    }

    @Operation(summary = "重置密码", description = "用户重置密码接口，传入用户DTO，返回重置结果")
    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody UsersDTO usersDTO) {
        return loginService.resetPassword(usersDTO);
    }

    @Operation(summary = "修改密码", description = "用户修改密码接口，传入原密码和新密码，返回修改结果")
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        return loginService.changePassword(oldPassword, newPassword);
    }

    @Operation(summary = "用户退出登录", description = "当前用户退出登录，清除Redis中的token")
    @GetMapping("/OutLogin")
    public Object OutLogin() {
        String userId = ThreadLocalContext.getUserId();
        String systemCode = ThreadLocalContext.getSystemCode();
        String key = RedisKeyUtils.buildOnlineUserKey(systemCode, userId);
        String token = stringRedisTemplate.opsForValue().get(key);
        if (token != null) {
            Boolean delete = stringRedisTemplate.delete(key);
            if (delete) {
                return "退出登录成功";
            } else {
                throw new BusinessException("退出登录失败", 444);
            }
        } else {
            throw new BusinessException("您已退出登录", 444);
        }
    }

    @Operation(summary = "获取在线用户", description = "获取所有当前在线的用户列表")
    @GetMapping("/online-users")
    public List<RedisOnlineUser> getOnlineUsers() {
        return onlineUserService.getAllOnlineUsers();
    }

    @Operation(summary = "踢出用户", description = "根据用户ID强制踢出用户登录状态")
    @GetMapping("/logout")
    public String logout(@RequestParam String userId) {
        onlineUserService.kickOut(userId);
        return "退出登录成功";
    }

    @Operation(summary = "修改个人信息", description = "修改当前登录用户的个人信息")
    @PostMapping("/modifyPersonalInfo")
    public ApiResponse<Boolean> modifyPersonalInfo(@Valid @RequestBody UsersDTO usersDTO) {
        String userId = ThreadLocalContext.getUserId();
        return userHubClient.modifyPersonalInfo(usersDTO, userId);
    }
}
