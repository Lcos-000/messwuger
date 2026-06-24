package com.campusassistant.student.controller;

import com.campusassistant.common.UserContext;
import com.campusassistant.pojo.Result;
import com.campusassistant.student.pojo.UserStatusVO;
import com.campusassistant.remote.spider.pojo.PersonalInfoVO;
import com.campusassistant.student.service.CurrentUserService;
import com.campusassistant.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
        UserContext userContext = ThreadLocalUtil.get();
        UserStatusVO statusVO = currentUserService.getStatusByStudentId(userContext.getStudentId());
        return Result.success(statusVO);
    }

    @Operation(summary = "获取用户个人信息")
    @GetMapping("/personal")
    public Result<PersonalInfoVO> getPersonal() {
        UserContext userContext = ThreadLocalUtil.get();
        PersonalInfoVO personalVO = currentUserService.getPersonalByStudentId(userContext.getStudentId());
        return Result.success(personalVO);
    }

    @Operation(summary = "用户取消订阅")
    @DeleteMapping("/delete")
    public Result<Void> unsubscribe() {
        currentUserService.self_unsubscribe();
        return Result.success();
    }

}
