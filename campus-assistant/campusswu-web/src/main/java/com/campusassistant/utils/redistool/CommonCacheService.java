package com.campusassistant.utils.redistool;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


@Component
@Slf4j
@RequiredArgsConstructor
public class CommonCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // 默认正常数据过期时长及单位（10分钟）
    private static final long DEFAULT_TIMEOUT = 10;

    // 默认空值(防穿透)过期时长及单位（5秒）
    private static final long DEFAULT_NULL_TIMEOUT = 5;
    // 默认空值(防穿透)值
    private static final String DEFAULT_NULL_VALUE = "null";


    // =======================  普通对象 (Class 类型) =======================

    /**
     * 基础缓存方法（使用默认时长 10 分钟）
     */
    public <T> T getWithCache(String redisKey,
                              Class<T> tClass,
                              Supplier<T> dbLoader) {
        return getWithCache(redisKey, tClass, dbLoader, DEFAULT_TIMEOUT, DEFAULT_NULL_TIMEOUT);
    }

    /**
     * 基础缓存方法（自定义时长）
     */
    public <T> T getWithCache(String redisKey,
                              Class<T> tClass,
                              Supplier<T> dbLoader,
                              long timeout,
                              long nullTimeout) {
        return getWithCacheCore(redisKey, dbLoader, json -> objectMapper.readValue(json, tClass),
                timeout, nullTimeout);
    }

    // =======================  泛型集合 (TypeReference 类型) =======================

    /**
     * 集合缓存方法（使用默认时长 10 分钟）
     */
    public <T> T getWithCache(String redisKey,
                              TypeReference<T> typeReference,
                              Supplier<T> dbLoader) {
        return getWithCache(redisKey, typeReference, dbLoader, DEFAULT_TIMEOUT, DEFAULT_NULL_TIMEOUT);
    }

    /**
     * 集合缓存方法（自定义时长）
     */
    public <T> T getWithCache(String redisKey,
                              TypeReference<T> typeReference,
                              Supplier<T> dbLoader,
                              long timeout,
                              long nullTimeout) {
        return getWithCacheCore(redisKey, dbLoader, json -> objectMapper.readValue(json, typeReference),
                timeout, nullTimeout);
    }

    // ======================= 核心底层逻辑 =======================

    private <T> T getWithCacheCore(String redisKey,
                                   Supplier<T> dbLoader,
                                   JsonDeserializer<T> deserializer,
                                   long timeout,
                                   long nullTimeout) {
        // 1. 查缓存
        String jsonValue = stringRedisTemplate.opsForValue().get(redisKey);

        // 2. 命中缓存
        if (jsonValue != null) {
            if (DEFAULT_NULL_VALUE.equals(jsonValue)) {
                return null;
            }
            try {
                return deserializer.deserialize(jsonValue);
            } catch (Exception e) {
                log.warn("反序列化失败，重新查询数据库...", e);
                stringRedisTemplate.delete(redisKey);
            }
        }

        // 3. 查数据库
        T result = dbLoader.get();

        // 4. 回填缓存
        if (result == null) {
            stringRedisTemplate.opsForValue().set(redisKey, DEFAULT_NULL_VALUE, nullTimeout, TimeUnit.SECONDS);
        } else {
            try {
                String json = objectMapper.writeValueAsString(result);
                stringRedisTemplate.opsForValue().set(redisKey, json, timeout, TimeUnit.MINUTES);
            } catch (Exception e) {
                throw new RuntimeException("缓存序列化失败", e);
            }
        }
        return result;
    }

    // 函数式接口：用于抹平 Class 和 TypeReference 反序列化的差异
    private interface JsonDeserializer<T> {
        T deserialize(String json) throws Exception;
    }

    // ======================= 缓存删除机制 =======================

    public void deleteCache(String key) {
        stringRedisTemplate.delete(key);
    }

    public void deleteByPattern(String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }
}