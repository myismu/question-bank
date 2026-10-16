package com.exambaby.qbank.service.code.utils;

public final class QuestionBankCacheKeyUtils {
    public static final String ALL_PUBLIC_QUESTION_BANKS_KEY = "questionBank:public:list";
    private static final String QUESTION_DETAIL_KEY_PREFIX = "question:detail:";
    private static final String QUESTION_BANK_LIST_KEY_PREFIX = "question:bank:list:";

    private QuestionBankCacheKeyUtils() {
    }

    public static String questionDetailKey(Long questionId) {
        return QUESTION_DETAIL_KEY_PREFIX + questionId;
    }

    public static String questionBankListKey(Long bankId) {
        return QUESTION_BANK_LIST_KEY_PREFIX + bankId;
    }
}
