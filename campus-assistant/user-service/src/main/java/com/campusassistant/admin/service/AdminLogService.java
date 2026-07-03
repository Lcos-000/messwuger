package com.campusassistant.admin.service;

import com.campusassistant.admin.pojo.vo.AdminLogFileListVO;
import com.campusassistant.admin.pojo.vo.AdminLogTailVO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface AdminLogService {

    AdminLogFileListVO listLogFiles();

    AdminLogTailVO initTail(String fileName);

    AdminLogTailVO pollTail(String fileName, Long offset);

    AdminLogTailVO historyTail(String fileName, Long beforeOffset);

    ResponseEntity<Resource> downloadLog(String fileName);
}