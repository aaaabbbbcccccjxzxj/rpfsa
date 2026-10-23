package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 宠宠币充值记录（模拟支付：提交即到账）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recharge {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    /** 充值金额（人民币元） */
    private BigDecimal amount;
    /** 到账宠宠币 */
    private Integer coin;
    private Date createTime;

    /** 充值人信息（非数据库字段，管理端列表装配） */
    @TableField(exist = false)
    private User user;

    public Recharge(Integer uid, BigDecimal amount, Integer coin) {
        this.uid = uid;
        this.amount = amount;
        this.coin = coin;
    }
}
