package com.campusassistant.commonservice.controller;

import com.campusassistant.commonservice.pojo.vo.ManualConfigVO;
import com.campusassistant.commonservice.pojo.vo.ScheduleConfigVO;
import com.campusassistant.commonservice.pojo.vo.NoticeResponseVO;
import com.campusassistant.commonservice.service.PublicConfigService;
import com.campusassistant.pojo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway/public")
@RequiredArgsConstructor
public class PublicConfigController {

    private final PublicConfigService publicConfigService;

    @GetMapping("/notice")
    public Result<NoticeResponseVO> getCurrentNotice() {
        return Result.success(publicConfigService.getCurrentNotice());
    }

    @GetMapping("/manual")
    public Result<ManualConfigVO> getManual() {
        return Result.success(publicConfigService.getManualConfig());
    }

    @GetMapping("/schedule-config")
    public Result<ScheduleConfigVO> getScheduleConfig() {
        return Result.success(publicConfigService.getScheduleConfig());
    }
}
