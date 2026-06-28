package com.campusassistant.student.controller;

import com.campusassistant.pojo.Result;
import com.campusassistant.student.pojo.UserStatusVO;
import com.campusassistant.remote.spider.pojo.PersonalInfoVO;
import com.campusassistant.student.pojo.dto.AutoPunchSwitchDTO;
import com.campusassistant.student.service.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "用户接口")
public class UserController {

    private final CurrentUserService currentUserService;

    @Operation(summary = "获取用户状态")
    @GetMapping("/status")
    public Result<UserStatusVO> getStatus() {
        UserStatusVO statusVO = currentUserService.getStatusByStudentId();
        return Result.success(statusVO);
    }

    @Operation(summary = "获取用户个人信息")
    @GetMapping("/personal")
    public Result<PersonalInfoVO> getPersonal() {
        PersonalInfoVO personalVO = currentUserService.getPersonalByStudentId();
        return Result.success(personalVO);
    }

    @Operation(summary = "用户取消订阅")
    @DeleteMapping("/delete")
    public Result<Void> unsubscribe() {
        currentUserService.self_unsubscribe();
        return Result.success();
    }

    @Operation(summary = "更新自动打卡开关")
    @PutMapping("/auto-punch")
    public Result<Void> updateAutoPunch(@Valid @RequestBody AutoPunchSwitchDTO dto) {
        currentUserService.updateAutoPunchEnabled(dto.getAutoPunchEnabled());
        return Result.success();
    }

}
