package com.campusassistant.remote.spider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusassistant.remote.spider.pojo.PersonalInfoEntity;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface SyncMapper extends BaseMapper<PersonalInfoEntity> {
}
