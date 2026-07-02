package com.campusassistant.utils;

import com.campusassistant.pojo.UserContext;
import com.campusassistant.provider.UserIdProvider;
import com.campusassistant.utils.ThreadLocalUtil;
import org.springframework.stereotype.Component;

@Component
public class WebUserIdProvider implements UserIdProvider {

    @Override
    public Long getCurrentUserId() {
        // 利用现成的拦截器中放入 ThreadLocal 的 UserContext
        UserContext userContext = ThreadLocalUtil.get(UserContext.class);
        if (userContext != null) {
            return userContext.getUserId();
        }
        return null; // 或者返回一个表示系统的默认 ID
    }
}
