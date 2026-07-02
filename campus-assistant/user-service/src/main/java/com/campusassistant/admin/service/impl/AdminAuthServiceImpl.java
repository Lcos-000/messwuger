package com.campusassistant.admin.service.impl;

import com.campusassistant.admin.service.AdminAuthService;
import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.properties.JwtProperties;
import com.campusassistant.student.pojo.UserEntity;
import com.campusassistant.student.pojo.dto.LoginDTO;
import com.campusassistant.student.service.impl.support.UserCacheSupport;
import com.campusassistant.student.service.impl.support.UserReadSupport;
import com.campusassistant.utils.JwtUtil;
import com.campusassistant.utils.UserContextUtil;
import com.campusassistant.utils.rediskey.UserPwdCacheKey;
import com.campusassistant.key.TokenCacheKey;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.campusassistant.constant.SystemConstants.*;
import static com.campusassistant.constant.SystemConstants.ROLE_ADMIN;
import static com.campusassistant.constant.SystemConstants.USER_ROLE;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final UserReadSupport userReadSupport;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final TokenCacheKey tokenCacheKey;
    private final UserPwdCacheKey userPwdCacheKey;
    private final UserCacheSupport userCacheSupport;

    @Override
    public String login(LoginDTO loginDTO) {
        String studentId = loginDTO.getStudentId();
        String plainPassword = loginDTO.getPassword();

        UserEntity userEntity = userReadSupport.findEntityByStudentId(studentId);
        if (userEntity == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED.getCode(), "账号或密码错误");
        }

        if (!passwordEncoder.matches(plainPassword, userEntity.getPassword())) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED.getCode(), "账号或密码错误");
        }

        log.info("用户{}：{} 正在尝试管理员登录", userEntity.getId(), studentId);
        if (!ROLE_ADMIN.equals(userEntity.getRole())) {
            log.warn("用户{}：{} 尝试管理员登录失败，因为不是管理员", userEntity.getId(), studentId);
            throw new BusinessException(ResultCodeEnum.FORBIDDEN.getCode(), "账号或密码错误");
        }
        log.info("用户{}：{} 管理员登录成功", userEntity.getId(), studentId);

        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID, userEntity.getId());
        claims.put(STUDENT_ID, userEntity.getStudentId());
        claims.put(USER_ROLE, ROLE_ADMIN);

        String token = jwtUtil.genToken(claims);

        stringRedisTemplate.opsForValue().set(
                tokenCacheKey.getKey(token),
                "",
                jwtProperties.getAdminRedisHours(),
                TimeUnit.HOURS
        );

        return token;
    }

    @Override
    public void logout(HttpServletRequest request) {
        String token = request.getHeader(jwtProperties.getJwtTokenName());
        String currentStudentId = UserContextUtil.requireStudentId();
        if (token != null && !token.isBlank()) {
            userCacheSupport.evictLoginSessionAndUserCaches(currentStudentId, token);
        }
    }
}
