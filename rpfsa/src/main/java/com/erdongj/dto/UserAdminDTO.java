package com.erdongj.dto;

import lombok.Data;

/**
 * 管理员维护用户信息（新增 / 修改）。
 * <p>
 * 只有走 {@code /admin/users} 的请求才允许改动权限位与积分；
 * password 为空表示不修改密码，服务端会跳过该字段。
 * <p>
 * username 未加 @NotNull：修改场景下常只改单个字段（如切换评论权限），
 * 由 Service 在新增分支单独校验必填。
 */
@Data
public class UserAdminDTO {

    /** 新增时为空 */
    private Integer id;

    /** 新增时必填，修改时可省略（省略则不改动） */
    private String username;

    /** 为空表示不修改密码 */
    private String password;

    private String nickname;

    private String email;

    private String phone;

    private String photo;

    private Integer petCoin;

    /** 1=可评论，0=已禁言 */
    private Integer comPermiss;
}
