package com.erdongj.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户注册请求。
 * <p>
 * 改造前：注册与"管理员新增用户"共用 {@code POST /user}，且直接绑定 User 实体，
 * 提交方可以夹带 {@code comPermiss}、{@code petCoin} 等字段。
 * 改造后：注册独立成 {@code POST /user/register}，字段白名单化，
 * 权限与积分由服务端给定初始值。
 */
@Data
public class UserRegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 30, message = "用户名过长")
    @Pattern(regexp = "^[^\\u4e00-\\u9fa5]+$", message = "用户名不能包含汉字")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 4, max = 20, message = "密码长度需为 4-20 位")
    private String password;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;
}
