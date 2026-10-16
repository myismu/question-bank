package com.exambaby.qbank.service.code.controller.publics;

import com.exambaby.qbank.service.code.dto.publics.PublicActivityAnswersDTO;
import com.exambaby.qbank.service.code.service.ActivityAnswersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公共端答题记录库接口", description = "公共端答题记录库接口")
@RestController
@RequestMapping("/public/activityAnswers")
public class PublicActivityAnswersController {

    private final ActivityAnswersService activityAnswersService;

    public PublicActivityAnswersController(ActivityAnswersService activityAnswersService) {
        this.activityAnswersService = activityAnswersService;
    }


    /**
     * ### 保存/更新答题记录
     * @param publicActivityAnswersDTO 答题记录 DTO
     */
    @Operation(summary = "批量保存/更新答题记录", description = "批量保存或更新某次活动下某道题的答题记录，支持单题或批量提交")
    @PostMapping("/saveOrUpdateBatch")
    public void saveOrUpdateBatch(@RequestBody List<PublicActivityAnswersDTO> publicActivityAnswersDTO) {
        activityAnswersService.saveOrUpdateActivityAnswers(publicActivityAnswersDTO);
    }

    /**
     * ### 查询答题记录
     * @param recordId 答题记录 ID
     * @return List<PublicActivityAnswersDTO> 答题记录列表
     */
    @Operation(summary = "查询活动答题进度", description = "根据 recordId 查询该次活动的所有答题记录，用于断点续答/进度恢复")
    @GetMapping("/list")
    public List<PublicActivityAnswersDTO> listActivityAnswers(@RequestParam String recordId) {
        return activityAnswersService.listActivityAnswers(recordId);
    }

    /**
     * ### 删除答题记录
     * @param recordId 答题记录 ID
     */
    @Operation(summary = "删除活动答题记录", description = "根据 recordId 删除该次活动的所有答题记录")
    @DeleteMapping("/deleteActivityAnswersByRecordId")
    public void deleteActivityAnswersByRecordId(@RequestParam String recordId) {
        activityAnswersService.deleteActivityAnswersByRecordId(recordId);
    }
}
