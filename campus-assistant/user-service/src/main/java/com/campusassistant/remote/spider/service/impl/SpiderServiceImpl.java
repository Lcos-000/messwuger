package com.campusassistant.remote.spider.service.impl;

import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.pojo.Result;
import com.campusassistant.remote.spider.client.SpiderServiceClient;
import com.campusassistant.remote.spider.service.SpiderService;
import com.campusassistant.student.service.impl.support.UserWriteSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.campusassistant.remote.spider.common.SyncStatusEnum.SYNCING_FAILED;
import static com.campusassistant.remote.spider.common.SyncStatusEnum.SYNCING;
import static com.campusassistant.remote.spider.common.constant.*;
import static com.campusassistant.student.common.PunchStatusEnum.PUNCHING;
import static com.campusassistant.student.common.PunchStatusEnum.PUNCH_FAILED;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpiderServiceImpl implements SpiderService {

    private final SpiderServiceClient spiderServiceClient;
    private final UserWriteSupport userWriteSupport;

    @Override
    public boolean validateCredentials(String studentId, String encryptedPassword) {
        Result<?> result = spiderServiceClient.verifyAccount(studentId, encryptedPassword, VERIFY);
        return Objects.equals(result.getCode(), ResultCodeEnum.SUCCESS.getCode());
    }

    @Async
    @Override
    public void asyncStartFullCrawl(String studentId, String encryptedPassword) {
        log.info("开始异步触发爬虫任务，学号: {}", studentId);

        try {
            // 将用户的同步状态更新为 "1" (SYNCING 同步中)
            userWriteSupport.updateSyncStatus(studentId, SYNCING.getCode());

            // 调用 Go 端 Feign 接口触发全量爬取
            Result<?> result = spiderServiceClient.startFullSpiderTask(studentId, encryptedPassword,FULL_CRAWL);

            if (!Objects.equals(result.getCode(), ResultCodeEnum.SUCCESS.getCode())) {
                log.error("触发 Go 端爬虫失败或降级");
                // 触发失败，更新状态为 "3" (FAILED)
                userWriteSupport.updateSyncStatus(studentId, SYNCING_FAILED.getCode());
            }
        } catch (Exception e) {
            log.error("异步触发全量爬虫时发生异常, 学号: {}", studentId, e);
            // 发生异常，更新状态为 "3" (FAILED)
            userWriteSupport.updateSyncStatus(studentId, SYNCING_FAILED.getCode());
        }
    }

    @Async
    @Override
    public void asyncStartPunchCard(String studentId, String encryptedPassword) {
        log.info("开始异步触发打卡任务，学号: {}", studentId);
        try {
            // 状态改为：打卡中
            userWriteSupport.updatePunchStatus(studentId, PUNCHING.getCode());

            Result<?> result = spiderServiceClient.startPunchCardTask(studentId, encryptedPassword, PUNCH_CARD);

            if (!Objects.equals(result.getCode(), ResultCodeEnum.SUCCESS.getCode())) {
                log.error("触发 Go 端打卡失败或降级");
                userWriteSupport.updatePunchStatus(studentId, PUNCH_FAILED.getCode());
            }
        } catch (Exception e) {
            log.error("异步触发自动打卡时发生异常, 学号: {}", studentId, e);
            userWriteSupport.updatePunchStatus(studentId, PUNCH_FAILED.getCode());
        }
    }

}
