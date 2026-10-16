package com.exambaby.qbank.service.code.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exambaby.common.core.domain.BusinessException;
import com.exambaby.qbank.service.code.dto.admin.AdminActivityAnswersDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicActivityAnswersDTO;
import com.exambaby.qbank.service.code.entity.ActivityAnswers;
import com.exambaby.qbank.service.code.entity.ActivityRecords;
import com.exambaby.qbank.service.code.mapper.ActivityAnswersMapper;
import com.exambaby.qbank.service.code.mapper.ActivityRecordsMapper;
import com.exambaby.qbank.service.code.mapstruct.ActivityAnswersMapStruct;
import com.exambaby.qbank.service.code.mapstruct.AdminActivityAnswersMapStruct;
import com.exambaby.qbank.service.code.service.ActivityAnswersService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 答题记录服务实现类
 */
@Service
@AllArgsConstructor
public class ActivityAnswersServiceImpl extends ServiceImpl<ActivityAnswersMapper, ActivityAnswers> implements ActivityAnswersService {

    private final ActivityAnswersMapStruct activityAnswersMapStruct;
    private final ActivityAnswersMapper activityAnswersMapper;
    private final AdminActivityAnswersMapStruct adminActivityAnswersMapStruct;
    private final ActivityRecordsMapper activityRecordsMapper;


    /**
     * 保存或更新答题记录
     * 使用 MyBatis-Plus 的 saveOrUpdate 方法，如果实体有主键则更新，无主键则插入
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateActivityAnswers(List<PublicActivityAnswersDTO> publicActivityAnswersDTO) {
        List<ActivityAnswers> entityList = activityAnswersMapStruct.toEntityList(publicActivityAnswersDTO);

        ActivityRecords activityRecord=activityRecordsMapper.selectById(entityList.get(0).getRecordId());
        // 遍历实体列表
        for (ActivityAnswers entity : entityList) {
            // 使用 questionId 和 recordId 构建查询条件
            LambdaQueryWrapper<ActivityAnswers> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ActivityAnswers::getQuestionId, entity.getQuestionId())
                    .eq(ActivityAnswers::getRecordId, entity.getRecordId());

            // 查询是否存在
            ActivityAnswers existing = this.getOne(queryWrapper, false);

            if (existing != null) {
                // 存在则更新
                entity.setId(existing.getId()); // 设置ID以便更新
                this.updateById(entity);
            } else {
                // 不存在则新增
                this.save(entity);
            }
        // 更新总分
        if (entity.getIsCorrect()==1){
            // 判断是否为考试类型：practice/exam
            if (activityRecord.getType().equals("exam")){
                activityRecord.setTotalScore(activityRecord.getTotalScore()+1);
            }else if (activityRecord.getType().equals("practice")){
                activityRecord.setUserScore(activityRecord.getUserScore()+1);
            }
        }
        }
        activityRecordsMapper.updateById(activityRecord);
    }

    /**
     * 根据记录ID查询答题记录列表
     * 使用 MyBatis-Plus 的 selectList 方法和 LambdaQueryWrapper 构建查询条件
     */
    @Override
    public List<PublicActivityAnswersDTO> listActivityAnswers(String recordId) {
        LambdaQueryWrapper<ActivityAnswers> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityAnswers::getRecordId, recordId)
                .orderByAsc(ActivityAnswers::getAnswerTime);

