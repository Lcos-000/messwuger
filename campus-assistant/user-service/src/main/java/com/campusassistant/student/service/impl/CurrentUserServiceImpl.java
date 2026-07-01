package com.campusassistant.student.service.impl;

import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.properties.JwtProperties;
import com.campusassistant.remote.spider.pojo.PersonalInfoEntity;
import com.campusassistant.remote.spider.pojo.PersonalInfoVO;
import com.campusassistant.remote.spider.service.SpiderService;
import com.campusassistant.student.service.impl.support.UserCacheSupport;
import com.campusassistant.student.service.impl.support.UserWriteSupport;
import com.campusassistant.utils.UserContextUtil;
import com.campusassistant.service.CommonCacheService;
import com.campusassistant.utils.converter.personalinfo.PersonalInfoVoConvertor;
import com.campusassistant.utils.converter.user.UserStatusVoConvertor;
import com.campusassistant.student.pojo.UserEntity;
import com.campusassistant.student.pojo.UserStatusVO;
import com.campusassistant.student.service.impl.support.UserReadSupport;
import com.campusassistant.student.service.CurrentUserService;
import com.campusassistant.utils.rediskey.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.campusassistant.enums.ResultCodeEnum.UNAUTHORIZED;

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

}
