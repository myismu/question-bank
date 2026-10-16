package com.exambaby.qbank.service.code.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exambaby.qbank.service.code.dto.BankSimpleInfoDTO;
import com.exambaby.qbank.service.code.dto.WrongQuestionStatsVO;
import com.exambaby.qbank.service.code.dto.admin.AdminWrongQuestionsDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicQuestionsDTO;
import com.exambaby.qbank.service.code.dto.publics.PublicWrongQuestionsDTO;
import com.exambaby.qbank.service.code.entity.WrongQuestions;
import com.exambaby.qbank.service.code.mapper.WrongQuestionsMapper;
import com.exambaby.qbank.service.code.mapstruct.WrongQuestionsMapStruct;
import com.exambaby.qbank.service.code.service.QuestionsService;
import com.exambaby.qbank.service.code.service.WrongQuestionsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WrongQuestionsServiceImpl extends ServiceImpl<WrongQuestionsMapper, WrongQuestions> implements WrongQuestionsService {
    private final WrongQuestionsMapper wrongQuestionsMapper;
    private final WrongQuestionsMapStruct wrongQuestionsMapStruct;
    private final QuestionsService questionsService;

    public WrongQuestionsServiceImpl(WrongQuestionsMapper wrongQuestionsMapper, WrongQuestionsMapStruct wrongQuestionsMapStruct, QuestionsService questionsService) {
        this.wrongQuestionsMapper = wrongQuestionsMapper;
        this.wrongQuestionsMapStruct = wrongQuestionsMapStruct;
        this.questionsService = questionsService;
    }

    @Override
    public AdminWrongQuestionsDTO createWrongQuestion(AdminWrongQuestionsDTO dto) {
        WrongQuestions entity = wrongQuestionsMapStruct.toEntityFromAdmin(dto);
        save(entity);
        return wrongQuestionsMapStruct.toAdminDTO(entity);
    }

    @Override
    public AdminWrongQuestionsDTO updateWrongQuestion(AdminWrongQuestionsDTO dto) {
        WrongQuestions entity = wrongQuestionsMapStruct.toEntityFromAdmin(dto);
        updateById(entity);
        return wrongQuestionsMapStruct.toAdminDTO(entity);
    }

    @Override
    public void deleteWrongQuestion(Long id) {
        removeById(id);
    }

    @Override
    public AdminWrongQuestionsDTO getWrongQuestionById(Long id) {
        WrongQuestions entity = getById(id);
        return wrongQuestionsMapStruct.toAdminDTO(entity);
    }

    @Override
    public Page<AdminWrongQuestionsDTO> pageWrongQuestions(long current, long size, Long userId, Long questionId) {
        LambdaQueryWrapper<WrongQuestions> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(WrongQuestions::getUserId, userId);
        }
        if (questionId != null) {
            wrapper.eq(WrongQuestions::getQuestionId, questionId);
        }
        Page<WrongQuestions> page = new Page<>(current, size);
        Page<WrongQuestions> entityPage = wrongQuestionsMapper.selectPage(page, wrapper);
        Page<AdminWrongQuestionsDTO> dtoPage = new Page<>(current, size, entityPage.getTotal());
        dtoPage.setRecords(wrongQuestionsMapStruct.toAdminDTOList(entityPage.getRecords()));
        return dtoPage;
    }

    @Override
    public List<AdminWrongQuestionsDTO> getAllWrongQuestions() {
        List<WrongQuestions> list = list();
        return wrongQuestionsMapStruct.toAdminDTOList(list);
    }

    @Override
    public List<WrongQuestions> list(LambdaQueryWrapper<WrongQuestions> in) {
        return wrongQuestionsMapper.selectList(in);
    }

    @Transactional
    @Override
    public void saveBatchWrongQuestions(List<WrongQuestions> newWrongQuestions) {
        this.saveBatch(newWrongQuestions);
    }

    @Override
    public int batchUpdateErrorCount(List<WrongQuestions> updateList) {
        return wrongQuestionsMapper.batchUpdateErrorCount(updateList);
    }

    @Override
    public Page<PublicWrongQuestionsDTO> getPageWrongQuestionsList(Integer pageNum, Integer pageSize, Long questionBankId, String questionType, String status, String userId) {
//        构建条件查询条件
        LambdaQueryWrapper<WrongQuestions> wrapper = new LambdaQueryWrapper<>();
        if ((questionBankId != null) && (questionBankId != 0)) {
//            wrapper.eq(WrongQuestions::getQuestionBankId, questionBankId);
        }
        if (questionType != null && !questionType.equals("")) {
//            wrapper.eq(WrongQuestions::getQuestionType, questionType);
        }
        if (status != null && !status.equals("")) {
            wrapper.eq(WrongQuestions::getStatus, status);
        }

        wrapper.eq(WrongQuestions::getUserId, userId);

        Page<WrongQuestions> page = new Page<>(pageNum, pageSize);
        Page<WrongQuestions> wrongQuestionsPage = wrongQuestionsMapper.selectPage(page, wrapper);
//        根据WrongQuestionns的questionId查询question
        List<PublicWrongQuestionsDTO> publicWrongQuestionsDTOS = wrongQuestionsMapStruct.toPublicDTOList(wrongQuestionsPage.getRecords());
        for (PublicWrongQuestionsDTO publicWrongQuestionsDTO : publicWrongQuestionsDTOS) {
            PublicQuestionsDTO questionsDTO = questionsService.getPublicQuestionById(publicWrongQuestionsDTO.getQuestionId());
            publicWrongQuestionsDTO.setQuestions(questionsDTO);
        }
        Page<PublicWrongQuestionsDTO> publicWrongQuestionsPage = new Page<>(pageNum, pageSize, wrongQuestionsPage.getTotal());
        publicWrongQuestionsPage.setRecords(publicWrongQuestionsDTOS);

        return publicWrongQuestionsPage;
    }

    /**
     * 根据条件查询错题列表
     *
     * @param pageNum  当前页
     * @param pageSize 每页大小
     * @param bankId   题库id
     * @param status   错题状态
     * @param type     题目类型
     * @return Page<WrongQuestions>
     */
    @Override
    public Page<PublicWrongQuestionsDTO> getWrongQuestionsPage(Integer pageNum, Integer pageSize, Long userId, Long bankId, String status, String type) {
        Page<WrongQuestions> page = new Page<>(pageNum, pageSize);
        Page<WrongQuestions> wrongQuestionsPage = wrongQuestionsMapper.selectWrongQuestionsWithQuestions(page, userId, bankId, status, type);
        if (wrongQuestionsPage.getRecords().size() == 0) {
            return new Page<>(pageNum, pageSize, wrongQuestionsPage.getTotal());
        }
        //转化为PublicWrongQuestionsDTO
        Page<PublicWrongQuestionsDTO> publicWrongQuestionsPage = new Page<>(pageNum, pageSize, wrongQuestionsPage.getTotal());
        publicWrongQuestionsPage.setRecords(wrongQuestionsMapStruct.toPublicDTOList(wrongQuestionsPage.getRecords()));
        return publicWrongQuestionsPage;
    }

    @Override
    public WrongQuestionStatsVO getWrongQuestionStats(String userId) {
        return wrongQuestionsMapper.getStatsByUserId(Long.valueOf(userId));
    }

    //根据id修改错题表的状态为ignored
    @Override
    public int removeWrongQuestion(Long wrongQuestionId) {
//        根据id修改错题表的状态为ignored
        WrongQuestions wrongQuestion = new WrongQuestions();
        wrongQuestion.setId(wrongQuestionId);
        wrongQuestion.setStatus("ignored");
        return wrongQuestionsMapper.updateById(wrongQuestion);
    }

    @Override
    public List<BankSimpleInfoDTO> getDistinctBanksByUserId(Long userId) {
        return wrongQuestionsMapper.getDistinctBanksByUserId(userId);
    }

} 