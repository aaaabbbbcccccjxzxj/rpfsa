package com.erdongj.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记"必须登录"的接口。
 * <p>
 * 拦截器的默认策略是：GET 请求放行（游客可浏览宠物 / 帖子 / 公告），
 * 其余写操作一律要求登录。对少数需要登录才能看的 GET 接口（如"我的领养"、
 * "我的消息"），用本注解显式声明。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLogin {
}
