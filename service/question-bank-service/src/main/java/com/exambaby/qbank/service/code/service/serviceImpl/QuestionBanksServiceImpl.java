package com.exambaby.qbank.service.code.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exambaby.api.userhub.client.UserHubClient;
import com.exambaby.common.core.domain.ApiResponse;
import com.exambaby.common.core.domain.BusinessException;
import com.exambaby.common.core.utils.ThreadLocalContext;
import com.exambaby.qbank.service.code.dto.admin.AdminQuestionBanksDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionBanksDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionChaptersDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionsDTO;
import com.exambaby.qbank.service.code.entity.QuestionBanks;
import com.exambaby.qbank.service.code.entity.Questions;
import com.exambaby.qbank.service.code.entity.UserQuestionBanks;
import com.exambaby.qbank.service.code.mapper.QuestionBanksMapper;
import com.exambaby.qbank.service.code.mapstruct.QuestionBanksMapStruct;
import com.exambaby.qbank.service.code.mapstruct.QuestionsMapStruct;
import com.exambaby.qbank.service.code.service.QuestionBanksService;
import com.exambaby.qbank.service.code.service.QuestionChaptersService;
import com.exambaby.qbank.service.code.service.QuestionsService;
import com.exambaby.qbank.service.code.service.UserQuestionBanksService;
import com.exambaby.qbank.service.code.utils.QuestionBankCacheKeyUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class QuestionBanksServiceImpl extends ServiceImpl<QuestionBanksMapper, QuestionBanks> implements QuestionBanksService {
    private static final long ALL_PUBLIC_QUESTION_BANKS_CACHE_TTL_MINUTES = 10;

    private final QuestionBanksMapper questionBanksMapper;
    private final QuestionBanksMapStruct questionBanksMapStruct;
    private final QuestionsService questionsService;
    private final QuestionsMapStruct questionsMapStruct;
    private final UserQuestionBanksService userQuestionBanksService;
    private final QuestionChaptersService questionChaptersService;
    private final UserHubClient userHubClient;
    private final RedisTemplate<String, Object> redisTemplate;

    public QuestionBanksServiceImpl(QuestionBanksMapper questionBanksMapper, QuestionBanksMapStruct questionBanksMapStruct, QuestionsService questionsService, QuestionsMapStruct questionsMapStruct, UserQuestionBanksService userQuestionBanksService, QuestionChaptersService questionChaptersService, UserHubClient userHubClient, RedisTemplate<String, Object> redisTemplate) {
        this.questionBanksMapper = questionBanksMapper;
        this.questionBanksMapStruct = questionBanksMapStruct;
        this.questionsService = questionsService;
        this.questionsMapStruct = questionsMapStruct;
        this.userQuestionBanksService = userQuestionBanksService;
        this.questionChaptersService = questionChaptersService;
        this.userHubClient = userHubClient;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public AdminQuestionBanksDTO createQuestionBank(AdminQuestionBanksDTO dto) {
        QuestionBanks entity = questionBanksMapStruct.toEntityFromAdmin(dto);
        save(entity);
        evictAllPublicQuestionBanksCache();
        return questionBanksMapStruct.toAdminDTO(entity);
    }

    @Override
    public AdminQuestionBanksDTO updateQuestionBank(AdminQuestionBanksDTO dto) {
        QuestionBanks entity = questionBanksMapStruct.toEntityFromAdmin(dto);
        updateById(entity);
        evictAllPublicQuestionBanksCache();
        return questionBanksMapStruct.toAdminDTO(entity);
    }

    @Override
    public void deleteQuestionBank(Long id) {
        removeById(id);
        evictAllPublicQuestionBanksCache();
    }

    @Override
    public AdminQuestionBanksDTO getQuestionBankById(Long id) {
        QuestionBanks entity = getById(id);
        return questionBanksMapStruct.toAdminDTO(entity);
    }

    @Override
    public Page<AdminQuestionBanksDTO> pageQuestionBanks(long current, long size, String name, Long userId, String type, Integer difficulty) {
        LambdaQueryWrapper<QuestionBanks> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(QuestionBanks::getName, name);
        }
        if (userId != null) {
            wrapper.eq(QuestionBanks::getUserId, userId);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(QuestionBanks::getType, type);
        }
        if (difficulty != null) {
            wrapper.eq(QuestionBanks::getDifficulty, difficulty);
        }
        Page<QuestionBanks> page = new Page<>(current, size);
        Page<QuestionBanks> entityPage = questionBanksMapper.selectPage(page, wrapper);
        Page<AdminQuestionBanksDTO> dtoPage = new Page<>(current, size, entityPage.getTotal());
        dtoPage.setRecords(questionBanksMapStruct.toAdminDTOList(entityPage.getRecords()));
        return dtoPage;
    }

    @Override
    public List<AdminQuestionBanksDTO> getAllQuestionBanks() {
        List<QuestionBanks> list = list();
        return questionBanksMapStruct.toAdminDTOList(list);
    }

    /**
     * 查询所有公开题库
     *
     * @return 题库列表
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<PublicQuestionBanksDTO> getAllPublicQuestionBanks() {
        Object cached = redisTemplate.opsForValue().get(QuestionBankCacheKeyUtils.ALL_PUBLIC_QUESTION_BANKS_KEY);
        if (cached instanceof List<?>) {
            return (List<PublicQuestionBanksDTO>) cached;
        }

        // 1. 构建查询条件
        LambdaQueryWrapper<QuestionBanks> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBanks::getType, "public");
//        2. 查询题库列表
        List<PublicQuestionBanksDTO> banks = getPublicQuestionBanksDTOS(wrapper);
        redisTemplate.opsForValue().set(
                QuestionBankCacheKeyUtils.ALL_PUBLIC_QUESTION_BANKS_KEY,
                banks,
                ALL_PUBLIC_QUESTION_BANKS_CACHE_TTL_MINUTES,
                TimeUnit.MINUTES
        );
        return banks;
    }

    /**
     * 根据题库ID查询公开题库
     *
     * @param questionBankId 题库ID
     * @return 题库DTO
     */
    @Transactional
    @Override
    public PublicQuestionBanksDTO getPublicQuestionBankById(Long questionBankId) {
        // 1. 查询题库表，获取题库信息
        LambdaQueryWrapper<QuestionBanks> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBanks::getId, questionBankId);
        QuestionBanks entity = questionBanksMapper.selectOne(wrapper);
//        2. 查询用户题库关联表，判断是否收藏了该题库
        String userId = ThreadLocalContext.getUserId();
//        判断userId是否是字符串

        LambdaQueryWrapper<UserQuestionBanks> uqw = new LambdaQueryWrapper<>();
        uqw.eq(UserQuestionBanks::getUserId, userId);
        uqw.eq(UserQuestionBanks::getQuestionBanksId, questionBankId);
        UserQuestionBanks userQuestionBanks = userQuestionBanksService.selectOnePublicQuestionBank(uqw);
        entity.setIsCollected(userQuestionBanks != null);

//        3. 根据题库ID查询题目长度
        LambdaQueryWrapper<Questions> qw = new LambdaQueryWrapper<>();
        qw.eq(Questions::getBankId, questionBankId);
        Long count = questionsService.selectQustionsCount(qw);
        entity.setQuestionCount(Math.toIntExact(count));
        // 4. 设置用户名
        PublicQuestionBanksDTO publicQuestionBanksDTO = questionBanksMapStruct.toPublicDTO(entity);
        ApiResponse<String> userName = userHubClient.getUserNameById(publicQuestionBanksDTO.getUserId());
        if (userName != null && userName.getData() != null) {
            publicQuestionBanksDTO.setUserName(userName.getData());
        }

        // 5. 返回题库DTO
        return publicQuestionBanksDTO;
    }

    /**
     * 根据用户ID查询收藏的题库列表（不需要判断是否收藏了）
     *
     * @param userId 用户ID
     * @return 题库列表
     */
    @Transactional
    @Override
    public List<PublicQuestionBanksDTO> getQuestionBanksByUserId(Long userId) {
        // 1. 查询用户管理题库表，获取题库ID列表
        LambdaQueryWrapper<UserQuestionBanks> userQuestionBanksWrapper = new LambdaQueryWrapper<>();
        userQuestionBanksWrapper.eq(UserQuestionBanks::getUserId, userId);
        List<UserQuestionBanks> userBanks = userQuestionBanksService.selectListPublic(userQuestionBanksWrapper);
        if (userBanks == null || userBanks.isEmpty()) {//如果没有收藏的题库，则返回空列表
            return List.of();
        }
//
        List<Long> bankIds = userBanks.stream().map(UserQuestionBanks::getQuestionBanksId).collect(Collectors.toList());
        // 2. 查询题库表，获取题库列表
        LambdaQueryWrapper<QuestionBanks> qbWrapper = new LambdaQueryWrapper<>();
        qbWrapper.in(QuestionBanks::getId, bankIds);
//        3.根据id列表查询题库列表
        return getPublicQuestionBanksDTOS(qbWrapper);
    }


    /**
     * 根据条件查询公开题库列表（重复代码段，因此提取出来）
     *
     * @param qbWrapper 条件构造器
     * @return 题库列表
     */
    private List<PublicQuestionBanksDTO> getPublicQuestionBanksDTOS(LambdaQueryWrapper<QuestionBanks> qbWrapper) {
//        1. 查询题库列表
        List<PublicQuestionBanksDTO> banks = questionBanksMapStruct.toPublicDTOList(questionBanksMapper.selectList(qbWrapper));
        if (banks.isEmpty()) {
            return banks;
        }
//        2. 批量查询题目数量
        List<Long> bankIds = banks.stream().map(PublicQuestionBanksDTO::getId).collect(Collectors.toList());
        Map<Long, Long> countMap = questionsService.countGroupByBankIds(bankIds);
//        3. 批量查询用户名
        List<Long> userIds = banks.stream().map(PublicQuestionBanksDTO::getUserId).distinct().collect(Collectors.toList());
        ApiResponse<Map<Long, String>> userNameResp = userHubClient.getUserNamesByIds(userIds);
        Map<Long, String> userNameMap = userNameResp != null && userNameResp.getData() != null
                ? userNameResp.getData() : Map.of();
//        4. 设置题目数量和用户名
        for (PublicQuestionBanksDTO bank : banks) {
            bank.setQuestionCount(countMap.getOrDefault(bank.getId(), 0L).intValue());
            String userName = userNameMap.get(bank.getUserId());
            if (userName != null) {
                bank.setUserName(userName);
            }
        }
        return banks;
    }

    /**
     * 根据题库ID查询题目列表
     *
     * @param publicQuestionBanksDTO 题库DTO
     * @return 题目列表
     */
    @Transactional
    @Override
    public String publicCreateQuestionBank(PublicQuestionBanksDTO publicQuestionBanksDTO) {
        String userId = ThreadLocalContext.getUserId();
        publicQuestionBanksDTO.setUserId(Long.valueOf(userId));
        // 1. 新增题库
        QuestionBanks questionBank = questionBanksMapStruct.toEntityFromPublic(publicQuestionBanksDTO);
        questionBanksMapper.insert(questionBank);
//        2. 新增章节信息,返回章节id(如果为空则不创建章节信息)
        Long questionChapterId = null;
        if (publicQuestionBanksDTO.getChaptersDTO() != null) {
            PublicQuestionChaptersDTO chaptersDTO = publicQuestionBanksDTO.getChaptersDTO();
            chaptersDTO.setBankId(questionBank.getId());
            questionChapterId = questionChaptersService.createQuestionChapters(chaptersDTO);
        }
        // 2. 新增题目
        if (publicQuestionBanksDTO.getQuestionsDTOList() != null && !publicQuestionBanksDTO.getQuestionsDTOList().isEmpty()) {
            for (PublicQuestionsDTO questionDTO : publicQuestionBanksDTO.getQuestionsDTOList()) {
                Questions question = questionsMapStruct.toEntityFromPublic(questionDTO);
                question.setBankId(questionBank.getId()); // 关联题库ID
                question.setChapterId(questionChapterId); // 关联章节ID
                questionsService.insertQuestions(question);
            }
        }
//        3.添加用户表和题库表关联信息
        UserQuestionBanks userQuestionBanks = new UserQuestionBanks();
        userQuestionBanks.setUserId(Long.valueOf(userId));
        userQuestionBanks.setQuestionBanksId(questionBank.getId());
        if (userQuestionBanksService.savea(userQuestionBanks) == 0) {
            throw new BusinessException("题库添加失败", 444);
        }
//        4. 返回成功信息
        evictAllPublicQuestionBanksCache();
        return "success";
    }

    /**
     * 根据题库ID查询题库信息和题目信息
     *
     * @param bankId 题库ID
     * @return 题库DTO
     */
    @Transactional
    @Override
    public Page<PublicQuestionBanksDTO> getQuestionBankByIdAndAllQuestions(Page<PublicQuestionBanksDTO> page, Long bankId, String title, String type, String analysis, Integer difficulty) {
//        1. 根据题库ID查询题库信息
        LambdaQueryWrapper<QuestionBanks> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBanks::getId, bankId);
        QuestionBanks entity = questionBanksMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException("题库不存在", 444);
        }
