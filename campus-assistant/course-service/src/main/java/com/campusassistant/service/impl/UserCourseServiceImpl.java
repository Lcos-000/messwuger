// 路径：service/impl/UserCourseServiceImpl.java
package com.campusassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusassistant.converter.grade.GradeDtoConvertor;
import com.campusassistant.converter.course.CourseDtoConvertor;
import com.campusassistant.converter.course.CourseVoConvertor;
import com.campusassistant.converter.grade.GradeVoConvertor;
import com.campusassistant.mapper.CourseMapper;
import com.campusassistant.mapper.GradeMapper;
import com.campusassistant.pojo.grade.dto.GradeDTO;
import com.campusassistant.pojo.grade.GradeEntity;
import com.campusassistant.pojo.grade.dto.GradeItemDTO;
import com.campusassistant.pojo.grade.GradeVO;
import com.campusassistant.pojo.schedule.CourseDTO;
import com.campusassistant.pojo.schedule.CourseEntity;
import com.campusassistant.pojo.schedule.CourseVO;
import com.campusassistant.service.UserCourseService;
import com.campusassistant.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCourseServiceImpl implements UserCourseService {

    private final CourseMapper courseMapper;
    private final CourseDtoConvertor courseDtoConverter;
    private final CourseVoConvertor courseVoConverter;
    private final GradeMapper gradeMapper;
    private final GradeDtoConvertor gradeDtoConvertor;
    private final GradeVoConvertor gradeVoConvertor;

    @Override
    @Transactional
    public void saveOrUpdateSchedule(CourseDTO courseDTO) {
        // 由学号、学年、学期联合唯一键保证每学期一条记录，并由数据库原子 upsert
        // 避免 check-then-act 在并发同步时产生重复课表。
        courseMapper.upsertSchedule(courseDtoConverter.toSource(courseDTO));
    }

    @Override
    public CourseVO getSchedule() {
        String studentId = UserContextUtil.requireStudentId();
        LambdaQueryWrapper<CourseEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseEntity::getStudentId, studentId)
                .orderByDesc(CourseEntity::getId)
                .last("LIMIT 1");
        CourseEntity entity = courseMapper.selectOne(queryWrapper);
        if (entity == null) {
            return null; // 或者返回一个空的 CourseVO()，视你前端需求而定
        }
        return courseVoConverter.toTarget(entity);
    }

    @Override
    public CourseVO getCurrentSchedule() {
        String studentId = UserContextUtil.requireStudentId();
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

    @Override
    public void saveOrUpdateGrades(GradeDTO gradeDTO) {
        String studentId = gradeDTO.getStudentId();
        String academicYear = gradeDTO.getAcademicYear();
        String semester = gradeDTO.getSemester();

        LambdaQueryWrapper<GradeEntity> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper
                .eq(GradeEntity::getStudentId, studentId)
                .eq(GradeEntity::getAcademicYear, academicYear)
                .eq(GradeEntity::getSemester, semester);

        // 先删旧数据
        gradeMapper.delete(deleteWrapper);

        // 如果本次回调为空列表，表示该学期成绩为空，删完即可
        if (gradeDTO.getGrades() == null || gradeDTO.getGrades().isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<GradeEntity> gradeEntities = new ArrayList<>();

        for (GradeItemDTO item : gradeDTO.getGrades()) {
            GradeEntity gradeEntity = gradeDtoConvertor.toSource(item);
            gradeEntity.setStudentId(studentId);
            gradeEntity.setAcademicYear(academicYear);
            gradeEntity.setSemester(semester);
            gradeEntity.setSyncTime(now);
            gradeEntities.add(gradeEntity);
        }

        for (GradeEntity gradeEntity : gradeEntities) {
            gradeMapper.insert(gradeEntity);
        }
    }

    @Override
    public List<GradeVO> getGradesByStudentId(String studentId, String academicYear, String semester) {
        LambdaQueryWrapper<GradeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GradeEntity::getStudentId, studentId)
                .eq(GradeEntity::getAcademicYear, academicYear)
                .eq(GradeEntity::getSemester, semester)
                .orderByAsc(GradeEntity::getId);

        List<GradeEntity> entities = gradeMapper.selectList(queryWrapper);
        return gradeVoConvertor.toTarget(entities);
    }

}
