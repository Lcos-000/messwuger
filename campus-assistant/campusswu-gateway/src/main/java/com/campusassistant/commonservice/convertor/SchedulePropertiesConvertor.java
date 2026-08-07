package com.campusassistant.commonservice.convertor;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.commonservice.pojo.vo.ScheduleConfigVO;
import com.campusassistant.config.ScheduleConfigProperties;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SchedulePropertiesConvertor extends BaseConvertor<ScheduleConfigProperties, ScheduleConfigVO> {
}