//        2. 构建查询条件，根据题库ID查询题目信息
        LambdaQueryWrapper<Questions> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(title)) {
            qw.like(Questions::getTitle, title);
        }
        if (StringUtils.hasText(type)) {
            qw.eq(Questions::getType, type);
        }
        if (StringUtils.hasText(analysis)) {
            qw.like(Questions::getAnalysis, analysis);
        }
        if (difficulty != null) {
            qw.eq(Questions::getDifficulty, difficulty);
        }

        qw.eq(Questions::getBankId, bankId);
        Page<Questions> questionsPage = questionsService.selectPageQustions(new Page<>(page.getCurrent(), page.getSize()), qw);
//        3. 将题库信息转换为题库DTO
        PublicQuestionBanksDTO publicQuestionBanksDTO = questionBanksMapStruct.toPublicDTO(entity);
//        4. 将题目信息转换为题目DTO列表
        List<PublicQuestionsDTO> questionsDTOList = questionsMapStruct.toPublicDTOList(questionsPage.getRecords());
//        5. 将题目DTO列表添加到题库DTO中
        publicQuestionBanksDTO.setQuestionsDTOList(questionsDTOList);
//        5. 构建分页对象
        Page<PublicQuestionBanksDTO> resultPage = new Page<>(page.getCurrent(), page.getSize(), questionsPage.getTotal());
        resultPage.setRecords(List.of(publicQuestionBanksDTO));
        //5. 返回题库DTO
        return resultPage;
    }

    /**
     * 根据题库ID查询题库信息是否存在
     *
     * @param wrapper 条件构造器
     * @return true：不存在，false：存在
     */
    @Override
    public boolean isExistQuestionBank(LambdaQueryWrapper<QuestionBanks> wrapper) {
        return questionBanksMapper.selectOne(wrapper) == null;
    }

    @Transactional
    @Override
    public boolean deleteQuestionBankById(Long bankId) {
//        1、删除用户关联题库表

        userQuestionBanksService.deleteByBankId(bankId);
//        2、根据题库id删除题目表
        questionsService.deleteByBankId(bankId);
//        3. 根据bankId删除章节表
        questionChaptersService.deleteByBankId(bankId);
//        4、删除题库表
        if (!removeById(bankId)) {
            throw new BusinessException("题库删除失败-1", 444);
        }
        evictAllPublicQuestionBanksCache();
        return true;
    }

    /**
     * 根据用户ID查询创建的题库列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<PublicQuestionBanksDTO> getQuestionBanksByCreatorId(Long userId) {
//        1. 根据用户id直接查询题库表
        LambdaQueryWrapper<QuestionBanks> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBanks::getUserId, userId);
        List<PublicQuestionBanksDTO> banks = questionBanksMapStruct.toPublicDTOList(questionBanksMapper.selectList(wrapper));
        if (banks.isEmpty()) {
            return banks;
        }
//        2. 批量查询题目数量
        List<Long> bankIds = banks.stream().map(PublicQuestionBanksDTO::getId).collect(Collectors.toList());
        Map<Long, Long> countMap = questionsService.countGroupByBankIds(bankIds);
//        3. 批量查询收藏状态
        LambdaQueryWrapper<UserQuestionBanks> uqw = new LambdaQueryWrapper<>();
        uqw.eq(UserQuestionBanks::getUserId, userId);
        uqw.in(UserQuestionBanks::getQuestionBanksId, bankIds);
        List<UserQuestionBanks> collectedBanks = userQuestionBanksService.selectListPublic(uqw);
        Set<Long> collectedBankIds = collectedBanks.stream()
                .map(UserQuestionBanks::getQuestionBanksId)
                .collect(Collectors.toSet());
//        4. 设置题目数量和是否收藏
        for (PublicQuestionBanksDTO bank : banks) {
            bank.setQuestionCount(countMap.getOrDefault(bank.getId(), 0L).intValue());
            bank.setIsCollected(collectedBankIds.contains(bank.getId()));
        }
        return banks;
    }

    private void evictAllPublicQuestionBanksCache() {
        redisTemplate.delete(QuestionBankCacheKeyUtils.ALL_PUBLIC_QUESTION_BANKS_KEY);
    }

    @Override
    public void PublicUpdateQuestionBank(PublicQuestionBanksDTO publicQuestionBanksDTO) {
//        新题库表
        QuestionBanks questionBank = questionBanksMapStruct.toEntityFromPublic(publicQuestionBanksDTO);
        if (questionBanksMapper.updateById(questionBank) == 0) {
            throw new BusinessException("题库更新失败", 444);
        }
        // 更新后清除公开题库列表缓存，避免首页继续读到旧封面
        evictAllPublicQuestionBanksCache();
    }
}