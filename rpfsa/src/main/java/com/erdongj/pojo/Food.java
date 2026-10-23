package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Erdong J
 * @Date: 2023/5/6 08:45
 * @Description:
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Food {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String foodname;
    private Integer inventory;
    private Integer price;
    private String picture;
    private Integer isDel;
    @TableField(exist = false)
    private Integer tempCount = 0; //用户临时计数用

}
