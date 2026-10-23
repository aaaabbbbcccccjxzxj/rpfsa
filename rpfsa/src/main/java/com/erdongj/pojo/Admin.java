package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 管理员
 * @author: Erdong J
 * @create: 2023-03-08 19:35
 **/

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Admin {

    /** 沿用既有表结构，以 adminname 作为主键 */
    @TableId(value = "adminname", type = IdType.INPUT)
    private String adminname;

    private String password;
}

