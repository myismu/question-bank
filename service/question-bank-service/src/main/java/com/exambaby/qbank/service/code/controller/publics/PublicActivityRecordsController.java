package com.exambaby.qbank.service.code.controller.publics;

import com.exambaby.common.core.domain.BusinessException;
import com.exambaby.common.core.utils.ThreadLocalContext;
import com.exambaby.qbank.service.code.dto.AnswerDTO;
import com.exambaby.qbank.service.code.dto.publics.*;
import com.exambaby.qbank.service.code.service.ActivityRecordsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公共端记录库接口", description = "公共端记录库接口")
@RestController
@RequestMapping("/public/activityRecords")
public class PublicActivityRecordsController {

    private final ActivityRecordsService activityRecordsService;

    public PublicActivityRecordsController(ActivityRecordsService activityRecordsService) {
        this.activityRecordsService = activityRecordsService;
    }

    @Operation(summary = "创建活动记录", description = "新建一条活动记录（练习或考试），返回活动ID（recordId）")
    @PostMapping("/createActivityRecord")
    public String createActivityRecord(@RequestBody PublicActivityRecordsDTO publicActivityRecordsDTO) {
        String userId = ThreadLocalContext.getUserId();
        publicActivityRecordsDTO.setUserId(Long.valueOf(userId));

        PublicActivityRecordsDTO activityRecord = activityRecordsService.createActivityRecord(publicActivityRecordsDTO);

        return String.valueOf(activityRecord.getId());
    }

    @Operation(summary = "完成/放弃活动", description = "用户交卷/放弃时，更新活动记录状态、统计分数/正确数/错误数等")
    @PostMapping("/finishActivityRecord")
    public void finishActivityRecord(@RequestBody PublicActivityRecordsDTO publicActivityRecordsDTO) {
        activityRecordsService.finishActivityRecord(publicActivityRecordsDTO);
    }

    @Operation(summary = "修改活动记录", description = "修改一条活动记录（练习或考试）")
    @PutMapping("/updateActivityRecord")
    public void updateActivityRecord(@RequestBody PublicActivityRecordsDTO publicActivityRecordsDTO) {
        activityRecordsService.updateActivityRecord(publicActivityRecordsDTO);
    }

    @Operation(summary = "查询用户活动历史", description = "根据用户的id，题库id，类型（练习/考试）查询用户的活动记录")
    @GetMapping("/selectActivityRecords")
    public PublicActivityRecordsDTO selectActivityRecords(@RequestParam Long bankId, @RequestParam String type) {
        String userId = ThreadLocalContext.getUserId();
        return activityRecordsService.ActivityRecords(userId, bankId, type);
    }

    @Operation(summary = "改卷", description = "根据recordId查询活动记录，再查询答题记录信息，进行批改")
    @GetMapping("/startAutoSubmit")
    public String startAutoSubmit(@RequestParam Long recordId, @RequestParam Long durationInSeconds) {
        if (recordId == null) throw new BusinessException("考试信息为空", 444);
        return activityRecordsService.registerAutoSubmitTask(recordId, durationInSeconds);
    }

    @GetMapping("/selectUserAnswerAndQuestionInfoByRecordId")
    public List<AnswerDTO> selectUserAnswerAndQuestionInfoByRecordId(@RequestParam Long recordId) {
        if (recordId == null) throw new BusinessException("考试信息为空", 444);
        return activityRecordsService.selectUserAnswerAndQuestionInfoByRecordId(recordId);
    }

    @Operation(summary = "获取用户学习统计概览", description = "获取用户的总练习次数、考试次数、题目数、正确率等统计信息")
    @GetMapping("/getStudyStatistics")
    public StudyStatisticsDTO getStudyStatistics() {
        String userId = ThreadLocalContext.getUserId();
        return activityRecordsService.getStudyStatistics(userId);
    }

    @Operation(summary = "获取用户学习趋势数据", description = "获取用户最近几天的学习趋势数据，包括每日练习次数、考试次数、题目数等")
    @GetMapping("/getStudyTrend")
    public List<StudyTrendDTO> getStudyTrend(@RequestParam(required = false) Integer days) {
        String userId = ThreadLocalContext.getUserId();
        return activityRecordsService.getStudyTrend(userId, days);
    }

    @Operation(summary = "获取用户题库学习进度", description = "获取用户在各个题库的学习进度，包括完成题目数、总题目数、进度百分比等")
    @GetMapping("/getBankProgress")
    public List<BankProgressDTO> getBankProgress() {
        String userId = ThreadLocalContext.getUserId();
        return activityRecordsService.getBankProgress(userId);
    }
}
