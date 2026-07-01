package com.campusassistant.utils.rediskey;

import com.campusassistant.key.AbstractCacheKeyProvider;
import org.springframework.stereotype.Component;

/**
 * 用户ID维度的缓存Key生成器
 * 继承抽象类时，将泛型 T 指定为 Long
 */
@Component
public class UserPersonalCacheKey extends AbstractCacheKeyProvider<String> {

    // 定义 ID 专用的前缀，注意和 Name 的前缀区分开
    private static final String USER_PERSONAL_PREFIX = "user:info:personal:studentId:";

    @Override
    public String getPrefix() {
        return USER_PERSONAL_PREFIX;
    }
}
