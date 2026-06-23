package com.campusassistant.remote.spider.service;

import com.campusassistant.remote.spider.pojo.SyncDataDTO;

public interface SyncService {
    void handleStudentDataSync(SyncDataDTO syncDataDTO);

    void handlePunchResult(String studentId, Boolean success);
}
