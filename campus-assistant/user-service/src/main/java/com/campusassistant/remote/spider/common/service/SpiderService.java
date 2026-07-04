package com.campusassistant.remote.spider.common.service;

import com.campusassistant.pojo.Result;
import com.campusassistant.remote.spider.grades.pojo.dto.GradesTaskSubmitDTO;

public interface SpiderService {

    boolean validateCredentials(String studentId, String encryptedPassword);

    void asyncStartFullCrawl(String studentId, String encryptedPassword);

    void asyncStartPunchCard(String studentId, String encryptedPassword);

    Result<?> submitGradesTask(String studentId, String encryptedPassword, GradesTaskSubmitDTO dto);

}
