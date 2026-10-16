package com.exambaby.qbank.service.code.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.common.core.domain.BusinessException;
import com.exambaby.common.core.dto.UsersDTO;
import com.exambaby.qbank.service.code.entity.Users;
import com.exambaby.qbank.service.code.mapper.UsersMapper;
import com.exambaby.qbank.service.code.mapstruct.UsersMapStruct;
import com.exambaby.qbank.service.code.service.UsersService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// UsersServiceImpl 实现类
@Service
public class UsersServiceImpl implements UsersService {

    private final UsersMapper usersMapper;

    private final UsersMapStruct usersMapStruct;


    public UsersServiceImpl(UsersMapper usersMapper, UsersMapStruct usersMapStruct ) {
        this.usersMapper = usersMapper;
        this.usersMapStruct = usersMapStruct;
    }

    @Override
    public UsersDTO getUserById(Long id) {
        Users user = usersMapper.selectById(id);
        return usersMapStruct.toDTO(user); // 👈 使用 MapStruct 做转换
    }

    @Override
    public String saveUser(UsersDTO usersDTO) {
        // 检查用户名是否已存在
        if (StringUtils.hasText(usersDTO.getUsername())) {
            LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Users::getUsername, usersDTO.getUsername());
            Long count = usersMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("该用户名已被注册", 444);

            }
        }

        // 检查手机号是否已存在
        if (StringUtils.hasText(usersDTO.getPhoneNumber())) {
            LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Users::getPhoneNumber, usersDTO.getPhoneNumber());
            Long count = usersMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("该手机号已被注册", 444);
            }
        }
        
        // 检查邮箱是否已存在
        if (StringUtils.hasText(usersDTO.getEmail())) {
            LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Users::getEmail, usersDTO.getEmail());
            Long count = usersMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("该邮箱已被注册", 444);
            }
        }
        
        Users user = usersMapStruct.toEntity(usersDTO);
        usersMapper.insert(user);
        return "创建用户成功";
    }

    @Override
    public List<UsersDTO> getAllUsers() {
        List<Users> usersList = usersMapper.selectList(null);
        return usersMapStruct.toDTOList(usersList); // 👈 批量 DTO 转换
    }

    @Override
    public Page<UsersDTO> getUsersByPage(Page<UsersDTO> page, String username, String email, String phoneNumber, Integer isLocked) {
        // 创建查询条件
        LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加模糊查询条件
        if (StringUtils.hasText(username)) {
            queryWrapper.like(Users::getUsername, username);
        }
        if (StringUtils.hasText(email)) {
            queryWrapper.like(Users::getEmail, email);
        }
        if (StringUtils.hasText(phoneNumber)) {
            queryWrapper.like(Users::getPhoneNumber, phoneNumber);
        }
        if (isLocked != null) {
            queryWrapper.eq(Users::getIsLocked, isLocked);
        }

        // 执行分页查询
        Page<Users> usersPage = usersMapper.selectPage(new Page<>(page.getCurrent(), page.getSize()), queryWrapper);
        
        // 转换为DTO
        Page<UsersDTO> dtoPage = new Page<>(usersPage.getCurrent(), usersPage.getSize(), usersPage.getTotal());
        dtoPage.setRecords(usersMapStruct.toDTOList(usersPage.getRecords()));
        
        return dtoPage;
    }

    @Override
    public Map<String, Object> getUserInfo(String userId) {
        List<String> nameList = new ArrayList<>();
        Map<String, Object> map=new HashMap<>();
//        查询用户信息
        Users dbUser = usersMapper.selectById(userId);
//        查询角色信息
        nameList.add("admin");
//        添加角色信息
        map.put("username",dbUser.getUsername());
        map.put("roles",nameList);
        return map;
    }

    @Override
    public String updateUser(UsersDTO usersDTO) {
        // 检查用户名是否已被其他用户使用
        if (StringUtils.hasText(usersDTO.getUsername())) {
            LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Users::getUsername, usersDTO.getUsername())
                    .ne(Users::getId, usersDTO.getId()); // 排除当前用户
            Long count = usersMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("该用户名已被其他用户注册", 444);
            }
        }

        // 检查手机号是否已被其他用户使用
        if (StringUtils.hasText(usersDTO.getPhoneNumber())) {
            LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Users::getPhoneNumber, usersDTO.getPhoneNumber())
                    .ne(Users::getId, usersDTO.getId()); // 排除当前用户
            Long count = usersMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("该手机号已被其他用户注册", 444);
            }
        }
        
        // 检查邮箱是否已被其他用户使用
        if (StringUtils.hasText(usersDTO.getEmail())) {
            LambdaQueryWrapper<Users> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Users::getEmail, usersDTO.getEmail())
                    .ne(Users::getId, usersDTO.getId()); // 排除当前用户
            Long count = usersMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("该邮箱已被其他用户注册", 444);
            }
        }
        
        Users user = usersMapStruct.toEntity(usersDTO);
        usersMapper.updateById(user);
        return "更新用户成功";
    }

    @Override
    public void deleteUser(Long id) {
        usersMapper.deleteById(id);
    }

}
