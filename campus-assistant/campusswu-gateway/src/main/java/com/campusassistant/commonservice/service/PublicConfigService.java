package com.campusassistant.commonservice.service;

import com.campusassistant.commonservice.pojo.vo.ManualConfigVO;
import com.campusassistant.commonservice.pojo.vo.ScheduleConfigVO;
import com.campusassistant.commonservice.pojo.vo.NoticeResponseVO;

public interface PublicConfigService {
    
    NoticeResponseVO getCurrentNotice();

    ManualConfigVO getManualConfig();

    ScheduleConfigVO getScheduleConfig();
}
