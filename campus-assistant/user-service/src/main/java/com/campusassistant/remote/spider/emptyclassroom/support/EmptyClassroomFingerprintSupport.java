package com.campusassistant.remote.spider.emptyclassroom.support;

import com.campusassistant.remote.spider.emptyclassroom.pojo.EmptyClassroomFingerprintPayload;
import com.campusassistant.remote.spider.emptyclassroom.pojo.dto.EmptyClassroomConditionDTO;
import com.campusassistant.utils.converter.emptyclassroom.EmptyClassroomFingerprintPayloadConvertor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@RequiredArgsConstructor
public class EmptyClassroomFingerprintSupport {

    private final ObjectMapper objectMapper;
    private final EmptyClassroomFingerprintPayloadConvertor emptyClassroomFingerprintPayloadConvertor;

    public String buildFingerprint(EmptyClassroomConditionDTO dto) {
        try {
            EmptyClassroomFingerprintPayload payload = emptyClassroomFingerprintPayloadConvertor.toTarget(dto);

            String json = objectMapper.writeValueAsString(payload);
            return sha256(json);
        } catch (Exception e) {
            throw new IllegalStateException("生成空教室查询指纹失败", e);
        }
    }

    private String sha256(String text) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}