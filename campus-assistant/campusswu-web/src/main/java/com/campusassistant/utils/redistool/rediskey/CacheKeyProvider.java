package com.campusassistant.utils.redistool.rediskey;

public interface CacheKeyProvider<T> {
    /**
     * 根据标识生成完整Key
     */
    String getKey(T identifier);

    /**
     * 获取Key的前缀
     */
    String getPrefix();
}
