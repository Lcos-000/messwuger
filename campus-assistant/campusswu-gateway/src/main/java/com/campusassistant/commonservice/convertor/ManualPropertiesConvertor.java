package com.campusassistant.commonservice.convertor;

import com.campusassistant.common.converter.BaseConvertor;
import com.campusassistant.commonservice.pojo.vo.ManualConfigVO;
import com.campusassistant.config.ManualConfigProperties;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManualPropertiesConvertor extends BaseConvertor<ManualConfigProperties, ManualConfigVO> {
}
