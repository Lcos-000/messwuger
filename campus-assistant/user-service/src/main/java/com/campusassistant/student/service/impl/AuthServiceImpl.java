package com.campusassistant.student.service.impl;

import com.campusassistant.common.UserContext;
import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.personalization.service.impl.support.ProfileWriteSupport;
import com.campusassistant.properties.JwtProperties;
import com.campusassistant.remote.spider.service.SpiderService;
import com.campusassistant.utils.AesUtil;
import com.campusassistant.utils.redistool.CommonCacheService;
import com.campusassistant.utils.JwtUtil;
import com.campusassistant.student.pojo.dto.LoginDTO;
import com.campusassistant.student.pojo.dto.UserDTO;
import com.campusassistant.student.pojo.UserEntity;
import com.campusassistant.student.service.AuthService;
import com.campusassistant.student.service.impl.support.UserReadSupport;
import com.campusassistant.student.service.impl.support.UserWriteSupport;
import com.campusassistant.utils.ThreadLocalUtil;
import com.campusassistant.utils.rediskey.*;
import com.campusassistant.utils.redistool.rediskey.TokenCacheKey;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.campusassistant.constant.SystemConstants.*;
import static com.campusassistant.enums.ResultCodeEnum.*;
import static com.campusassistant.remote.spider.common.SyncStatusEnum.SYNCING_SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final StringRedisTemplate stringRedisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final TokenCacheKey tokenCacheKey;
    private final UserWriteSupport userWriteSupport;
    private final UserReadSupport userReadSupport;
    private final SpiderService spiderService;
    private final UserPwdCacheKey userPwdCacheKey;
    private final CommonCacheService commonCacheService;
    private final UserStatusCacheKey userStatusCacheKey;
    private final UserPersonalCacheKey userPersonalCacheKey;
    private final AesUtil aesUtil;
    private final ProfileWriteSupport profileWriteSupport;

    @Override
    public void register(UserDTO userDTO) {
        String studentId = userDTO.getStudentId();
        String plainPassword = userDTO.getPassword();

        UserEntity existingUser = userReadSupport.findEntityByStudentId(studentId);
        if (existingUser != null) {
            throw new BusinessException(USER_ALREADY_EXISTS);
        }

        String encryptedPassword;
        try {
            encryptedPassword = aesUtil.encrypt(plainPassword);
        } catch (Exception e) {
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR);
        }

        boolean isValid = spiderService.validateCredentials(studentId, encryptedPassword);
        if (!isValid) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }
        userWriteSupport.addUser(userDTO);
        // 触发异步爬虫任务
        spiderService.asyncStartFullCrawl(studentId, encryptedPassword);
        // 初始化用户个性化配置
        profileWriteSupport.initByStudentId(studentId);

    }

    @Override
    public String login(LoginDTO loginDTO){
        String studentId = loginDTO.getStudentId();
        String plainPassword = loginDTO.getPassword();
        UserEntity userEntity = userReadSupport.findEntityByStudentId(studentId);
        if (userEntity ==null){
            throw new BusinessException(NOT_FOUND.getCode(), "用户不存在");
        }
        if (!passwordEncoder.matches(plainPassword, userEntity.getPassword())){ //验证密码
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED.getCode(),"密码错误");
        }
        // 第二步：验证通过后，立即加密密码
        String encryptedPassword;
        try {
            encryptedPassword = aesUtil.encrypt(plainPassword);
        } catch (Exception e) {
            log.error("密码加密失败", e);
            throw new BusinessException(ResultCodeEnum.SYSTEM_ERROR);
        }
        log.info("学号 {} 密码加密完成", studentId);

        UserContext userContext = new UserContext();
        userContext.setUserId(userEntity.getId());
        userContext.setStudentId(userEntity.getStudentId());
        userContext.setRole("暂时未开发");
        ThreadLocalUtil.set(userContext);

        Map<String,Object> claims = new HashMap<>();//创建一个 Map 集合，存入登录成功的用户关键信息
        claims.put(USER_ID, userEntity.getId());
        claims.put(STUDENT_ID, userEntity.getStudentId());
        claims.put(USER_ROLE, "暂时未开发");
        String token = jwtUtil.genToken(claims);//调用工具类生成加密字符串（Token）

        stringRedisTemplate.opsForValue().
                set(tokenCacheKey.getKey(token),
                        "",
                        jwtProperties.getRedisHours(),
                        TimeUnit.HOURS);//实际存入,key为token,value为token（占位即可，可以换），设置有效期1小时

        stringRedisTemplate.opsForValue().
                set(userPwdCacheKey.getKey(studentId),
                        encryptedPassword,
                        jwtProperties.getPasswordDays(),
                        TimeUnit.DAYS);

        // login时只有从未同步过才触发，不判断SUCCESS
        if (!Objects.equals(userEntity.getSyncStatus(), SYNCING_SUCCESS.getCode())) {
            spiderService.asyncStartFullCrawl(studentId, encryptedPassword);
        }
        return token;
    }

    @Override
    public void logout(HttpServletRequest request) {
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        UserContext userContext = ThreadLocalUtil.get();
        String studentId = userContext.getStudentId();
        if (token != null) {
            // 从 Redis 删除 Token
            stringRedisTemplate.delete(tokenCacheKey.getKey(token));
            commonCacheService.deleteCache(userPwdCacheKey.getKey(userContext.getStudentId()));
            commonCacheService.deleteCache(userStatusCacheKey.getKey(studentId));
            commonCacheService.deleteCache(userPersonalCacheKey.getKey(userContext.getStudentId()));
        }
    }

}
