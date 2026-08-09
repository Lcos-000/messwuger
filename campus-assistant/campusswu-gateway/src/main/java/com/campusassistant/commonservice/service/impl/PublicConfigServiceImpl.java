package com.campusassistant.commonservice.service.impl;

import com.campusassistant.commonservice.convertor.ManualPropertiesConvertor;
import com.campusassistant.commonservice.convertor.NoticePropertiesConvertor;
import com.campusassistant.commonservice.convertor.SchedulePropertiesConvertor;
import com.campusassistant.commonservice.pojo.vo.ManualConfigVO;
import com.campusassistant.commonservice.pojo.vo.ScheduleConfigVO;
import com.campusassistant.commonservice.pojo.vo.NoticeResponseVO;
import com.campusassistant.commonservice.service.PublicConfigService;
import com.campusassistant.config.ManualConfigProperties;
import com.campusassistant.config.NoticeProperties;
import com.campusassistant.config.ScheduleConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicConfigServiceImpl implements PublicConfigService {

    private final NoticeProperties noticeProperties;
    private final ManualConfigProperties manualConfigProperties;
    private final ScheduleConfigProperties scheduleConfigProperties;
    private final NoticePropertiesConvertor noticePropertiesConvertor;
    private final ManualPropertiesConvertor manualPropertiesConvertor;
    private final SchedulePropertiesConvertor schedulePropertiesConvertor;

    @Override
    public NoticeResponseVO getCurrentNotice() {
        log.info("获取到公告:版本[{}]，标题[{}]，等级[{}]，更新时间[{}]",
                noticeProperties.getVersion(),
                noticeProperties.getTitle(),
                noticeProperties.getLevel(),
                noticeProperties.getUpdatedAt());
        return noticePropertiesConvertor.toTarget(noticeProperties);
    }

    @Override
    public ManualConfigVO getManualConfig() {
        log.info("获取到手册配置");
        return manualPropertiesConvertor.toTarget(manualConfigProperties);
    }

    @Override
    public ScheduleConfigVO getScheduleConfig() {
        log.info("获取到时间表配置");
        return schedulePropertiesConvertor.toTarget(scheduleConfigProperties);
    }
}
