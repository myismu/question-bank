package com.exambaby.qbank.service.code.component;

import com.exambaby.qbank.service.code.service.ActivityRecordsService;
import com.exambaby.qbank.service.code.utils.ExamRedisKeyUtils;
import jakarta.annotation.PostConstruct;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * 启用监听 Redisson 延时队列，自动提交试卷
 */
@Component
public class AutoSubmitListener {

    private final RedissonClient redissonClient;
    private final ActivityRecordsService activityRecordsService;
    public AutoSubmitListener(RedissonClient redissonClient, ActivityRecordsService activityRecordsService) {
        this.redissonClient = redissonClient;
        this.activityRecordsService = activityRecordsService;
    }


    @PostConstruct
    public void startListener() {
        new Thread(() -> {
            RQueue<String> queue = redissonClient.getQueue(ExamRedisKeyUtils.EXAM_QUEUE_KEY);
            while (!redissonClient.isShutdown()) {
                try {
                    String recordId = queue.poll(); // 非阻塞
                    if (recordId != null) {
                        // 处理业务逻辑
                        activityRecordsService.reviewActivityRecord(Long.valueOf(recordId),"timeout");
                    }
                    Thread.sleep(5000); // 防止CPU空转
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "exam-auto-submi1t-thread").start();
    }
}