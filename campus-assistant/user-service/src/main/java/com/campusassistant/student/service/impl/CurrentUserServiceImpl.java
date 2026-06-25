package com.campusassistant.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.remote.spider.mapper.SyncMapper;
import com.campusassistant.remote.spider.pojo.PersonalInfoEntity;
import com.campusassistant.remote.spider.pojo.PersonalInfoVO;
import com.campusassistant.remote.spider.service.SpiderService;
import com.campusassistant.utils.ThreadLocalUtil;
import com.campusassistant.common.UserContext;
import com.campusassistant.utils.redistool.CommonCacheService;
import com.campusassistant.utils.converter.PersonalInfoVoConvertor;
import com.campusassistant.utils.converter.UserStatusVoConvertor;
import com.campusassistant.student.mapper.UserMapper;
import com.campusassistant.student.pojo.UserEntity;
import com.campusassistant.student.pojo.UserStatusVO;
import com.campusassistant.student.service.impl.support.UserReadSupport;
import com.campusassistant.student.service.CurrentUserService;
import com.campusassistant.utils.rediskey.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.campusassistant.enums.ResultCodeEnum.UNAUTHORIZED;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentUserServiceImpl implements CurrentUserService {
    private final UserMapper userMapper;
    private final UserReadSupport userReadSupport;
    private final UserStatusVoConvertor userStatusVoConvertor;
    private final CommonCacheService commonCacheService;
    private final UserStatusCacheKey userStatusCacheKey;
    private final UserPwdCacheKey userPwdCacheKey;
    private final StringRedisTemplate stringRedisTemplate;
    private final SpiderService spiderService;
    private final UserPersonalCacheKey userPersonalCacheKey;
    private final SyncMapper syncMapper;
    private final PersonalInfoVoConvertor personalInfoVoConvertor;

    @Override
    public void self_unsubscribe() {
        UserContext userContext = ThreadLocalUtil.get();
        Long currentUserId = userContext.getUserId();
        if (currentUserId == null) {
            throw new BusinessException(UNAUTHORIZED);
        }
        UserEntity currentUser = userReadSupport.findEntityById(currentUserId);
        log.info("用户正在执行注销操作，用户id：[{}]，用户学号：[{}]",currentUser.getId(),currentUser.getStudentId());
        int rows = userMapper.deleteById(currentUserId);
        if (rows == 0) {
            throw new BusinessException(ResultCodeEnum.NOT_FOUND.getCode(),"操作失败");
        }
        
        String studentId = currentUser.getStudentId();
        if (studentId != null && !studentId.isEmpty()) {
            commonCacheService.deleteCache(userStatusCacheKey.getKey(studentId));
            commonCacheService.deleteCache(userPwdCacheKey.getKey(studentId));
            commonCacheService.deleteCache(userPersonalCacheKey.getKey(studentId));
        } else {
            log.warn("用户注销时发现用户名为空，跳过缓存删除, userId: {}", currentUserId);
        }
        log.info("用户已注销；[{}]",currentUser);
    }

    @Override
    public UserStatusVO getStatusByStudentId(String studentId) {
        if (studentId == null) {
            return null;
        }
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
    public PersonalInfoVO getPersonalByStudentId(String studentId) {
        if (studentId == null) {
            return null;
        }
        return commonCacheService.getWithCache(
                userPersonalCacheKey.getKey(studentId),
                PersonalInfoVO.class,
                () -> {
                    LambdaQueryWrapper<PersonalInfoEntity> lqw = new LambdaQueryWrapper<>();
                    lqw.eq(PersonalInfoEntity::getStudentId, studentId);
                    PersonalInfoEntity personalInfoEntity = syncMapper.selectOne(lqw);
                    if (personalInfoEntity == null) {
                        return null;
                    }
                    return personalInfoVoConvertor.toTarget(personalInfoEntity);
                }
        );
    }

    @Override
    public void refreshData() {
        UserContext userContext = ThreadLocalUtil.get();
        String studentId = userContext.getStudentId();
        // 从 Redis 中取出之前缓存的明文密码
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

}
