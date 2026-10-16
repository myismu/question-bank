package com.exambaby.qbank.service.code.utils;

public final class ExamRedisKeyUtils {
    public static final String EXAM_QUEUE_KEY = "questionBank:exam";

    private ExamRedisKeyUtils() {
    }

    public static String buildExamLockKey(Long recordId) {
        return EXAM_QUEUE_KEY + ":" + recordId;
    }
}
