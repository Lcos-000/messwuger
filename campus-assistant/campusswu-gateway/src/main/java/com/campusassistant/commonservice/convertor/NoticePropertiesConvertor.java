package com.campusassistant.commonservice.convertor;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.commonservice.pojo.vo.NoticeResponseVO;
import com.campusassistant.config.NoticeProperties;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NoticePropertiesConvertor extends BaseConvertor<NoticeProperties, NoticeResponseVO> {
}
