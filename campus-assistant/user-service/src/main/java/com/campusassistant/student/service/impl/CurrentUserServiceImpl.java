package com.campusassistant.student.service.impl;

import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.pojo.Result;
import com.campusassistant.properties.JwtProperties;
import com.campusassistant.remote.course.pojo.RemoteGradeVO;
import com.campusassistant.remote.course.service.UserGradeService;
import com.campusassistant.remote.spider.emptyclassroom.code.EmptyClassroomQueryStatusEnum;
import com.campusassistant.remote.spider.emptyclassroom.pojo.dto.EmptyClassroomCallbackDTO;
import com.campusassistant.remote.spider.emptyclassroom.pojo.dto.EmptyClassroomTaskSubmitDTO;
import com.campusassistant.remote.spider.emptyclassroom.support.EmptyClassroomFingerprintSupport;
import com.campusassistant.student.pojo.dto.EmptyClassroomQueryDTO;
import com.campusassistant.student.pojo.dto.GradesQueryDTO;
import com.campusassistant.remote.spider.grades.pojo.dto.GradesTaskSubmitDTO;
import com.campusassistant.remote.spider.sync.pojo.entity.PersonalInfoEntity;
import com.campusassistant.remote.spider.sync.pojo.vo.PersonalInfoVO;
import com.campusassistant.remote.spider.common.service.SpiderService;
import com.campusassistant.student.pojo.vo.EmptyClassroomQueryResultVO;
import com.campusassistant.student.service.impl.support.UserCacheSupport;
import com.campusassistant.student.service.impl.support.UserWriteSupport;
import com.campusassistant.utils.UserContextUtil;
import com.campusassistant.service.CommonCacheService;
import com.campusassistant.utils.converter.emptyclassroom.EmptyClassroomQueryResultFromCallbackConvertor;
import com.campusassistant.utils.converter.emptyclassroom.EmptyClassroomQueryResultFromQueryConvertor;
import com.campusassistant.utils.converter.emptyclassroom.EmptyClassroomSubmitDtoConvertor;
import com.campusassistant.utils.converter.personalinfo.PersonalInfoVoConvertor;
import com.campusassistant.utils.converter.user.UserStatusVoConvertor;
import com.campusassistant.student.pojo.UserEntity;
import com.campusassistant.student.pojo.vo.UserStatusVO;
import com.campusassistant.student.service.impl.support.UserReadSupport;
import com.campusassistant.student.service.CurrentUserService;
import com.campusassistant.utils.rediskey.EmptyClassroomCacheKey;
import com.campusassistant.utils.rediskey.user.UserPersonalCacheKey;
import com.campusassistant.utils.rediskey.user.UserPwdCacheKey;
import com.campusassistant.utils.rediskey.user.UserStatusCacheKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.campusassistant.student.pojo.vo.EmptyClassroomTaskSubmitVO;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.campusassistant.enums.ResultCodeEnum.UNAUTHORIZED;
import static com.campusassistant.remote.spider.emptyclassroom.code.EmptyClassroomQueryResultStatusConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {
    private final UserReadSupport userReadSupport;
    private final UserWriteSupport userWriteSupport;
    private final UserStatusVoConvertor userStatusVoConvertor;
    private final CommonCacheService commonCacheService;
    private final UserStatusCacheKey userStatusCacheKey;
    private final UserPwdCacheKey userPwdCacheKey;
    private final StringRedisTemplate stringRedisTemplate;
    private final SpiderService spiderService;
    private final UserPersonalCacheKey userPersonalCacheKey;
    private final PersonalInfoVoConvertor personalInfoVoConvertor;
    private final UserCacheSupport userCacheSupport;
    private final JwtProperties jwtProperties;
    private final UserGradeService userGradeService;
    private final EmptyClassroomSubmitDtoConvertor emptyClassroomSubmitDtoConvertor;
    private final EmptyClassroomFingerprintSupport emptyClassroomFingerprintSupport;
    private final EmptyClassroomCacheKey emptyClassroomCacheKey;
    private final EmptyClassroomQueryResultFromQueryConvertor emptyClassroomQueryResultFromQueryConvertor;
    private final EmptyClassroomQueryResultFromCallbackConvertor emptyClassroomQueryResultFromCallbackConvertor;
    private final ObjectMapper objectMapper;

    @Override
    public void self_unsubscribe(HttpServletRequest request) {
        String token = request.getHeader(jwtProperties.getJwtTokenName());
        String currentStudentId = UserContextUtil.requireStudentId();
        UserEntity userEntity = userReadSupport.findEntityByStudentId(currentStudentId);
        if (userEntity == null) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND.getCode(),"用户不存在");
        }
        Long userId = userEntity.getId();
        log.info("用户正在执行注销操作，用户id：[{}]，用户学号：[{}]",userId,currentStudentId);
        int rows = userWriteSupport.deleteUserById(userId);
        if (rows == 0) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND.getCode(),"操作失败");
        }

        if (!currentStudentId.isEmpty()) {
            userCacheSupport.evictLoginSessionAndUserCaches(currentStudentId, token);
        } else {
            log.warn("用户注销时发现用户名为空，跳过缓存删除, userId: {}", userId);
        }
        log.info("用户已注销；[{}]",userEntity);
    }

    @Override
    public UserStatusVO getStatusByStudentId() {
        String studentId = UserContextUtil.requireStudentId();
        return commonCacheService.getWithCache(
                userStatusCacheKey.getKey(studentId),
                UserStatusVO.class,
                () -> {
                    UserEntity entity = userReadSupport.findEntityByStudentId(studentId);
                    return entity == null ? null : userStatusVoConvertor.toTarget(entity);
                }
        );
    }

    @Override
    public PersonalInfoVO getPersonalByStudentId() {
        String studentId = UserContextUtil.requireStudentId();
        return commonCacheService.getWithCache(
                userPersonalCacheKey.getKey(studentId),
                PersonalInfoVO.class,
                () -> {
                    PersonalInfoEntity personalInfoEntity = userReadSupport.findPersonalInfoByStudentId(studentId);
                    return personalInfoEntity == null ? null : personalInfoVoConvertor.toTarget(personalInfoEntity);
                }
        );
    }

    @Override
    public void refreshData() {
        String studentId = UserContextUtil.requireStudentId();
        String redisKey = userPwdCacheKey.getKey(studentId);
        String encryptedPassword = stringRedisTemplate.opsForValue().get(redisKey);
        // 判断密码是否还在缓存中
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            // 如果 Redis 里没密码了，说明登录太久（缓存过期）或 Redis 被清理
            throw new BusinessException(UNAUTHORIZED);
        }
        // 密码完好，直接异步触发爬虫
        spiderService.asyncStartFullCrawl(studentId, encryptedPassword);
    }

    @Override
    public void updateAutoPunchEnabled(Integer enabled) {
        String studentId = UserContextUtil.requireStudentId();
        userWriteSupport.updateAutoPunchEnabled(studentId, enabled);
        stringRedisTemplate.delete(userStatusCacheKey.getKey(studentId));
    }

    @Override
    public Result<?> submitGradesTask(GradesQueryDTO dto) {
        String studentId = UserContextUtil.requireStudentId();
        String encryptedPassword = stringRedisTemplate.opsForValue().get(userPwdCacheKey.getKey(studentId));

        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            throw new BusinessException(UNAUTHORIZED);
        }

        GradesTaskSubmitDTO submitDTO = new GradesTaskSubmitDTO();
        submitDTO.setAcademicYear(dto.getAcademicYear());
        submitDTO.setSemester(dto.getSemester());

        return spiderService.submitGradesTask(studentId, encryptedPassword, submitDTO);

    }

    @Override
    public List<RemoteGradeVO> getGrades(String academicYear, String semester) {
        return userGradeService.getGradesWithCache(academicYear, semester);
    }

    @Override
    public Result<?> submitEmptyClassroomTask(EmptyClassroomQueryDTO dto) {
        String studentId = UserContextUtil.requireStudentId();
        String encryptedPassword = stringRedisTemplate.opsForValue().get(userPwdCacheKey.getKey(studentId));

        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            throw new BusinessException(UNAUTHORIZED);
        }

        EmptyClassroomTaskSubmitDTO submitDTO = emptyClassroomSubmitDtoConvertor.toTarget(dto);

        String fingerprint = emptyClassroomFingerprintSupport.buildFingerprint(submitDTO);
        String resultKey = emptyClassroomCacheKey.getResultKey(fingerprint);
        String statusKey = emptyClassroomCacheKey.getStatusKey(fingerprint);

        // 先查结果缓存：有结果直接返回
        if (stringRedisTemplate.hasKey(resultKey)) {
            return Result.success(
                    EmptyClassroomTaskSubmitVO.of(RESULT_READY, true, false)
            );
        }

        // 再查状态缓存
        String currentStatus = stringRedisTemplate.opsForValue().get(statusKey);
        if (EmptyClassroomQueryStatusEnum.QUERYING.getCode().equals(currentStatus)) {
            return Result.success(
                    EmptyClassroomTaskSubmitVO.of(QUERYING, false, false)
            );
        }
        if (EmptyClassroomQueryStatusEnum.TIMEOUT.getCode().equals(currentStatus)) {
            return Result.success(
                    EmptyClassroomTaskSubmitVO.of(TIMEOUT, false, false)
            );
        }

        // FAILED 允许重试，先删掉旧状态再继续抢锁
        if (EmptyClassroomQueryStatusEnum.FAILED.getCode().equals(currentStatus)) {
            stringRedisTemplate.delete(statusKey);
        }

        // 原子设置 QUERYING，防止并发重复触发
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(
                statusKey,
                EmptyClassroomQueryStatusEnum.QUERYING.getCode(),
                90,TimeUnit.SECONDS
        );

        if (!Boolean.TRUE.equals(locked)) {
            return Result.success(
                    EmptyClassroomTaskSubmitVO.of(QUERYING, false, false)
            );
        }

        // 真正调用 Go
        try {
            Result<?> submitResult = spiderService.submitEmptyClassroomTask(studentId, encryptedPassword, submitDTO);

            if (submitResult == null ||
                    submitResult.getCode() == null ||
                    !submitResult.getCode().equals(ResultCodeEnum.SUCCESS.getCode())) {

                stringRedisTemplate.opsForValue().set(
                        statusKey,
                        EmptyClassroomQueryStatusEnum.FAILED.getCode(),
                        2, TimeUnit.MINUTES
                );

                throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR.getCode(), "提交空教室查询任务失败");
            }

            return Result.success(
                    EmptyClassroomTaskSubmitVO.of(SUBMITTED, false, true)
            );
        } catch (BusinessException e) {
            stringRedisTemplate.opsForValue().set(
                    statusKey,
                    EmptyClassroomQueryStatusEnum.FAILED.getCode(),
                    2, TimeUnit.MINUTES
            );
            throw e;
        } catch (Exception e) {
            stringRedisTemplate.opsForValue().set(
                    statusKey,
                    EmptyClassroomQueryStatusEnum.FAILED.getCode(),
                    2, TimeUnit.MINUTES
            );
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR.getCode(), "提交空教室查询任务异常");
        }
    }

    @Override
    public EmptyClassroomQueryResultVO getEmptyClassroomResult(EmptyClassroomQueryDTO dto) {
        String studentId = UserContextUtil.requireStudentId();
        String encryptedPassword = stringRedisTemplate.opsForValue().get(userPwdCacheKey.getKey(studentId));

        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            throw new BusinessException(UNAUTHORIZED);
        }

        EmptyClassroomTaskSubmitDTO submitDTO = emptyClassroomSubmitDtoConvertor.toTarget(dto);

        String fingerprint = emptyClassroomFingerprintSupport.buildFingerprint(submitDTO);
        String resultKey = emptyClassroomCacheKey.getResultKey(fingerprint);
        String statusKey = emptyClassroomCacheKey.getStatusKey(fingerprint);

        String resultJson = stringRedisTemplate.opsForValue().get(resultKey);
        if (resultJson != null && !resultJson.isBlank()) {
            try {
                EmptyClassroomCallbackDTO callbackDTO = objectMapper.readValue(resultJson, EmptyClassroomCallbackDTO.class);

                EmptyClassroomQueryResultVO vo = emptyClassroomQueryResultFromCallbackConvertor.toTarget(callbackDTO);
                vo.setQueryStatus(RESULT_READY);
                vo.setResultReady(true);
                return vo;
            } catch (Exception e) {
                log.error("解析空教室结果缓存失败，fingerprint: {}", fingerprint, e);
                throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR.getCode(), "解析空教室查询结果失败");
            }
        }

        String currentStatus = stringRedisTemplate.opsForValue().get(statusKey);

        EmptyClassroomQueryResultVO vo = emptyClassroomQueryResultFromQueryConvertor.toTarget(dto);
        vo.setResultReady(false);
        vo.setClassrooms(List.of());
        if (EmptyClassroomQueryStatusEnum.QUERYING.getCode().equals(currentStatus)) {
            vo.setQueryStatus(QUERYING);
            return vo;
        }
        if (EmptyClassroomQueryStatusEnum.TIMEOUT.getCode().equals(currentStatus)) {
            vo.setQueryStatus(TIMEOUT);
            return vo;
        }
        vo.setQueryStatus(FAILED);
        return vo;
    }

}
