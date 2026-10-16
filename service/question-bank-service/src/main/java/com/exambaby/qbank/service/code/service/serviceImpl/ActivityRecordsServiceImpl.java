package com.exambaby.qbank.service.code.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.common.core.domain.BusinessException;
import com.exambaby.qbank.service.code.dto.AnswerDTO;
import com.exambaby.qbank.service.code.dto.admin.AdminActivityRecordsDTO;
import com.exambaby.qbank.service.code.dto.publics.*;
import com.exambaby.qbank.service.code.entity.*;
import com.exambaby.qbank.service.code.mapper.ActivityRecordsMapper;
import com.exambaby.qbank.service.code.mapper.QuestionBanksMapper;
import com.exambaby.qbank.service.code.mapstruct.ActivityRecordsMapStruct;
import com.exambaby.qbank.service.code.mapstruct.AdminActivityRecordsMapStruct;
import com.exambaby.qbank.service.code.service.ActivityAnswersService;
import com.exambaby.qbank.service.code.service.ActivityRecordsService;
import com.exambaby.qbank.service.code.utils.ExamRedisKeyUtils;
import com.exambaby.qbank.service.code.service.QuestionsService;
import com.exambaby.qbank.service.code.service.WrongQuestionsService;
import lombok.AllArgsConstructor;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ActivityRecordsServiceImpl implements ActivityRecordsService {

    private final ActivityRecordsMapper activityRecordsMapper;
    private final ActivityRecordsMapStruct activityRecordsMapStruct;
    private final AdminActivityRecordsMapStruct adminActivityRecordsMapStruct;
    private final ActivityAnswersService activityAnswersService;
    private final RedissonClient redissonClient;
    private final QuestionsService questionsService;
    private final WrongQuestionsService wrongQuestionsService;
//    questionBanksMapper
    private final QuestionBanksMapper questionBanksMapper;



    @Override
    @Transactional
    public PublicActivityRecordsDTO createActivityRecord(PublicActivityRecordsDTO publicActivityRecordsDTO) {
        ActivityRecords activityRecords = activityRecordsMapStruct.toEntity(publicActivityRecordsDTO);
        // 设置默认值
        activityRecords.setStatus("in_progress");
        activityRecords.setStartTime(LocalDateTime.now());
        // 使用 MyBatis-Plus 的 insert 方法
        activityRecordsMapper.insert(activityRecords);
        return activityRecordsMapStruct.toDTO(activityRecords);
    }

    /**
     * 结束活动记录
     *
     * @param publicActivityRecordsDTO 活动记录DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishActivityRecord(PublicActivityRecordsDTO publicActivityRecordsDTO) {
        ActivityRecords activityRecords = activityRecordsMapStruct.toEntity(publicActivityRecordsDTO);
//        先判断活动是否还在进行中，如果还在进行中，则更新状态为completed，否则旧不处理
        ActivityRecords activityRecords1 = activityRecordsMapper.selectById(activityRecords.getId());
        if (!activityRecords1.getStatus().equals("in_progress")) {
            return;//提前退出，避免后续操作
        }
        activityRecords.setEndTime(LocalDateTime.now());
        activityRecordsMapper.updateById(activityRecords);
// ① 批量查询已存在的错题
        LambdaQueryWrapper<ActivityAnswers> activityAnswersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        activityAnswersLambdaQueryWrapper.eq(ActivityAnswers::getRecordId, activityRecords.getId())
                .eq(ActivityAnswers::getIsCorrect, 0); // 查询isCorrect为0的记录（错题）
        List<ActivityAnswers> wrongList = activityAnswersService.getActivityAnswersList(activityAnswersLambdaQueryWrapper);

// ② 检查是否有错题
        if (wrongList == null || wrongList.isEmpty()) {
            return; // 提前退出，避免后续操作
        }

// ③ 提取 questionId 列表
        Long userId = publicActivityRecordsDTO.getUserId();
        List<Long> questionIdList = wrongList.stream()
                .map(ActivityAnswers::getQuestionId)
                .collect(Collectors.toList());

        if (questionIdList.isEmpty()) {
            return; // 提前退出
        }

// 查询已存在的错题
        List<WrongQuestions> existList = wrongQuestionsService.list(
                new LambdaQueryWrapper<WrongQuestions>()
                        .eq(WrongQuestions::getUserId, userId)
                        .in(WrongQuestions::getQuestionId, questionIdList)
        );

// ④ 分类已存在的错题和新错题
        Set<Long> existQuestionIdSet = existList.stream()// 已存在的错题id集合
                .map(WrongQuestions::getQuestionId)
                .collect(Collectors.toSet());// 取出questionId

        List<Long> newQuestionIdList = questionIdList.stream()// 新错题id集合
                .filter(qid -> !existQuestionIdSet.contains(qid))
                .toList();
        List<WrongQuestions> updateList = existList.stream()// 旧的错题id集合
                .filter(q -> existQuestionIdSet.contains(q.getQuestionId()))
                .collect(Collectors.toList());

// ⑤ 批量插入新错题
        if (!newQuestionIdList.isEmpty()) {
            List<WrongQuestions> newWrongQuestions = newQuestionIdList.stream()
                    .map(qid -> {
                        WrongQuestions wq = new WrongQuestions();
                        wq.setUserId(userId); // 用户id
                        wq.setQuestionId(qid); // 题目id
                        wq.setErrorCount(1);// 错误次数，第一次错题为1
                        wq.setStatus("pending");// 状态，默认为pending：待复习
                        wq.setBankId(activityRecords.getBankId());// 题库id
                        return wq;
                    })
                    .collect(Collectors.toList());

            wrongQuestionsService.saveBatchWrongQuestions(newWrongQuestions);
        }

// ⑥ 批量更新已存在错题
        if (!updateList.isEmpty()) {
            for (WrongQuestions question : updateList) {
                question.setErrorCount(question.getErrorCount() + 1); // 更新错误次数
                question.setStatus("pending");
            }
            wrongQuestionsService.batchUpdateErrorCount(updateList);
        }
    }

    /**
     * 根据用户id、题库id、类型查询活动记录
     *
     * @param userId 用户id
     * @param bankId 题库id
     * @param type   类型
     * @return 活动记录列表
     */
    @Override
    public PublicActivityRecordsDTO ActivityRecords(String userId, Long bankId, String type) {
        //1. 使用 LambdaQueryWrapper 构建查询条件
        LambdaQueryWrapper<ActivityRecords> queryWrapper = new LambdaQueryWrapper<>();
        /**
         * 查询条件：
         * 1. 用户id
         * 2. 题库id
         * 3. 类型
         * 4. 状态为进行中
         * 5. 按照开始时间倒序排序
         */
        queryWrapper.eq(ActivityRecords::getUserId, userId)
                .eq(ActivityRecords::getType, type)
                .eq(ActivityRecords::getBankId, bankId)
                .eq(ActivityRecords::getStatus, "in_progress")
                .orderByDesc(ActivityRecords::getStartTime);
//        2. 使用 MyBatis-Plus 的 selectList 方法查询
        ActivityRecords activityRecords = activityRecordsMapper.selectOne(queryWrapper);
        PublicActivityRecordsDTO publicActivityRecordsDTO = activityRecordsMapStruct.toDTO(activityRecords);
//        3. 如果活动记录不为空，则根据id查询对应的答题记录表信息
        if (publicActivityRecordsDTO != null) {
            List<PublicActivityAnswersDTO> publicActivityAnswersDTOS = activityAnswersService.getPublicActivityRecordsDTOS(publicActivityRecordsDTO.getId());
            publicActivityRecordsDTO.setActivityAnswersList(publicActivityAnswersDTOS);
        }
        return publicActivityRecordsDTO;
    }

    /**
     * 自动交卷、批改
     *
     * @param recordId 记录ID
     * @param status   状态
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reviewActivityRecord(Long recordId, String status) {
        /**
         * 根据recordId查询活动记录
         * 根据活动记录里的题库id查询出对应题库里的题目及其正确答案
         * 根据recordId查询用户的答题记录
         * 比对用户答案和正确答案，计算isCorrect值
         * 更新答题记录的isCorrect字段和分数，并将状态更新status
         */
//        状态：in_progress/completed/timeout/abandoned */
        if (!status.equals("in_progress") && !status.equals("completed") && !status.equals("timeout") && !status.equals("abandoned")) {
            throw new BusinessException("状态信息错误", 444);
        }
//        1. 根据recordId查询活动记录
        ActivityRecords activityRecords = activityRecordsMapper.selectById(recordId);
        if (activityRecords == null) {
            throw new BusinessException("该记录不存在", 444);
        }
//        3. 根据recordId查询用户的答题记录
        List<AnswerDTO> answerDTOS = activityRecordsMapper.selectUserAnswerAndQuestionInfoByRecordId(recordId);
//        4. 比对用户答案和正确答案，计算isCorrect值 (1: 正确, 0: 错误)
        List<Long> ids = new ArrayList<>();//用于存储答对的记录id，方便修改答题记录
        int numCorrect = 0;//用于统计正确的题目数，后续设置到活动记录表中
        for (AnswerDTO answerDTO : answerDTOS) {
            int i = judgeAnswer(answerDTO);
//            如果答案正确，则将答案DTO添加到correctAnswerDTOS中，并将isCorrect设置为1
            if (i == 1) {
                answerDTO.setIsCorrect(1);
                numCorrect++;
                ids.add(answerDTO.getId());
            }
        }
//        5. 更新答题记录的isCorrect字段和分数，并将状态更新status
        activityRecordsMapper.batchUpdateTypeByIds(1, ids);//更新所有答题记录的isCorrect字段设置为1
        activityRecords.setUserScore(numCorrect);
        activityRecords.setStatus(status);
//        设置结束时间
        activityRecords.setEndTime(LocalDateTime.now());

        if (activityRecordsMapper.updateById(activityRecords) == 0) {
            throw new BusinessException("交卷批改失败", 444);
        }
    }

    @Override
    public String registerAutoSubmitTask(Long recordId, Long durationInSeconds) {
//        根据recordId查询活动记录，判断是否在i=in_progress
        LambdaQueryWrapper<ActivityRecords> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityRecords::getId, recordId)
                .eq(ActivityRecords::getStatus, "in_progress");
        ActivityRecords activityRecords = activityRecordsMapper.selectOne(queryWrapper);
        if (activityRecords == null) {
            throw new BusinessException("该记录不存在或状态不为in_progress", 444);
        }
        String lockKey = ExamRedisKeyUtils.buildExamLockKey(recordId);
