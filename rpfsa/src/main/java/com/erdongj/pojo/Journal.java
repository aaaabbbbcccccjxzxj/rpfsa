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
 * @Date: 2023/6/3 15:46
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Journal {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    @TableField(exist = false)
    private User user;
    private Integer pid;
    @TableField(exist = false)
    private Pet pet;
    private String title;
    private String diary;
    private Date createTime;
    private Integer isDel;
}
