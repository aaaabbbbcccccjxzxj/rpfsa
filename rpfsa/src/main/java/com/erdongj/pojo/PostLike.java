package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 帖子点赞记录。(uid, poid) 唯一约束防止重复点赞。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    private Integer poid;
    private Date createTime;

    public PostLike(Integer uid, Integer poid) {
        this.uid = uid;
        this.poid = poid;
    }
}
