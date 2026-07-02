package com.campusassistant.admin.controller;

import com.campusassistant.admin.pojo.vo.AdminResourceVO;
import com.campusassistant.admin.service.AdminResourceService;
import com.campusassistant.anno.AdminCheck;
import com.campusassistant.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员资源接口")
@AdminCheck
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminResourcesController {

    private final AdminResourceService adminResourceService;

    @Operation(summary = "获取管理员资源跳转列表")
    @GetMapping("/resources")
    public Result<AdminResourceVO> getResources() {
        return Result.success(adminResourceService.getResourceList());
    }
}