package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: Erdong J
 * @Date: 2023/4/20 13:41
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Adopt {

    /** 审核状态：未通过 */
    public static final int STATE_REJECTED = 0;
    /** 审核状态：已通过 */
    public static final int STATE_PASSED = 1;
    /** 审核状态：审核中 */
    public static final int STATE_PENDING = 2;

    /** 通知未读（沿用历史数据约定：1 = 未读） */
    public static final int UNREAD = 1;
    /** 通知已读 */
    public static final int READ = 0;

    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("userid")
    private Integer uid;
    @TableField("petid")
    private Integer pid;
    @TableField(exist = false) //表明数据库并不存在该字段
    private User user;
    private String username;
    private String nickname;
    @TableField(exist = false)
    private Pet pet;
    private String petname;
    private Integer state;
    private Date applyTime;
    private Integer isDel;
    private String adress;
    private String reason;
    private Integer isRead; //1表示有消息未读 0表示没有消息。
    private String ponReason;

    public Adopt(Integer uid, Integer pid, String reason) {
        this.uid = uid;
        this.pid = pid;
        this.reason = reason;
    }
}
