package com.campusassistant.remote.spider.controller;

import com.campusassistant.pojo.Result;
import com.campusassistant.remote.spider.pojo.SyncDataDTO;
import com.campusassistant.remote.spider.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/api/v1/sync") // 按照 V3 文档里的路径
@RequiredArgsConstructor
public class InternalSyncController {

    private final SyncService syncService;

    /**
     * 接收 Go 爬虫回调的综合学生数据
     * TODO
     * 添加参数校验
     */
    @PostMapping("/student-data")
    public Result<?> receiveStudentData(@RequestBody SyncDataDTO syncDataDTO) {

        syncService.handleStudentDataSync(syncDataDTO);

        return Result.success("数据接收成功");
    }

    /**
     * 接收 Go 端打卡结果回调
     */
    @PostMapping("/punch-result")
    public Result<?> receivePunchResult(@RequestParam("studentId") String studentId,
                                        @RequestParam("success") Boolean success) {
        syncService.handlePunchResult(studentId, success);
        return Result.success("打卡结果接收成功");
    }

}
