package com.campusassistant.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) //同时支持类和方法
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminCheck {
}
