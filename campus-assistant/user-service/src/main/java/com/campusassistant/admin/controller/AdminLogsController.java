package com.campusassistant.admin.controller;

import com.campusassistant.admin.pojo.vo.AdminLogFileListVO;
import com.campusassistant.admin.pojo.vo.AdminLogTailVO;
import com.campusassistant.admin.service.AdminLogService;
import com.campusassistant.anno.AdminCheck;
import com.campusassistant.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员日志接口")
@AdminCheck
@RestController
@RequestMapping("/admin/logs")
@RequiredArgsConstructor
public class AdminLogsController {

    private final AdminLogService adminLogService;

    @Operation(summary = "获取日志文件列表")
    @GetMapping("/files")
    public Result<AdminLogFileListVO> listFiles() {
        return Result.success(adminLogService.listLogFiles());
    }

    @Operation(summary = "初始化查看日志，返回最后500行")
    @GetMapping("/tail/init")
    public Result<AdminLogTailVO> initTail(@RequestParam("fileName") String fileName) {
        return Result.success(adminLogService.initTail(fileName));
    }

    @Operation(summary = "轮询日志增量")
    @GetMapping("/tail/poll")
    public Result<AdminLogTailVO> pollTail(@RequestParam("fileName") String fileName,
                                           @RequestParam("offset") Long offset) {
        return Result.success(adminLogService.pollTail(fileName, offset));
    }

    @Operation(summary = "向前翻查历史日志")
    @GetMapping("/tail/history")
    public Result<AdminLogTailVO> historyTail(@RequestParam("fileName") String fileName,
                                              @RequestParam("beforeOffset") Long beforeOffset) {
        return Result.success(adminLogService.historyTail(fileName, beforeOffset));
    }

    @Operation(summary = "下载日志文件")
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("fileName") String fileName) {
        return adminLogService.downloadLog(fileName);
    }

}