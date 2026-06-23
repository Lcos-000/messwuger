// 路径：mapper/CourseMapper.java
package com.campusassistant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusassistant.pojo.CourseEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseMapper extends BaseMapper<CourseEntity> {
}
