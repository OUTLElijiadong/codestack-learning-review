package com.wy.review.annotation;

import java.lang.annotation.*;

/**
 * 角色权限注解：标注在 Controller 方法或类上，由 RoleInterceptor 统一裁决
 * 例：方法级 @RequireRole("student") 仅学生；类级 @RequireRole({"teacher","admin"})
 * 不标注则只需登录即可访问（如 /common/upload）
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {

    /** 允许访问的角色集合 */
    String[] value();
}
