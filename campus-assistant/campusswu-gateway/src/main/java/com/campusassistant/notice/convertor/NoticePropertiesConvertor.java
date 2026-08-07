package com.campusassistant.notice.convertor;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.notice.pojo.vo.NoticeResponseVO;
import com.campusassistant.notice.properties.NoticeProperties;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NoticePropertiesConvertor extends BaseConvertor<NoticeProperties, NoticeResponseVO> {
}
