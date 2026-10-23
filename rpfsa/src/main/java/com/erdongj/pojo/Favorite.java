package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 收藏表：用户收藏宠物。
 * (uid, pid) 在数据库层有唯一约束，重复收藏由服务端转为"取消收藏"。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    private Integer pid;
    private Date createTime;

    /** 收藏对应的宠物信息（非数据库字段，查询时装配） */
    @TableField(exist = false)
    private Pet pet;

    public Favorite(Integer uid, Integer pid) {
        this.uid = uid;
        this.pid = pid;
    }
}
