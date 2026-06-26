package com.campusassistant.personalization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campusassistant.common.UserContext;
import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.personalization.common.CustomAssetTypeEnum;
import com.campusassistant.personalization.mapper.UserProfileCustomAssetMapper;
import com.campusassistant.personalization.pojo.entity.UserProfileCustomAssetEntity;
import com.campusassistant.personalization.pojo.vo.UploadCustomAssetVO;
import com.campusassistant.personalization.pojo.vo.UserProfileCustomAssetVO;
import com.campusassistant.personalization.service.AliyunOssService;
import com.campusassistant.personalization.service.UserProfileCustomAssetService;
import com.campusassistant.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

import static com.campusassistant.enums.ResultCodeEnum.PARAM_ERROR;
import static com.campusassistant.enums.ResultCodeEnum.UNAUTHORIZED;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileCustomAssetServiceImpl implements UserProfileCustomAssetService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;

    private final UserProfileCustomAssetMapper userProfileCustomAssetMapper;
    private final AliyunOssService aliyunOssService;

    @Override
    public UserProfileCustomAssetVO getCurrentUserCustomAssets() {
        String studentId = getCurrentStudentId();
        UserProfileCustomAssetEntity entity = getByStudentId(studentId);

        UserProfileCustomAssetVO vo = new UserProfileCustomAssetVO();
        if (entity == null) {
            return vo;
        }

        vo.setCustomAvatar(entity.getCustomAvatar());
        vo.setCustomBackground(entity.getCustomBackground());
        vo.setCustomWallpaper(entity.getCustomWallpaper());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadCustomAssetVO uploadCustomAsset(String type, MultipartFile file) {
        String studentId = getCurrentStudentId();
        validateFile(file);

        CustomAssetTypeEnum assetType = CustomAssetTypeEnum.fromCode(type);
        if (assetType == null) {
            throw new BusinessException(PARAM_ERROR.getCode(), "图片类型不支持");
        }

        String objectKey = buildObjectKey(studentId, assetType, file.getOriginalFilename());
        String url = aliyunOssService.upload(file, objectKey);

        UserProfileCustomAssetEntity exist = getByStudentId(studentId);
        if (exist == null) {
            UserProfileCustomAssetEntity entity = new UserProfileCustomAssetEntity();
            entity.setStudentId(studentId);
            setAssetField(entity, assetType, url);
            userProfileCustomAssetMapper.insert(entity);
        } else {
            LambdaUpdateWrapper<UserProfileCustomAssetEntity> luw = new LambdaUpdateWrapper<>();
            luw.eq(UserProfileCustomAssetEntity::getStudentId, studentId);

            switch (assetType) {
                case AVATAR -> luw.set(UserProfileCustomAssetEntity::getCustomAvatar, url);
                case BACKGROUND -> luw.set(UserProfileCustomAssetEntity::getCustomBackground, url);
                case WALLPAPER -> luw.set(UserProfileCustomAssetEntity::getCustomWallpaper, url);
                default -> throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR);
            }

            userProfileCustomAssetMapper.update(null, luw);
        }

        UploadCustomAssetVO vo = new UploadCustomAssetVO();
        vo.setType(assetType.getCode());
        vo.setUrl(url);
        return vo;
    }

    private String getCurrentStudentId() {
        UserContext userContext = ThreadLocalUtil.get();
        String studentId = userContext.getStudentId();
        if (studentId == null) {
            throw new BusinessException(UNAUTHORIZED);
        }
        return studentId;
    }

    private UserProfileCustomAssetEntity getByStudentId(String studentId) {
        LambdaQueryWrapper<UserProfileCustomAssetEntity> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserProfileCustomAssetEntity::getStudentId, studentId);
        return userProfileCustomAssetMapper.selectOne(lqw);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(PARAM_ERROR.getCode(), "上传文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(PARAM_ERROR.getCode(), "图片不能超过 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new BusinessException(PARAM_ERROR.getCode(), "仅支持图片文件上传");
        }
    }

    private String buildObjectKey(String studentId, CustomAssetTypeEnum assetType, String originalFilename) {
        String suffix = getFileSuffix(originalFilename);
        return String.format(
                "profile-custom/%s/%s/%s/%s%s",
                assetType.getOssFolder(),
                LocalDate.now(),
                studentId,
                UUID.randomUUID().toString().replace("-", ""),
                suffix
        );
    }

    private String getFileSuffix(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }
        return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT);
    }

    private void setAssetField(UserProfileCustomAssetEntity entity, CustomAssetTypeEnum assetType, String url) {
        switch (assetType) {
            case AVATAR -> entity.setCustomAvatar(url);
            case BACKGROUND -> entity.setCustomBackground(url);
            case WALLPAPER -> entity.setCustomWallpaper(url);
            default -> throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR);
        }
    }
}