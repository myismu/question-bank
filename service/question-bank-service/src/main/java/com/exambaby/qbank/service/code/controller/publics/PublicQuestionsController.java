package com.exambaby.qbank.service.code.controller.publics;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exambaby.common.core.domain.BusinessException;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionsDTO;
import com.exambaby.qbank.service.code.service.QuestionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公共题目接口", description = "公关端题目接口")
@RestController
@RequestMapping("/public/questions")
public class PublicQuestionsController {
    private final QuestionsService questionsService;

    public PublicQuestionsController(QuestionsService questionsService) {
        this.questionsService = questionsService;
    }


    /**
     * 根据题库ID获取题目列表
     *
     * @param id 题库ID
     * @return 题目DTO列表
     */
    @Operation(summary = "根据题库ID获取题目列表", description = "根据题库ID获取该题库下的所有题目")
    @GetMapping("/getQuestionsById")
    public List<PublicQuestionsDTO> getQuestionsById(@RequestParam Long id) {
        return questionsService.getQuestionsByBankId(id);
    }

    /**
     * 添加题目接口，参数：题目DTO，返回类型：boolean，返回值：成功或失败信息
     *
     * @param publicQuestionsDTO 题目DTO
     * @return 成功或失败信息
     */
    @Operation(summary = "添加题目", description = "添加新题目到指定题库，参数为题目DTO，返回是否成功")
    @PostMapping("/addQuestion")
    public boolean addQuestion(@RequestBody PublicQuestionsDTO publicQuestionsDTO) {
//        查看题库id是否存在
        if (publicQuestionsDTO.getBankId() == null) {
            throw new IllegalArgumentException("题库id不能为空");
        }
        return questionsService.addQuestion(publicQuestionsDTO);
    }

    /**
     * 批量添加题目接口，参数：题目DTO列表，返回类型：boolean，返回值：成功或失败信息
     *
     * @param publicQuestionsDTOList 题目DTO列表
     * @return 成功或失败信息
     */
    @Operation(summary = "批量添加题目", description = "批量添加题目到指定题库，参数为题目DTO列表，返回是否成功")
    @PostMapping("/BatchAddQuestion")
    public boolean BatchAddQuestion(@RequestBody List<PublicQuestionsDTO> publicQuestionsDTOList) {
        if (publicQuestionsDTOList.get(0).getBankId() == null) {
            throw new BusinessException("题库ID不能为空", 444);
        }
        return questionsService.BatchAddQuestion(publicQuestionsDTOList);
    }

    /**
     * 删除题目接口，参数：题目id，返回类型：boolean，返回值：成功或失败信息
     *
     * @param questionId 题目id
     * @return 成功或失败信息
     */
    @Operation(summary = "删除题目", description = "根据题目ID删除题目，返回是否成功")
    @DeleteMapping("/deleteQuestion")
    public boolean deleteQuestion(@RequestParam Long questionId) {
        //查看题目id是否存在
        if (questionId == null) {
            throw new IllegalArgumentException("要删除的题目信息不存在");
        }
        return questionsService.deleteQuestion(questionId);
    }

    /**
     * 修改题目接口，参数：题目DTO，返回类型：boolean，返回值：成功或失败信息
     *
     * @param publicQuestionsDTO 题目DTO
     * @return 成功或失败信息
     */
    @Operation(summary = "修改题目", description = "根据题目DTO修改题目信息，返回是否成功")
    @PutMapping("/updateQuestion")
    public boolean updateQuestion(@RequestBody PublicQuestionsDTO publicQuestionsDTO) {
        //查看题目id是否存在
        if (publicQuestionsDTO.getId() == null) {
            throw new BusinessException("要修改的题目信息不存在", 444);
        }
        return questionsService.PublicUpdateQuestion(publicQuestionsDTO);
    }

    /**
     * 分页查询题目列表接口，参数：页码，每页数量，题库id，返回类型：Page<PublicQuestionsDTO>，返回值：题目DTO列表
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param id       题库id
     * @return 题目DTO列表
     */
    @Operation(summary = "分页查询题目列表", description = "分页查询题目列表，参数为页码，每页数量，题库id，返回题目DTO列表")
    @GetMapping("/getQuestionsByPage")
    public Page<PublicQuestionsDTO> getQuestionsByPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                                       @RequestParam Long id) {
        return questionsService.getQuestionsByPage(pageNum, pageSize, id);
    }

    //    根据活动id获取题目列表接口，参数：活动id，返回类型：List<PublicQuestionsDTO>，返回值：题目DTO列表
    @Operation(summary = "根据活动id获取题目列表", description = "根据活动id获取该活动下的所有题目")
    @GetMapping("/getQuestionsByActivityId")
    public List<PublicQuestionsDTO> getQuestionsByActivityId(@RequestParam Long activityId) {
//        return questionsService.getQuestionsByActivityId(activityId);
        return null;
    }
}
