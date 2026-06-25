package com.campusassistant.common.converter;

import java.util.List;

public interface BaseConvertor<S, T> {

    // 单个对象转换 (正向与逆向)
    T toTarget(S source);
    S toSource(T target);

    // 集合转换 (正向与逆向)
    List<T> toTarget(List<S> sources);
    List<S> toSource(List<T> targets);
}
