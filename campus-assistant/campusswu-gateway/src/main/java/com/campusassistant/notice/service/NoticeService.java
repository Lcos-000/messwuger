package com.campusassistant.notice.service;

import com.campusassistant.notice.convertor.NoticePropertiesConvertor;
import com.campusassistant.notice.pojo.vo.NoticeResponseVO;
import com.campusassistant.notice.properties.NoticeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeProperties noticeProperties;
    private final NoticePropertiesConvertor noticePropertiesConvertor;

    public NoticeResponseVO getCurrentNotice() {
        log.info("获取到公告版本[{}]，标题[{}]，内容[{}]，等级[{}]，更新时间[{}]",
                noticeProperties.getVersion(),
                noticeProperties.getTitle(),
                noticeProperties.getContent(),
                noticeProperties.getLevel(),
                noticeProperties.getUpdatedAt());
        return noticePropertiesConvertor.toTarget(noticeProperties);
    }

}