// 路径：service/impl/UserCourseServiceImpl.java
package com.campusassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusassistant.pojo.UserContext;
import com.campusassistant.converter.CourseDtoConvertor;
import com.campusassistant.converter.CourseVoConvertor;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.mapper.CourseMapper;
import com.campusassistant.pojo.CourseDTO;
import com.campusassistant.pojo.CourseEntity;
import com.campusassistant.pojo.CourseVO;
import com.campusassistant.service.UserCourseService;
import com.campusassistant.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.campusassistant.enums.ResultCodeEnum.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class UserCourseServiceImpl implements UserCourseService {

    private final CourseMapper courseMapper;
    private final CourseDtoConvertor courseDtoConverter;
    private final CourseVoConvertor courseVoConverter;

    @Override
    public void saveOrUpdateSchedule(CourseDTO courseDTO) {
        // 先查询数据库里是否已经存在该生、该学期的课表
        LambdaQueryWrapper<CourseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseEntity::getStudentId, courseDTO.getStudentId());
//                .eq(CourseEntity::getAcademicYear, courseDTO.getAcademicYear())
//                .eq(CourseEntity::getSemester, courseDTO.getSemester());

        CourseEntity existEntity = courseMapper.selectOne(queryWrapper);
        if (existEntity != null) {
            //  如果存在，直接覆盖更新 scheduleJson
            existEntity.setScheduleJson(courseDTO.getScheduleJson());
            courseMapper.updateById(existEntity);
        } else {
            // 3. 如果不存在，新增一条记录
            CourseEntity newEntity = courseDtoConverter.toSource(courseDTO);
            courseMapper.insert(newEntity);
        }
    }

    @Override
    public CourseVO getSchedule() {
        UserContext userContext = ThreadLocalUtil.get();
        if (userContext==null){
            throw new BusinessException(UNAUTHORIZED);
        }
        String studentId = userContext.getStudentId();
        LambdaQueryWrapper<CourseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseEntity::getStudentId, studentId);
//                .eq(CourseEntity::getAcademicYear, academicYear)
//                .eq(CourseEntity::getSemester, semester);
        CourseEntity entity = courseMapper.selectOne(queryWrapper);
        if (entity == null) {
            return null; // 或者返回一个空的 CourseVO()，视你前端需求而定
        }
        return courseVoConverter.toTarget(entity);
    }

    @Override
    public CourseVO getCurrentSchedule() {
        UserContext userContext = ThreadLocalUtil.get();
        if (userContext==null){
            throw new BusinessException(UNAUTHORIZED);
        }
        String studentId = userContext.getStudentId();
        // 取出该学生最新的一条课表记录 (按 ID 倒序排)
        LambdaQueryWrapper<CourseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseEntity::getStudentId, studentId)
                .orderByDesc(CourseEntity::getId)
                .last("LIMIT 1");

        CourseEntity entity = courseMapper.selectOne(queryWrapper);
        if (entity == null) {
            return null;
        }
        return courseVoConverter.toTarget(entity);
    }

    @Override
    public CourseVO getScheduleByStudentId(String studentId) {
        LambdaQueryWrapper<CourseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseEntity::getStudentId, studentId)
                .orderByDesc(CourseEntity::getId);
        IPage<CourseEntity> page = new Page<>(1, 1);
        IPage<CourseEntity> resultPage = courseMapper.selectPage(page, queryWrapper);
        List<CourseEntity> records = resultPage.getRecords();
        if (records.isEmpty()) {
            return null;
        }
        return courseVoConverter.toTarget(records.get(0));
    }


}
