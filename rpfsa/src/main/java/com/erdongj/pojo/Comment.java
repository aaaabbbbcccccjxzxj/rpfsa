package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @Author: Erdong J
 * @Date: 2023/4/28 15:22
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    /** 一级评论的 level 取值；二级评论的 level 存的是其父评论 id */
    public static final int TOP_LEVEL = 0;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    @TableField(exist = false)
    private User user; //是哪个用户评论的
    private Integer poid;
    private Integer pid; //与poid必须有一个为零.
    private Integer level; //0(默认):一级评论  ; 某一级评论id:二级评论
    @TableField(exist = false)
    private List<Comment> commentList; //当level为0时,才有子评论。
    /**
     * 归属帖子仅在服务端组装数据时使用。
     * 序列化必须忽略：Post.comments → Comment.post → Post.comments 会构成循环引用，
     * Jackson 会递归产出数百 KB 的深层嵌套且最终输出非法 JSON（热门榜接口曾因此不可用）。
     */
    @JsonIgnore
    @TableField(exist = false)
    private Post post; //归属哪个post
    /** 归属对象标题（帖子标题或宠物名，管理端列表展示用，非数据库字段） */
    @TableField(exist = false)
    private String targetTitle;
    private Date createTime;
    private String content;
    private Integer isDel;

    public Comment(Integer uid, Integer poid, String content) {
        this.uid = uid;
        this.poid = poid;
        this.content = content;
    }
}
