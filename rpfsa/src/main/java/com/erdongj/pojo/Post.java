package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @Author: Erdong J
 * @Date: 2023/4/28 15:17
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    /** 帖子类型：社区帖子 */
    public static final int TYPE_POST = 0;
    /** 帖子类型：公告 */
    public static final int TYPE_NOTICE = 1;

    /** 管理员发布的内容以 uid = 0 作为标识（沿用既有数据约定） */
    public static final int ADMIN_UID = 0;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String content;
    private Integer uid;
    @TableField(exist = false) //说明该帖子是谁发的
    private User user;
    private Integer classic; //管理员：0:网站公告,1:领养指南,2:近期新闻,3:社区置顶公告，用户：4:寻找猫狗，5：咨询领养，6：分享生活，7：其他
    private Integer type;  //0表示帖子，1表示公告
    private Integer commentCount;
    private Date createTime;
    private Integer isPinned;  //是否置顶 管理员可以修改
    private Integer likeCount; //点赞数
    @TableField(exist = false)
    private Boolean liked; //当前登录用户是否已点赞（详情接口装配，游客为 null）
    @TableField(exist = false)
    private List<Comment> comments; //用来存放该条帖子的评论
    private Integer isDel;

}
