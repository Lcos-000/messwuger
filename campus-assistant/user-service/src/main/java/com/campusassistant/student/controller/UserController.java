package com.campusassistant.student.controller;

import com.campusassistant.common.UserContext;
import com.campusassistant.pojo.Result;
import com.campusassistant.student.pojo.UserStatusVO;
import com.campusassistant.remote.spider.pojo.PersonalInfoVO;
import com.campusassistant.student.service.CurrentUserService;
import com.campusassistant.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final CurrentUserService currentUserService;

    @GetMapping("/status")
    public Result<UserStatusVO> getStatus() {
        UserContext userContext = ThreadLocalUtil.get();
        UserStatusVO statusVO = currentUserService.getStatusByStudentId(userContext.getStudentId());
        return Result.success(statusVO);
    }

    @GetMapping("/personal")
    public Result<PersonalInfoVO> getPersonal() {
        UserContext userContext = ThreadLocalUtil.get();
        PersonalInfoVO personalVO = currentUserService.getPersonalByStudentId(userContext.getStudentId());
        return Result.success(personalVO);
    }

    @DeleteMapping("/delete")
    public Result<Void> unsubscribe() {
        currentUserService.self_unsubscribe();
        return Result.success();
    }

}
