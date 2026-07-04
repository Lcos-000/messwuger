package com.campusassistant.remote.spider.sync.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusassistant.remote.spider.sync.pojo.entity.PersonalInfoEntity;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface SyncMapper extends BaseMapper<PersonalInfoEntity> {
}
