package com.campusassistant.remote.spider.sync.service.impl;

import com.campusassistant.pojo.Result;
import com.campusassistant.remote.course.client.CourseServiceClient;
import com.campusassistant.remote.spider.sync.mapper.SyncMapper;
import com.campusassistant.remote.spider.sync.pojo.dto.PersonalInfoDTO;
import com.campusassistant.remote.spider.sync.pojo.dto.SyncDataDTO;
import com.campusassistant.student.pojo.UserEntity;
import com.campusassistant.student.service.impl.support.UserReadSupport;
import com.campusassistant.student.service.impl.support.UserWriteSupport;
import com.campusassistant.utils.converter.grade.GradesDtoConvertor;
import com.campusassistant.utils.converter.personalinfo.PersonalInfoConvertor;
import com.campusassistant.utils.rediskey.CourseMixCacheKey;
import com.campusassistant.utils.rediskey.EmptyClassroomCacheKey;
import com.campusassistant.utils.rediskey.GradeCacheKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncServiceImplTest {

    @Mock private UserWriteSupport userWriteSupport;
    @Mock private UserReadSupport userReadSupport;
    @Mock private CourseServiceClient courseServiceClient;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private CourseMixCacheKey courseMixCacheKey;
    @Mock private GradeCacheKey gradeCacheKey;
    @Mock private PersonalInfoConvertor personalInfoConvertor;
    @Mock private SyncMapper syncMapper;
    @Mock private ObjectMapper objectMapper;
    @Mock private GradesDtoConvertor gradesDtoConvertor;
    @Mock private EmptyClassroomCacheKey emptyClassroomCacheKey;
    @Mock private com.campusassistant.remote.spider.emptyclassroom.support.EmptyClassroomFingerprintSupport fingerprintSupport;
    @Mock private com.campusassistant.remote.spider.emptyclassroom.support.EmptyClassCallbackCheckSupport callbackCheckSupport;

    private SyncServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SyncServiceImpl(
                userWriteSupport,
                userReadSupport,
                courseServiceClient,
                redisTemplate,
                courseMixCacheKey,
                gradeCacheKey,
                personalInfoConvertor,
                syncMapper,
                objectMapper,
                gradesDtoConvertor,
                emptyClassroomCacheKey,
                fingerprintSupport,
                callbackCheckSupport
        );
    }

    @Test
    void handleStudentDataSync_doesNotMarkSuccessWhenCourseServiceReturnsError() throws Exception {
        SyncDataDTO data = syncData();
        when(userReadSupport.findEntityByStudentId("2025001")).thenReturn(new UserEntity());
        when(syncMapper.selectOne(any())).thenReturn(null);
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(courseServiceClient.syncScheduleData(any())).thenReturn(Result.error("course unavailable"));

        assertThrows(RuntimeException.class, () -> service.handleStudentDataSync(data));

        verify(userWriteSupport, never()).updateSyncStatus(any(), any());
        verify(redisTemplate, never()).delete(any(String.class));
    }

    @Test
    void handleStudentDataSync_doesNotMarkSuccessWhenCourseServiceReturnsNull() throws Exception {
        SyncDataDTO data = syncData();
        when(userReadSupport.findEntityByStudentId("2025001")).thenReturn(new UserEntity());
        when(syncMapper.selectOne(any())).thenReturn(null);
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        when(courseServiceClient.syncScheduleData(any())).thenReturn(null);

        assertThrows(RuntimeException.class, () -> service.handleStudentDataSync(data));

        verify(userWriteSupport, never()).updateSyncStatus(any(), any());
        verify(redisTemplate, never()).delete(any(String.class));
    }

    private SyncDataDTO syncData() {
        SyncDataDTO data = new SyncDataDTO();
        data.setStudentId("2025001");
        data.setAcademicYear("2025-2026");
        data.setSemester("12");
        data.setPersonalInfoDTO(new PersonalInfoDTO());
        return data;
    }
}
