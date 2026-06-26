package com.campusassistant.personalization.service;

import org.springframework.web.multipart.MultipartFile;

public interface AliyunOssService {

    String upload(MultipartFile file, String objectKey);
}