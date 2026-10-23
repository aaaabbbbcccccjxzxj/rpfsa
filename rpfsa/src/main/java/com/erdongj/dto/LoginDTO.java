package com.erdongj.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录请求。
 * <p>
 * 改造前登录直接绑 User 实体，导致实体上的 password 字段必须在序列化时保留；
 * 现在登录单独用 DTO 接收，实体就可以安全地把 password 标记为 {@code @JsonIgnore}，
 * 彻底杜绝"密码随用户对象下发到前端"。
 */
@Data
public class LoginDTO {

    @NotBlank(message = "请输入用户名")
    private String username;

    @NotBlank(message = "请输入密码")
    private String password;
}
