package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @description: 用户表
 * @author: Erdong J
 * @create: 2023-03-09 10:41
 **/
@Data
@NoArgsConstructor
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    /** 密码：@JsonIgnore 保证任何响应体都不会把它带出去 */
    @JsonIgnore
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private Integer comPermiss;
    private Date createTime;
    private String photo;
    private Integer petCoin;
    private Integer isDel;
    private Integer howAdopt;

    /** 管理端用户列表展示用：发给管理员的未读消息数（非数据库字段，由服务端一次查询填充） */
    @TableField(exist = false)
    private Integer noReadCount;

    public User(String username, String password, String phone) {
        this.username = username;
        this.password = password;
        this.phone = phone;
    }
}

