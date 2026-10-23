package com.erdongj.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户修改"自己的"资料。
 * <p>
 * 改造前：前端把整个 User 对象（含 password、comPermiss、petCoin、isDel）回传，
 * 后端 {@code updateById} 无条件接受 —— 用户可自行提权、改积分。
 * 改造后：只允许修改下列展示性字段，权限与积分一律不可经此接口变更。
 */
@Data
public class UserProfileDTO {

    @Size(max = 30, message = "用户名过长")
    @Pattern(regexp = "^[^\\u4e00-\\u9fa5]*$", message = "用户名不能包含汉字")
    private String username;

    @Size(max = 30, message = "昵称过长")
    private String nickname;

    @Size(max = 30, message = "邮箱过长")
    private String email;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;

    @Size(max = 50, message = "头像文件名过长")
    private String photo;
}