// 尝试设置一个带过期时间的 key，如果已经存在就不再设置
        boolean isNewTask = redissonClient.getBucket(lockKey)
                .trySet("1", durationInSeconds, TimeUnit.SECONDS);

        if (isNewTask) {
            // 说明是第一次添加该任务，可以继续注册延时任务
            RQueue<String> queue = redissonClient.getQueue(ExamRedisKeyUtils.EXAM_QUEUE_KEY);
            RDelayedQueue<String> delayedQueue = redissonClient.getDelayedQueue(queue);
            delayedQueue.offer(String.valueOf(recordId), durationInSeconds, TimeUnit.SECONDS);

            return "设置自动交卷任务：" + recordId + "，将在 " + durationInSeconds + " 秒后触发";
        } else {
            // 已经添加过任务，直接跳过
            return "任务已存在，跳过重复注册：" + recordId;
        }

    }

    @Override
    public List<AnswerDTO> selectUserAnswerAndQuestionInfoByRecordId(Long recordId) {
        return activityRecordsMapper.selectUserAnswerAndQuestionInfoByRecordId(recordId);
    }

    @Override
    public void updateActivityRecord(PublicActivityRecordsDTO publicActivityRecordsDTO) {
        ActivityRecords activityRecords = activityRecordsMapStruct.toEntity(publicActivityRecordsDTO);
        activityRecordsMapper.updateById(activityRecords);
    }

    @Override
    @Transactional
    public AdminActivityRecordsDTO createAdminActivityRecords(AdminActivityRecordsDTO dto) {
        ActivityRecords entity = adminActivityRecordsMapStruct.toEntity(dto);
        activityRecordsMapper.insert(entity);
        return adminActivityRecordsMapStruct.toDTO(entity);
    }

    @Override
    @Transactional
    public AdminActivityRecordsDTO updateAdminActivityRecords(AdminActivityRecordsDTO dto) {
        ActivityRecords existingEntity = activityRecordsMapper.selectById(dto.getId());
        if (existingEntity == null) {
            throw new BusinessException("活动记录不存在", 404);
        }
        ActivityRecords entity = adminActivityRecordsMapStruct.toEntity(dto);
        activityRecordsMapper.updateById(entity);
        return adminActivityRecordsMapStruct.toDTO(entity);
    }

    @Override
    @Transactional
    public void deleteAdminActivityRecords(Long id) {
        ActivityRecords existingEntity = activityRecordsMapper.selectById(id);
        if (existingEntity == null) {
            throw new BusinessException("活动记录不存在", 404);
        }
        activityRecordsMapper.deleteById(id);
    }

    @Override
    public AdminActivityRecordsDTO getAdminActivityRecordsById(Long id) {
        ActivityRecords entity = activityRecordsMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("活动记录不存在", 404);
        }
        return adminActivityRecordsMapStruct.toDTO(entity);
    }

    @Override
    public Page<AdminActivityRecordsDTO> pageAdminActivityRecords(long current, long size, String userId, Long bankId, String type) {
        LambdaQueryWrapper<ActivityRecords> queryWrapper = new LambdaQueryWrapper<>();
        
        if (userId != null && !userId.trim().isEmpty()) {
            queryWrapper.eq(ActivityRecords::getUserId, userId);
        }
        if (bankId != null) {
            queryWrapper.eq(ActivityRecords::getBankId, bankId);
        }
        if (type != null && !type.trim().isEmpty()) {
            queryWrapper.eq(ActivityRecords::getType, type);
        }
        
        queryWrapper.orderByDesc(ActivityRecords::getStartTime);
        
        Page<ActivityRecords> entityPage = new Page<>(current, size);
        Page<ActivityRecords> resultPage = activityRecordsMapper.selectPage(entityPage, queryWrapper);
        
        // 手动创建Page对象解决类型转换问题
        Page<AdminActivityRecordsDTO> dtoPage = new Page<>();
        dtoPage.setRecords(adminActivityRecordsMapStruct.toDTOList(resultPage.getRecords()));
        dtoPage.setTotal(resultPage.getTotal());
        dtoPage.setSize(resultPage.getSize());
        dtoPage.setCurrent(resultPage.getCurrent());
        dtoPage.setPages(resultPage.getPages());
        
        return dtoPage;
    }

    @Override
    public List<AdminActivityRecordsDTO> getAllAdminActivityRecords() {
        List<ActivityRecords> entities = activityRecordsMapper.selectList(null);
        return adminActivityRecordsMapStruct.toDTOList(entities);
    }

    /**
     * 判断答案是否正确
     *
     * @param answerDTO 答案DTO
     * @return 正确返回1，错误返回0
     */
    public int judgeAnswer(AnswerDTO answerDTO) {
        // 处理null值
        if (answerDTO.getAnswer() == null || answerDTO.getUserAnswer() == null) {
            return 0;
        }
        switch (answerDTO.getType()) {
            case "single", "judge" -> {
                // 单选题、判断题比较第一个元素
                return (answerDTO.getAnswer().size() > 0 && answerDTO.getUserAnswer().size() > 0 &&
                        answerDTO.getAnswer().get(0).equals(answerDTO.getUserAnswer().get(0))) ? 1 : 0;
            }
            // 判断题比较第一个元素
            case "multiple" -> {
                // 多选题需要排序后比较
                if (answerDTO.getAnswer().size() != answerDTO.getUserAnswer().size()) {
                    return 0;
                }
                List<String> sortedCorrect = answerDTO.getAnswer().stream()
                        .sorted()
                        .toList();
                List<String> sortedUser = answerDTO.getUserAnswer().stream()
                        .sorted()
                        .toList();
                return sortedCorrect.equals(sortedUser) ? 1 : 0;
            }
            case "fill" -> {
                // 填空题可能需要考虑大小写和空格
                if (answerDTO.getAnswer().isEmpty() || answerDTO.getUserAnswer().isEmpty()) {
                    return 0;
                }

                // 检查用户答案是否在正确答案列表中
                for (String userAns : answerDTO.getUserAnswer()) {
                    boolean found = false;
                    for (String correctAns : answerDTO.getAnswer()) {
                        if (correctAns.trim().equalsIgnoreCase(userAns.trim())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return 0;
                    }
                }
                return 1;
            }
            case "short" -> {
                // 简答题可能需要更复杂的判断或人工评分
                // 这里可以返回null或特殊值，表示需要人工评分
                return 1; // 需要人工评分
            }
            default -> {
                return 0;
            }
        }
    }

    /**
     * 获取用户学习统计概览
     *
     * @param userId 用户ID
     * @return 学习统计概览DTO
     */
    @Override
    public StudyStatisticsDTO getStudyStatistics(String userId) {
        StudyStatisticsDTO statisticsDTO = new StudyStatisticsDTO();
        
        // 查询用户的活动记录统计
        LambdaQueryWrapper<ActivityRecords> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityRecords::getUserId, userId);
        queryWrapper.eq(ActivityRecords::getStatus, "completed");
        List<ActivityRecords> activityRecords = activityRecordsMapper.selectList(queryWrapper);
        
        // 计算总练习次数和考试次数
        long practiceCount = activityRecords.stream()
                .filter(record -> "practice".equals(record.getType()))
                .count();
        long examCount = activityRecords.stream()
                .filter(record -> "exam".equals(record.getType()))
                .count();
        
        // 计算总题目数和正确题目数
        int totalQuestions = 0;
        int correctCount = 0;
        
        for (ActivityRecords record : activityRecords) {
            if (record.getQuestionIds() != null) {
                totalQuestions += record.getQuestionIds().size();
            }
            if (record.getUserScore() != null) {
                correctCount += record.getUserScore();
            }
        }
        
        // 计算正确率
        double accuracyRate = totalQuestions > 0 ? (double) correctCount / totalQuestions * 100 : 0;
        
        // 设置统计结果
        statisticsDTO.setTotalPracticeCount((int) practiceCount);
        statisticsDTO.setTotalExamCount((int) examCount);
        statisticsDTO.setTotalQuestions(totalQuestions);
        statisticsDTO.setCorrectCount(correctCount);
        statisticsDTO.setAccuracyRate(Math.round(accuracyRate * 100.0) / 100.0);
        statisticsDTO.setWrongCount(totalQuestions - correctCount);
        
        return statisticsDTO;
    }

    /**
     * 获取用户学习趋势数据
     *
     * @param userId 用户ID
     * @param days 天数（默认7天）
     * @return 学习趋势DTO列表
     */
    @Override
    public List<StudyTrendDTO> getStudyTrend(String userId, Integer days) {
        if (days == null || days <= 0) {
            days = 7; // 默认查询最近7天
        }
        
        List<StudyTrendDTO> trendList = new ArrayList<>();
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        
        // 查询指定时间范围内的活动记录
        LambdaQueryWrapper<ActivityRecords> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityRecords::getUserId, userId)
                .ge(ActivityRecords::getStartTime, startDate)
                .le(ActivityRecords::getStartTime, endDate)
                .eq(ActivityRecords::getStatus, "completed")
                .orderByAsc(ActivityRecords::getStartTime);
        
        List<ActivityRecords> activityRecords = activityRecordsMapper.selectList(queryWrapper);
        
        // 按日期分组统计
        Map<String, StudyTrendDTO> dateMap = new HashMap<>();
        
        for (ActivityRecords record : activityRecords) {
            String date = record.getStartTime().toLocalDate().toString();
            
            StudyTrendDTO trendDTO = dateMap.getOrDefault(date, new StudyTrendDTO());
            trendDTO.setDate(date);

            // 统计练习和考试次数
            if ("practice".equals(record.getType())) {
                trendDTO.setPracticeCount(trendDTO.getPracticeCount() + 1);
            } else if ("exam".equals(record.getType())) {
                trendDTO.setExamCount(trendDTO.getExamCount() + 1);
            }
            
            // 统计题目数量
            if (record.getQuestionIds() != null) {
                trendDTO.setQuestionCount(trendDTO.getQuestionCount() + record.getQuestionIds().size());
            }
            
            // 统计正确题目数
            if (record.getUserScore() != null) {
                trendDTO.setCorrectCount(trendDTO.getCorrectCount() + record.getUserScore());
            }
            
            dateMap.put(date, trendDTO);
        }
        
        // 填充缺失的日期数据
        for (int i = 0; i < days; i++) {
            String date = startDate.plusDays(i).toLocalDate().toString();
            if (!dateMap.containsKey(date)) {
                StudyTrendDTO trendDTO = new StudyTrendDTO();
                trendDTO.setDate(date);
                trendDTO.setPracticeCount(0);
                trendDTO.setExamCount(0);
                trendDTO.setQuestionCount(0);
                trendDTO.setCorrectCount(0);
                dateMap.put(date, trendDTO);
            }
        }
        
        // 按日期排序返回结果
        return dateMap.values().stream()
                .sorted(Comparator.comparing(StudyTrendDTO::getDate))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户题库学习进度
     *
     * @param userId 用户ID
     * @return 题库进度DTO列表
     */
    @Override
    public List<BankProgressDTO> getBankProgress(String userId) {
        List<BankProgressDTO> progressList = new ArrayList<>();
        
        // 查询所有题库
        List<QuestionBanks> allBanks = questionBanksMapper.selectList(null);
        
        for (QuestionBanks bank : allBanks) {
            BankProgressDTO progressDTO = new BankProgressDTO();
            progressDTO.setBankId(bank.getId());
            progressDTO.setBankName(bank.getName());
            
            // 查询该题库的题目总数
            LambdaQueryWrapper<Questions> questionWrapper = new LambdaQueryWrapper<>();
            questionWrapper.eq(Questions::getBankId, bank.getId());
            long totalQuestions = questionsService.count(questionWrapper);// 获取题库题目数量
            progressDTO.setTotalQuestions((int) totalQuestions);
            
            // 查询用户在该题库的活动记录
            LambdaQueryWrapper<ActivityRecords> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(ActivityRecords::getUserId, userId)
                    .eq(ActivityRecords::getStatus, "completed")
                    .eq(ActivityRecords::getBankId, bank.getId());
            
            List<ActivityRecords> userRecords = activityRecordsMapper.selectList(recordWrapper);
            
            // 计算已完成的题目数（去重）
            Set<Long> completedQuestionIds = new HashSet<>();
            for (ActivityRecords record : userRecords) {
                if (record.getQuestionIds() != null) {
                    completedQuestionIds.addAll(record.getQuestionIds());
                }
            }
            
            int completedCount = completedQuestionIds.size();
            progressDTO.setCompletedQuestions(completedCount);
            
            // 计算进度百分比
            double progressPercentage = totalQuestions > 0 ? (double) completedCount / totalQuestions * 100 : 0;
            progressDTO.setProgressPercentage(Math.round(progressPercentage * 100.0) / 100.0);
            
            // 设置进度条颜色
            if (progressPercentage >= 80) {
                progressDTO.setProgressColor("success");
            } else if (progressPercentage >= 50) {
                progressDTO.setProgressColor("warning");
            } else {
                progressDTO.setProgressColor("danger");
            }
            
            progressList.add(progressDTO);
        }
        
        // 按进度百分比降序排序
        progressList.sort((a, b) -> Double.compare(b.getProgressPercentage(), a.getProgressPercentage()));
        
        return progressList;
    }
}