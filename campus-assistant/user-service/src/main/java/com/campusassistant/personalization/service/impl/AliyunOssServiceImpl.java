package com.campusassistant.personalization.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.personalization.config.AliyunOssProperties;
import com.campusassistant.personalization.service.AliyunOssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AliyunOssServiceImpl implements AliyunOssService {

    private final AliyunOssProperties aliyunOssProperties;

    @Override
    public String upload(MultipartFile file, String objectKey) {
        OSS ossClient = null;
        try (InputStream inputStream = file.getInputStream()) {
            ossClient = new OSSClientBuilder().build(
                    aliyunOssProperties.getEndpoint(),
                    aliyunOssProperties.getAccessKeyId(),
                    aliyunOssProperties.getAccessKeySecret()
            );

            ossClient.putObject(
                    aliyunOssProperties.getBucketName(),
                    objectKey,
                    inputStream
            );

            return buildUrl(objectKey);
        } catch (Exception e) {
            log.error("上传 OSS 失败，objectKey={}", objectKey, e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private String buildUrl(String objectKey) {
        String prefix = aliyunOssProperties.getUrlPrefix();
        if (prefix.endsWith("/")) {
            return prefix + objectKey;
        }
        return prefix + "/" + objectKey;
    }
}