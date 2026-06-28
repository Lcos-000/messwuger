package com.campusassistant.utils;

import com.campusassistant.enums.ResultCodeEnum;
import com.campusassistant.exception.BusinessException;
import com.campusassistant.pojo.UserContext;

public final class UserContextUtil {

    private UserContextUtil() {}

    public static UserContext requireCurrentUser() {
        UserContext userContext = ThreadLocalUtil.get(UserContext.class);
        if (userContext == null) {
            throw new BusinessException(ResultCodeEnum.UNAUTHORIZED);
        }
        return userContext;
    }

    public static Long requireUserId() {
        return requireCurrentUser().getUserId();
    }

    public static String requireStudentId() {
        return requireCurrentUser().getStudentId();
    }

    public static String requireRole() {
        return requireCurrentUser().getRole();
    }

    public static Long getCurrentUserIdOrNull() {
        UserContext userContext = ThreadLocalUtil.get(UserContext.class);
        return userContext == null ? null : userContext.getUserId();
    }

    public static String getCurrentStudentIdOrNull() {
        UserContext userContext = ThreadLocalUtil.get(UserContext.class);
        return userContext == null ? null : userContext.getStudentId();
    }

    public static String getCurrentRole() {
        UserContext userContext = ThreadLocalUtil.get(UserContext.class);
        return userContext == null ? null : userContext.getRole();
    }



}
