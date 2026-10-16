package com.exambaby.qbank.service.code.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exambaby.common.core.domain.BusinessException;
import com.exambaby.common.core.dto.RedisOnlineUser;
import com.exambaby.common.core.dto.UsersDTO;
import com.exambaby.common.core.utils.JwtUtil;
import com.exambaby.common.core.utils.ThreadLocalContext;
import com.exambaby.qbank.service.code.entity.Users;
import com.exambaby.qbank.service.code.mapper.UsersMapper;
import com.exambaby.qbank.service.code.service.LoginService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.exambaby.common.core.utils.ClientTypeUtil.resolveClientType;


@Service
public class LoginServiceImpl implements LoginService {

    private final StringRedisTemplate stringRedisTemplate;
    private final UsersMapper usersMapper;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OnlineUserServiceImpl onlineUserService;
    public LoginServiceImpl(StringRedisTemplate stringRedisTemplate,
                            UsersMapper usersMapper,
                            JwtUtil jwtUtil,
                            RedisTemplate<String, Object> redisTemplate,
                            OnlineUserServiceImpl onlineUserService
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.usersMapper = usersMapper;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.onlineUserService = onlineUserService;

    }


    @Override
    public String login(UsersDTO usersDTO) {


//
        if (usersDTO.getUsername() == null || usersDTO.getPassword() == null) {
            throw new BusinessException("用户名或密码不能为空",444);
        }
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getUsername, usersDTO.getUsername());
        Users dbUser = usersMapper.selectOne(queryWrapper);


        // 判断用户是否存在
        if (dbUser == null) {
            throw new BusinessException("您输入的用户不存在",444);
        }
//        判断用户是否被锁定：0-未锁定，1-已锁定
        if (dbUser.getIsLocked() == 1) {
            throw new BusinessException("您的账号已被禁用",444);
        }
        // 校验密码
        if (!dbUser.getPassword().equals(usersDTO.getPassword())) {
            throw new BusinessException("您输入的密码错误",444);
        }


        //根据系统代码：用户id构建jwt token
        String dbUserId = String.valueOf(dbUser.getId());
        String token = jwtUtil.generateToken(dbUserId, List.of("admin", "user"),"questionBank");

        // Redis 保存用户在线信息
        String ip = ThreadLocalContext.getIp();
        String userAgent = ThreadLocalContext.getUserAgent();
        String clientType = resolveClientType(userAgent); // 可选：解析为 PC / Mobile

        RedisOnlineUser onlineUser = RedisOnlineUser.builder()
                .userId(dbUser.getId())
                .username(usersDTO.getUsername())
                .ip(ip)
                .userAgent(userAgent)
                .clientType(clientType)
                .token(token)
                .loginTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
        onlineUserService.save(onlineUser,"questionBank" ,60 * 60 * 24, TimeUnit.DAYS);

        // 返回token
        return token;
    }

    @Override
    public String resetPassword(UsersDTO usersDTO) {
        // 1. 根据用户名查询用户是否存在
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Users::getUsername, usersDTO.getUsername());
        Users dbUser = usersMapper.selectOne(queryWrapper);

        if (dbUser == null) {
            throw new BusinessException("用户不存在", 444);
        }

        // 2. 验证邮箱和手机号
        if (!dbUser.getEmail().equals(usersDTO.getEmail()) || !dbUser.getPhoneNumber().equals(usersDTO.getPhoneNumber())) {
            throw new BusinessException("邮箱或手机号不正确", 444);
        }

        // 3. 重置密码为12345678
        dbUser.setPassword("12345678");
        usersMapper.updateById(dbUser);

        return "密码重置成功";
    }

    @Override
    public String changePassword(String oldPassword, String newPassword) {
        String userId = ThreadLocalContext.getUserId();

        Users dbUser = usersMapper.selectById((Serializable) userId);
        if (dbUser == null) {
            throw new BusinessException("用户不存在", 444);
        }

        if (!dbUser.getPassword().equals(oldPassword)) {
            throw new BusinessException("原密码错误", 444);
        }

        dbUser.setPassword(newPassword);
        usersMapper.updateById(dbUser);

        return "密码修改成功";
    }

}
