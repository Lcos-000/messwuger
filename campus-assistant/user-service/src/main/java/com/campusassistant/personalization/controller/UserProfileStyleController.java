package com.campusassistant.personalization.controller;

import com.campusassistant.personalization.pojo.UserProfileStyleUpdateDTO;
import com.campusassistant.personalization.pojo.vo.ProfileDefaultOptionsVO;
import com.campusassistant.personalization.pojo.vo.UploadCustomAssetVO;
import com.campusassistant.personalization.pojo.vo.UserProfileCustomAssetVO;
import com.campusassistant.personalization.pojo.vo.UserProfileStyleVO;
import com.campusassistant.personalization.service.UserProfileStyleService;
import com.campusassistant.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.campusassistant.personalization.service.UserProfileCustomAssetService;


@Tag(name = "用户个性化配置")
@RestController
@RequestMapping("/personalization")
@RequiredArgsConstructor
public class UserProfileStyleController {

    private final UserProfileStyleService userProfileStyleService;
    private final UserProfileCustomAssetService userProfileCustomAssetService;

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

    @Operation(summary = "查询当前用户自定义图片资源")
    @GetMapping("/get-custom-assets")
    public Result<UserProfileCustomAssetVO> getCurrentCustomAssets() {
        return Result.success(userProfileCustomAssetService.getCurrentUserCustomAssets());
    }

    @Operation(summary = "上传当前用户自定义图片")
    @PostMapping("/upload-custom-asset")
    public Result<UploadCustomAssetVO> uploadCustomAsset(
            @RequestParam("type") String type,
            @RequestPart("file") MultipartFile file) {
        return Result.success(userProfileCustomAssetService.uploadCustomAsset(type, file));
    }

}