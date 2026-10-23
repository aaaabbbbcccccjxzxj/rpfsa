package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: Erdong J
 * @Date: 2023/5/9 15:23
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    /** 未读。沿用历史数据约定：is_read = 1 表示未读，0 表示已读 */
    public static final int UNREAD = 1;
    /** 已读 */
    public static final int READ = 0;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    private String content;
    private String userReply;
    private Integer isRead;
    private Date createTime;
}
