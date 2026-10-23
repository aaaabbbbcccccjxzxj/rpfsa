package com.erdongj.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 管理员登录请求。
 */
@Data
public class AdminLoginDTO {

    @NotBlank(message = "请输入管理员账号")
    private String adminname;

    @NotBlank(message = "请输入管理员密码")
    private String password;
}
