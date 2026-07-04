package com.campusassistant.remote.spider.sync.service;

import com.campusassistant.remote.spider.grades.pojo.dto.GradesCallbackDTO;
import com.campusassistant.remote.spider.sync.pojo.dto.SyncDataDTO;

public interface SyncService {
    void handleStudentDataSync(SyncDataDTO syncDataDTO);

    void handlePunchResult(String studentId, Boolean success);

    void handleGradesCallback(GradesCallbackDTO gradesCallbackDTO);

}
