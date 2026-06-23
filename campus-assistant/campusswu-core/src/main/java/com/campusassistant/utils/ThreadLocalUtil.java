package com.campusassistant.utils;

public final class ThreadLocalUtil {

    private static final ThreadLocal<Object> THREAD_LOCAL = new ThreadLocal<>();

    // 私有构造方法，禁止外部创建实例
    private ThreadLocalUtil(){}

    public static void set(Object value) {
        THREAD_LOCAL.set(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get() {
        return (T) THREAD_LOCAL.get();
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        Object value = THREAD_LOCAL.get();
        if (clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

}