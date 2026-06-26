package com.campusassistant.personalization.service;

import com.campusassistant.personalization.pojo.vo.UploadCustomAssetVO;
import com.campusassistant.personalization.pojo.vo.UserProfileCustomAssetVO;
import org.springframework.web.multipart.MultipartFile;

public interface UserProfileCustomAssetService {

    UserProfileCustomAssetVO getCurrentUserCustomAssets();

    UploadCustomAssetVO uploadCustomAsset(String type, MultipartFile file);
}