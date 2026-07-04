package com.campusassistant.remote.spider.sync.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.pojo.Result;
import com.campusassistant.remote.course.client.CourseServiceClient;
import com.campusassistant.remote.course.pojo.RemoteGradeBatchDTO;
import com.campusassistant.remote.course.pojo.RemoteGradeDTO;
import com.campusassistant.remote.course.pojo.schedule.RemoteCourseDTO;
import com.campusassistant.remote.spider.grades.pojo.dto.GradesCallbackDTO;
import com.campusassistant.remote.spider.sync.mapper.SyncMapper;
import com.campusassistant.remote.spider.sync.pojo.entity.PersonalInfoEntity;
import com.campusassistant.remote.spider.sync.pojo.dto.SyncDataDTO;
import com.campusassistant.remote.spider.sync.service.SyncService;
import com.campusassistant.student.code.PunchStatusEnum;
import com.campusassistant.student.pojo.UserEntity;
import com.campusassistant.student.service.impl.support.UserReadSupport;
import com.campusassistant.student.service.impl.support.UserWriteSupport;
import com.campusassistant.utils.converter.grade.GradesDtoConvertor;
import com.campusassistant.utils.converter.personalinfo.PersonalInfoConvertor;
import com.campusassistant.utils.rediskey.CourseMixCacheKey;
import com.campusassistant.utils.rediskey.GradeCacheKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.campusassistant.student.code.SyncStatusEnum.SYNCING_SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncServiceImpl  implements SyncService {

    private final UserWriteSupport userWriteSupport;
    private final UserReadSupport userReadSupport;
    private final CourseServiceClient courseServiceClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final CourseMixCacheKey courseMixCacheKey;
    private final GradeCacheKey gradeCacheKey;
    private final PersonalInfoConvertor personalInfoConvertor;
    private final SyncMapper syncMapper;
    private final ObjectMapper objectMapper;
    private final GradesDtoConvertor gradesDtoConvertor;



    @Override
    @Transactional// 开启事务，保证本地用户信息更新和状态变更的一致性
    public void handleStudentDataSync(SyncDataDTO syncDataDTO) {
        String studentId = syncDataDTO.getStudentId();

        log.info("接收到爬虫回推数据，准备处理。学号: {}", studentId);

        //  校验用户是否存在
        UserEntity userEntity = userReadSupport.findEntityByStudentId(studentId);
        if (userEntity == null) {
            log.error("爬虫回调失败：未找到对应的本地用户。学号: {}", studentId);
            throw new BusinessException(ResultCodeEnum.NOT_FOUND.getCode(), "未找到对应的本地用户");
        }

        try {
            // 修改基本信息
            PersonalInfoEntity personalInfoEntity = personalInfoConvertor.toSource(syncDataDTO.getPersonalInfoDTO());
            LambdaQueryWrapper<PersonalInfoEntity> lqw = new LambdaQueryWrapper<>();
            lqw.eq(PersonalInfoEntity::getStudentId, studentId);
            PersonalInfoEntity entity = syncMapper.selectOne(lqw);

            if (entity == null) {
                syncMapper.insert(personalInfoEntity);
            }
            else {
                syncMapper.update(personalInfoEntity,lqw);
            }

            RemoteCourseDTO remoteCourseDTO = new RemoteCourseDTO();
            remoteCourseDTO.setStudentId(syncDataDTO.getStudentId());
            remoteCourseDTO.setSemester(syncDataDTO.getSemester());
            remoteCourseDTO.setAcademicYear(syncDataDTO.getAcademicYear());
            String scheduleJson = objectMapper.writeValueAsString(syncDataDTO.getScheduleData());
            remoteCourseDTO.setScheduleJson(scheduleJson);

            // 远程调用写入 course-service
            Result<String> stringResult = courseServiceClient.syncScheduleData(remoteCourseDTO);
            log.info("调用 Course 服务同步课表数据，结果: {}", stringResult);

            // 重点：清空 user-service 中旧的 Redis 缓存，保证下次查出最新数据
            String redisKey = courseMixCacheKey.getKey(remoteCourseDTO.getStudentId());
            stringRedisTemplate.delete(redisKey);

            //  将同步状态置为成功
            userWriteSupport.updateSyncStatus(studentId, SYNCING_SUCCESS.getCode());
            log.info("学号: {} 数据回推处理完成！", studentId);
        }catch (Exception e){
            log.error("将课表数据转发至 Course 服务或处理 JSON 时出错", e);
            // 这里可以视情况决定是否要让事务回滚 (throw new RuntimeException)
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR.getCode(), "处理课表数据出错");
        }

    }

    @Override
    public void handlePunchResult(String studentId, Boolean success) {
        Integer targetStatus = success ? PunchStatusEnum.PUNCH_SUCCESS.getCode()
                : PunchStatusEnum.PUNCH_FAILED.getCode();
        userWriteSupport.updatePunchStatus(studentId, targetStatus);
        log.info("学号: {} 打卡回调处理完成，结果: {}", studentId, success ? "成功" : "失败");
    }

    @Override
    public void handleGradesCallback(GradesCallbackDTO gradesCallbackDTO) {
        if (gradesCallbackDTO == null) {
            log.warn("接收到空的成绩回调数据");
            return;
        }

        String studentId = gradesCallbackDTO.getStudentId();
        String academicYear = gradesCallbackDTO.getAcademicYear();
        String semester = gradesCallbackDTO.getSemester();

        log.info("接收到成绩回调，学号: {}, 学年: {}, 学期: {}, 成绩条数: {}",
                studentId,
                academicYear,
                semester,
                gradesCallbackDTO.getGrades() == null ? 0 : gradesCallbackDTO.getGrades().size());

        if (studentId == null || studentId.isBlank()) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "异常参数studentId为空");
        }
        if (academicYear == null || academicYear.isBlank()) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "异常参数academicYear为空");
        }
        if (semester == null || semester.isBlank()) {
            throw new BusinessException(ResultCodeEnum.PARAM_ERROR.getCode(), "异常参数semester为空");
        }
        if (userReadSupport.findEntityByStudentId(studentId) == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND.getCode(), "未找到对应用户");
        }

        List<RemoteGradeDTO> remoteGrades =
                gradesCallbackDTO.getGrades() == null ?
                new ArrayList<>() : gradesDtoConvertor.toTarget(gradesCallbackDTO.getGrades());

        for (RemoteGradeDTO dto : remoteGrades) {
            dto.setStudentId(studentId);
            dto.setAcademicYear(academicYear);
            dto.setSemester(semester);
        }

        RemoteGradeBatchDTO batchDTO = new RemoteGradeBatchDTO();
        batchDTO.setStudentId(studentId);
        batchDTO.setAcademicYear(academicYear);
        batchDTO.setSemester(semester);
        batchDTO.setGrades(remoteGrades);

        Result<String> result = courseServiceClient.syncGradeData(batchDTO);
        log.info("调用 Course 服务同步成绩数据，结果: {}", result);

        if (result == null || result.getCode() == null || !result.getCode().equals(ResultCodeEnum.SUCCESS.getCode())) {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR.getCode(), "同步成绩到Course服务失败");
        }

        stringRedisTemplate.delete(gradeCacheKey.getKey(studentId, academicYear, semester));
    }

}
