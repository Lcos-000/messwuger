package com.campusassistant.admin.controller;

import com.campusassistant.admin.service.AdminAuthService;
import com.campusassistant.anno.AdminCheck;
import com.campusassistant.pojo.Result;
import com.campusassistant.student.pojo.dto.LoginDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员认证接口")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Validated LoginDTO loginDTO) {
        return Result.success(adminAuthService.login(loginDTO));
    }

    @AdminCheck
    @Operation(summary = "管理员登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        adminAuthService.logout(request);
        return Result.success();
    }

}
