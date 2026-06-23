package com.campusassistant.student.controller;

import com.campusassistant.pojo.Result;
import com.campusassistant.student.pojo.dto.LoginDTO;
import com.campusassistant.student.pojo.dto.UserDTO;
import com.campusassistant.student.service.AuthService;
import com.campusassistant.student.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {


    private final AuthService authService;
    private final CurrentUserService currentUserService;


    @PostMapping("/register")
    public Result<String> register(@RequestBody @Validated UserDTO userDTO) {
        authService.register(userDTO);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody @Validated LoginDTO loginDTO){
        String token = authService.login(loginDTO);
        return Result.success(token);
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return Result.success();
    }

    @PostMapping("/refresh")
    public Result<String> refreshData() {
        currentUserService.refreshData();
        return Result.success("刷新任务已提交，后台正在同步数据");
    }

}
