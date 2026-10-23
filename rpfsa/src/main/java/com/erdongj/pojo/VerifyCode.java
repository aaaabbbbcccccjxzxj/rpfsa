package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 找回密码验证码。
 * 演示环境没有邮件通道，验证码由接口直接回显给前端展示；
 * 生产接入邮件服务后只需把"回显"改为"发送邮件"。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCode {

    /** 验证码有效期：10 分钟 */
    public static final long TTL_MILLIS = 10 * 60 * 1000L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String email;
    private String code;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;
    /** 0 未使用，1 已使用 */
    private Integer used;
    private Date createTime;

    public VerifyCode(String email, String code) {
        this.email = email;
        this.code = code;
        this.expireTime = new Date(System.currentTimeMillis() + TTL_MILLIS);
        this.used = 0;
    }
}
