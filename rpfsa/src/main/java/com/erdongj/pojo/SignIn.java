package com.erdongj.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

/**
 * 每日签到记录。(uid, sign_date) 唯一约束保证同一用户一天只能签到一次。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignIn {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    private LocalDate signDate;
    private Integer coin;
    private Date createTime;

    public SignIn(Integer uid, LocalDate signDate, Integer coin) {
        this.uid = uid;
        this.signDate = signDate;
        this.coin = coin;
    }
}
