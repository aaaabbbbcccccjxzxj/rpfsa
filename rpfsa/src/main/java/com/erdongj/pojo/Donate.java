package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: Erdong J
 * @Date: 2023/5/6 20:48
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Donate {

    /** 分配状态：未完成分配 */
    public static final int STATE_PENDING = 0;
    /** 分配状态：已完成分配 */
    public static final int STATE_DONE = 1;

    /** 通知未读（沿用历史数据约定：1 = 未读） */
    public static final int UNREAD = 1;
    /** 通知已读 */
    public static final int READ = 0;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    @TableField(exist = false)
    private User user;
    private String username;
    private String nickname;
    private Integer pid;
    @TableField(exist = false)
    private Pet pet;
    private String petname;
    private String donateList;
    private Integer totalPrice;
    private Integer state;
    private Integer isRead;
    private Date createTime;
    private Integer isDel;
}
