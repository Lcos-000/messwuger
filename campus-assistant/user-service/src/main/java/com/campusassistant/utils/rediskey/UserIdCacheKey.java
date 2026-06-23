package com.campusassistant.utils.rediskey;

import com.campusassistant.utils.redistool.rediskey.AbstractCacheKeyProvider;
import org.springframework.stereotype.Component;

/**
 * 用户ID维度的缓存Key生成器
 * 继承抽象类时，将泛型 T 指定为 Long
 */
@Component
public class UserIdCacheKey extends AbstractCacheKeyProvider<Long> {

    // 定义 ID 专用的前缀，注意和 Name 的前缀区分开
    private static final String USER_ID_PREFIX = "user:info:id:";

    @Override
    public String getPrefix() {
        return USER_ID_PREFIX;
    }
}
