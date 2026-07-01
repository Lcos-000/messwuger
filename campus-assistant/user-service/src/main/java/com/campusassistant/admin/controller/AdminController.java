package com.campusassistant.admin.controller;

import com.campusassistant.admin.pojo.vo.AdminResourceVO;
import com.campusassistant.admin.service.AdminService;
import com.campusassistant.anno.AdminCheck;
import com.campusassistant.pojo.Result;
import com.campusassistant.student.pojo.dto.LoginDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员资源接口")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Validated LoginDTO loginDTO) {
        return Result.success(adminService.login(loginDTO));
    }

    @AdminCheck
    @Operation(summary = "管理员登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        adminService.logout(request);
        return Result.success();
    }

    @AdminCheck
    @Operation(summary = "获取管理员资源跳转列表")
    @GetMapping("/resources")
    public Result<AdminResourceVO> getResources() {
        return Result.success(adminService.getResourceList());
    }
}