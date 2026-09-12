package com.campusassistant.service.impl;

import com.campusassistant.converter.course.CourseDtoConvertor;
import com.campusassistant.converter.course.CourseVoConvertor;
import com.campusassistant.mapper.CourseMapper;
import com.campusassistant.mapper.GradeMapper;
import com.campusassistant.pojo.schedule.CourseDTO;
import com.campusassistant.pojo.schedule.CourseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCourseServiceImplTest {

    @Mock private CourseMapper courseMapper;
    @Mock private CourseDtoConvertor courseDtoConvertor;
    @Mock private CourseVoConvertor courseVoConvertor;
    @Mock private GradeMapper gradeMapper;

    private UserCourseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserCourseServiceImpl(
                courseMapper,
                courseDtoConvertor,
                courseVoConvertor,
                gradeMapper,
                null,
                null
        );
    }

    @Test
    void saveOrUpdateSchedule_usesAtomicTermAwareUpsert() {
        CourseDTO dto = new CourseDTO();
        dto.setStudentId("2025001");
        dto.setAcademicYear("2025-2026");
        dto.setSemester("12");
        dto.setScheduleJson("[]");
        CourseEntity entity = new CourseEntity();

        when(courseDtoConvertor.toSource(dto)).thenReturn(entity);

        service.saveOrUpdateSchedule(dto);

        verify(courseMapper).upsertSchedule(entity);
        verify(courseMapper, never()).selectOne(org.mockito.ArgumentMatchers.any());
        verify(courseMapper, never()).insert(org.mockito.ArgumentMatchers.any());
        verify(courseMapper, never()).updateById(org.mockito.ArgumentMatchers.any());
    }
}
