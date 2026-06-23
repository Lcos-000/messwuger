package com.campusassistant.utils.rediskey;

import org.springframework.lang.Nullable;

public abstract class AbstractCacheKeyProvider<T> implements CacheKeyProvider<T> {

    /**
     * 模板方法：定义了Key生成的标准流程
     * 子类不能重写此方法，必须通过 getPrefix() 提供前缀
     */
    @Override
    public final String getKey(@Nullable T identifier) {
        // 1. 空值校验
        if (identifier == null) {
            return null;
        }
        // 2. 拼接逻辑由父类统一控制
        return getPrefix() + identifier;
    }

    /**
     * 抽象方法：强制子类提供具体的前缀
     * 子类只需关心前缀是什么，无需关心怎么拼接
     */
    @Override
    public abstract String getPrefix();
}