        List<ActivityAnswers> activityAnswersList = this.list(queryWrapper);
        return activityAnswersMapStruct.toDTOList(activityAnswersList);
    }

    /**
     * 根据记录ID查询公开的答题记录列表
     *
     * @param id 记录ID
     * @return 公开的答题记录列表
     */
    @Override
    public List<PublicActivityAnswersDTO> getPublicActivityRecordsDTOS(Long id) {
        LambdaQueryWrapper<ActivityAnswers> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityAnswers::getRecordId, id)
                .orderByAsc(ActivityAnswers::getAnswerTime);
        List<ActivityAnswers> activityAnswersList = this.list(queryWrapper);
        return activityAnswersMapStruct.toDTOList(activityAnswersList);
    }

    /**
     * 根据记录ID删除答题记录
     *
     * @param recordId 记录ID
     */
    @Transactional
    @Override
    public void deleteActivityAnswersByRecordId(String recordId) {
        LambdaQueryWrapper<ActivityAnswers> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityAnswers::getRecordId, recordId);
        this.remove(queryWrapper);
    }

    @Override
    public List<ActivityAnswers> getActivityAnswersList(LambdaQueryWrapper<ActivityAnswers> activityAnswersLambdaQueryWrapper) {
        return this.list(activityAnswersLambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminActivityAnswersDTO createAdminActivityAnswers(AdminActivityAnswersDTO dto) {
        // 转换DTO为实体
        ActivityAnswers entity = adminActivityAnswersMapStruct.toEntity(dto);
        // 保存实体
        this.save(entity);
        // 转换实体为DTO并返回
        return adminActivityAnswersMapStruct.toDTO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminActivityAnswersDTO updateAdminActivityAnswers(AdminActivityAnswersDTO dto) {
        // 检查记录是否存在
        ActivityAnswers existing = this.getById(dto.getId());
        if (existing == null) {
            throw new BusinessException("答题记录不存在，ID: " + dto.getId());
        }
        
        // 转换DTO为实体
        ActivityAnswers entity = adminActivityAnswersMapStruct.toEntity(dto);
        // 更新实体
        this.updateById(entity);
        // 转换实体为DTO并返回
        return adminActivityAnswersMapStruct.toDTO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdminActivityAnswers(Long id) {
        // 检查记录是否存在
        ActivityAnswers existing = this.getById(id);
        if (existing == null) {
            throw new RuntimeException("答题记录不存在，ID: " + id);
        }
        
        // 删除记录
        this.removeById(id);
    }

    @Override
    public AdminActivityAnswersDTO getAdminActivityAnswersById(Long id) {
        // 查询记录
        ActivityAnswers entity = this.getById(id);
        if (entity == null) {
            throw new RuntimeException("答题记录不存在，ID: " + id);
        }
        
        // 转换实体为DTO并返回
        return adminActivityAnswersMapStruct.toDTO(entity);
    }

    @Override
    public Page<AdminActivityAnswersDTO> pageAdminActivityAnswers(long current, long size, String recordId, Long questionId) {
        // 创建分页对象
        Page<ActivityAnswers> page = new Page<>(current, size);
        
        // 构建查询条件
        LambdaQueryWrapper<ActivityAnswers> queryWrapper = new LambdaQueryWrapper<>();
        if (recordId != null && !recordId.trim().isEmpty()) {
            queryWrapper.eq(ActivityAnswers::getRecordId, recordId);
        }
        if (questionId != null) {
            queryWrapper.eq(ActivityAnswers::getQuestionId, questionId);
        }
        
        // 按答题时间升序排序
        queryWrapper.orderByAsc(ActivityAnswers::getAnswerTime);
        
        // 执行分页查询
        Page<ActivityAnswers> entityPage = this.page(page, queryWrapper);
        
        // 转换为DTO分页
        Page<AdminActivityAnswersDTO> dtoPage = new Page<>(current, size);
        dtoPage.setRecords(adminActivityAnswersMapStruct.toDTOList(entityPage.getRecords()));
        dtoPage.setTotal(entityPage.getTotal());
        dtoPage.setSize(entityPage.getSize());
        dtoPage.setCurrent(entityPage.getCurrent());
        dtoPage.setPages(entityPage.getPages());
        
        return dtoPage;
    }

    @Override
    public List<AdminActivityAnswersDTO> getAllAdminActivityAnswers() {
        // 查询所有记录
        List<ActivityAnswers> entities = this.list();
        
        // 转换为DTO列表并返回
        return adminActivityAnswersMapStruct.toDTOList(entities);
    }



} 