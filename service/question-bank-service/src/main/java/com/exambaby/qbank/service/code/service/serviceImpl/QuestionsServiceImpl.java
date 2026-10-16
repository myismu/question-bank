package com.exambaby.qbank.service.code.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exambaby.common.core.domain.BusinessException;
import com.exambaby.qbank.service.code.dto.admin.AdminQuestionsDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionBanksDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionsDTO;
import com.exambaby.qbank.service.code.entity.QuestionBanks;
import com.exambaby.qbank.service.code.entity.Questions;
import com.exambaby.qbank.service.code.mapper.QuestionsMapper;
import com.exambaby.qbank.service.code.mapstruct.QuestionsMapStruct;
import com.exambaby.qbank.service.code.service.QuestionBanksService;
import com.exambaby.qbank.service.code.service.QuestionsService;
import com.exambaby.qbank.service.code.utils.QuestionBankCacheKeyUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class QuestionsServiceImpl extends ServiceImpl<QuestionsMapper, Questions> implements QuestionsService {
    private static final long QUESTION_DETAIL_CACHE_TTL_MINUTES = 30;
    private static final long QUESTION_BANK_LIST_CACHE_TTL_MINUTES = 30;

    private final QuestionsMapper questionsMapper;
    private final QuestionsMapStruct questionsMapStruct;
    private final QuestionBanksService questionBanksService;
    private final RedisTemplate<String, Object> redisTemplate;

    public QuestionsServiceImpl(QuestionsMapper questionsMapper, QuestionsMapStruct questionsMapStruct,
                                @Lazy QuestionBanksService questionBanksService,
                                RedisTemplate<String, Object> redisTemplate) {
        this.questionsMapper = questionsMapper;
        this.questionsMapStruct = questionsMapStruct;
        this.questionBanksService = questionBanksService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public AdminQuestionsDTO createQuestion(AdminQuestionsDTO dto) {
        Questions entity = questionsMapStruct.toEntityFromAdmin(dto);
        save(entity);
        evictQuestionBankCache(entity.getBankId());
        return questionsMapStruct.toAdminDTO(entity);
    }

    @Override
    public boolean updateQuestion(AdminQuestionsDTO dto) {
        Questions oldQuestion = getById(dto.getId());
        Questions entity = questionsMapStruct.toEntityFromAdmin(dto);
        boolean updated = updateById(entity);
        if (updated) {
            evictQuestionCaches(dto.getId(), oldQuestion != null ? oldQuestion.getBankId() : null);
            evictQuestionBankCache(entity.getBankId());
        }
        return updated;
    }

    @Override
    public boolean deleteQuestion(Long id) {
        Questions oldQuestion = getById(id);
        boolean removed = removeById(id);
        if (removed) {
            evictQuestionCaches(id, oldQuestion != null ? oldQuestion.getBankId() : null);
        }
        return removed;
    }

    @Override
    public AdminQuestionsDTO getQuestionById(Long id) {
        return questionsMapStruct.toAdminDTO(getById(id));
    }

    @Override
    public Page<AdminQuestionsDTO> pageQuestions(long current, long size, String title, Long bankId, String type, Integer difficulty) {
        LambdaQueryWrapper<Questions> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like(Questions::getTitle, title);
        }
        if (bankId != null) {
            wrapper.eq(Questions::getBankId, bankId);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Questions::getType, type);
        }
        if (difficulty != null) {
            wrapper.eq(Questions::getDifficulty, difficulty);
        }
        Page<Questions> page = new Page<>(current, size);
        Page<Questions> entityPage = questionsMapper.selectPage(page, wrapper);
        Page<AdminQuestionsDTO> dtoPage = new Page<>(current, size, entityPage.getTotal());
        dtoPage.setRecords(questionsMapStruct.toAdminDTOList(entityPage.getRecords()));
        return dtoPage;
    }

    @Override
    public List<AdminQuestionsDTO> getAllQuestions() {
        List<Questions> list = list();
        return questionsMapStruct.toAdminDTOList(list);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PublicQuestionsDTO> getQuestionsByBankId(Long bankId) {
        String cacheKey = QuestionBankCacheKeyUtils.questionBankListKey(bankId);// 根据id生成缓存键
        Object cached = redisTemplate.opsForValue().get(cacheKey);// 从缓存中获取数据
        if (cached instanceof List<?>) {// 判断缓存数据类型，如果缓存命中（存在且是 List 类型），直接返回缓存的数据
            return (List<PublicQuestionsDTO>) cached; // 返回缓存数据
        }
//        缓存未命中时，从数据库查询
        LambdaQueryWrapper<Questions> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Questions::getBankId, bankId);
        List<Questions> list = questionsMapper.selectList(wrapper);
        List<PublicQuestionsDTO> dtoList = questionsMapStruct.toPublicDTOList(list);
        // 将查询结果缓存到Redis中
        redisTemplate.opsForValue().set(cacheKey, dtoList, QUESTION_BANK_LIST_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return dtoList;
    }

    /**
     * 添加题目到题库中
     *
     * @param publicQuestionsDTO 题目信息
     */
    @Override
    public Boolean addQuestion(PublicQuestionsDTO publicQuestionsDTO) {
//        根据题库id查看题库是否存在
//        构建查询条件
        LambdaQueryWrapper<QuestionBanks> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBanks::getId, publicQuestionsDTO.getBankId());
        if (questionBanksService.isExistQuestionBank(wrapper)) {
            throw new BusinessException("您要添加的题库不存在", 444);
        }

//        根据题目id将题目添加到题库中
        Questions questions = questionsMapStruct.toEntityFromPublic(publicQuestionsDTO);
        int i = questionsMapper.insert(questions);
//        如果添加失败就抛出异常
        if (i == 0) {
            throw new BusinessException("添加题目失败", 444);
        }
        evictQuestionBankCache(publicQuestionsDTO.getBankId());
        return i > 0;
    }

    @Override
    public boolean PublicUpdateQuestion(PublicQuestionsDTO publicQuestionsDTO) {
        Questions oldQuestion = getById(publicQuestionsDTO.getId());
        Questions entity = questionsMapStruct.toEntityFromPublic(publicQuestionsDTO);
        boolean updated = updateById(entity);
        if (updated) {
            evictQuestionCaches(publicQuestionsDTO.getId(), oldQuestion != null ? oldQuestion.getBankId() : null);
            evictQuestionBankCache(entity.getBankId());
        }
        return updated;
    }

    @Override
    public Long selectQustionsCount(LambdaQueryWrapper<Questions> qw) {
        return questionsMapper.selectCount(qw);
    }

    @Override
    public void insertQuestions(Questions question) {
        questionsMapper.insert(question);
        evictQuestionBankCache(question.getBankId());
    }

    @Override
    public Page<Questions> selectPageQustions(Page<PublicQuestionBanksDTO> publicQuestionBanksDTOPage, LambdaQueryWrapper<Questions> qw) {

        return questionsMapper.selectPage(new Page<>(publicQuestionBanksDTOPage.getCurrent(), publicQuestionBanksDTOPage.getSize()), qw);
    }


    @Override
    public void deleteByBankId(Long bankId) {
        questionsMapper.delete(new LambdaQueryWrapper<Questions>().eq(Questions::getBankId, bankId));
        evictQuestionBankCache(bankId);
    }

    @Transactional
    @Override
    public boolean BatchAddQuestion(List<PublicQuestionsDTO> publicQuestionsDTOList) {
        boolean added = questionsMapper.insertBatch(questionsMapStruct.toEntityListFromPublic(publicQuestionsDTOList)) > 0;
        if (added && publicQuestionsDTOList != null && !publicQuestionsDTOList.isEmpty()) {
            evictQuestionBankCache(publicQuestionsDTOList.get(0).getBankId());
        }
        return added;
    }

    @Override
    public Page<PublicQuestionsDTO> getQuestionsByPage(Integer pageNum, Integer pageSize, Long bankId) {
//        构建查询条件
        LambdaQueryWrapper<Questions> wrapper = new LambdaQueryWrapper<>();
//        如果bankId不为空，则加入查询条件
        if (bankId != null) {
            wrapper.eq(Questions::getBankId, bankId);
        }

        //        调用分页查询方法
        Page<Questions> page = new Page<>(pageNum, pageSize);
        Page<Questions> entityPage = questionsMapper.selectPage(page, wrapper);
        //        转换为DTO
        Page<PublicQuestionsDTO> dtoPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        dtoPage.setRecords(questionsMapStruct.toPublicDTOList(entityPage.getRecords()));
        return dtoPage;
    }

    @Override
    public PublicQuestionsDTO getPublicQuestionById(Long id) {
        String cacheKey = QuestionBankCacheKeyUtils.questionDetailKey(id);
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof PublicQuestionsDTO publicQuestionsDTO) {
            return publicQuestionsDTO;
        }

        Questions entity = getById(id);
        if (entity == null) {
            return null;
        }
        PublicQuestionsDTO dto = questionsMapStruct.toPublicDTO(entity);
        redisTemplate.opsForValue().set(cacheKey, dto, QUESTION_DETAIL_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return dto;
    }

    //根据多个题库id和题目类型批量查询题目
    @Override
    public List<PublicQuestionsDTO> getQuestionsByBankIdAndType(List<Long> bankId, String type) {
//        构建查询条件
        LambdaQueryWrapper<Questions> wrapper = new LambdaQueryWrapper<>();

        if (type != null && !type.equals("")) {
            wrapper.eq(Questions::getType, type);
        }
        //        查询
        List<Questions> list = questionsMapper.selectList(wrapper);
        //        转换为DTO
        return questionsMapStruct.toPublicDTOList(list);
    }

    @Override
    public long count(LambdaQueryWrapper<Questions> questionWrapper) {
        return questionsMapper.selectCount(questionWrapper);
    }

    @Override
    public Map<Long, Long> countGroupByBankIds(List<Long> bankIds) {
        if (bankIds == null || bankIds.isEmpty()) {
            return Map.of();
        }
        List<Map<String, Object>> rows = questionsMapper.countByBankIds(bankIds);
        return rows.stream().collect(Collectors.toMap(
                row -> (Long) row.get("bank_id"),
                row -> (Long) row.get("count")
        ));
    }

    private void evictQuestionCaches(Long questionId, Long bankId) {
        evictQuestionCache(questionId);
        evictQuestionBankCache(bankId);
    }

    private void evictQuestionCache(Long questionId) {
        if (questionId != null) {
            redisTemplate.delete(QuestionBankCacheKeyUtils.questionDetailKey(questionId));
        }
    }

    private void evictQuestionBankCache(Long bankId) {
        if (bankId != null) {
            redisTemplate.delete(QuestionBankCacheKeyUtils.questionBankListKey(bankId));
        }
    }
}
