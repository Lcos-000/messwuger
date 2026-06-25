package com.campusassistant.personalization.controller;

import com.campusassistant.personalization.pojo.UserProfileStyleUpdateDTO;
import com.campusassistant.personalization.pojo.vo.ProfileDefaultOptionsVO;
import com.campusassistant.personalization.pojo.vo.UserProfileStyleVO;
import com.campusassistant.personalization.service.UserProfileStyleService;
import com.campusassistant.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户个性化配置")
@RestController
@RequestMapping("/personalization")
@RequiredArgsConstructor
public class UserProfileStyleController {

    private final UserProfileStyleService userProfileStyleService;

    @Operation(summary = "查询当前用户个性化配置")
    @GetMapping("/get-profile")
    public Result<UserProfileStyleVO> getCurrentProfileStyle() {
        UserProfileStyleVO vo = userProfileStyleService.getByStudentId();
        return Result.success(vo);
    }

    @Operation(summary = "更新当前用户个性化配置")
    @PutMapping("/update-profile")
    public Result<Void> updateCurrentProfileStyle(@Valid @RequestBody UserProfileStyleUpdateDTO dto) {
        userProfileStyleService.updateByStudentId(dto);
        return Result.success();
    }

    @Operation(summary = "查询用户个性化配置默认选项")
    @GetMapping("/get-default-options")
    public Result<ProfileDefaultOptionsVO> getDefaultOptions() {
        return Result.success(userProfileStyleService.getDefaultOptions());
    }

}