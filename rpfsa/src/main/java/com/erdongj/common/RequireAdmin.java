package com.erdongj.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记"仅管理员可访问"的接口。
 * <p>
 * 由 {@code AuthInterceptor} 读取并校验 token 中的角色，替代原先"所有接口裸奔"的状态。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdmin {
}
