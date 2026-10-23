package com.erdongj.service;

import com.erdongj.pojo.Recharge;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 宠宠币钱包：签到、充值、记录查询。
 */
public interface WalletService {

    /** 每日签到奖励（宠宠币） */
    int SIGN_COIN = 10;

    /**
     * 每日签到。同一天重复签到会抛出业务异常。
     *
     * @return 本次获得的宠宠币
     */
    int sign(Integer uid);

    /** 今天是否已签到 */
    boolean signedToday(Integer uid);

    /**
     * 模拟充值：金额按白名单档位校验，1 元 = 10 宠宠币，提交即到账。
     *
     * @return 到账的宠宠币数量
     */
    int recharge(Integer uid, BigDecimal amount);

    /** 我的充值记录（时间倒序，最多 50 条） */
    List<Recharge> myRecharges(Integer uid);

    /** 钱包概览：当前余额 + 今日签到状态 */
    Map<String, Object> overview(Integer uid);

    /** 充值记录分页（管理端，装配充值人信息） */
    com.erdongj.pojo.Pagination<Recharge> adminRecharges(int current);
}
