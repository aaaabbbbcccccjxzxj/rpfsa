package com.erdongj.controller;

import com.erdongj.common.RequireAdmin;
import com.erdongj.common.RequireLogin;
import com.erdongj.dto.RechargeDTO;
import com.erdongj.pojo.Recharge;
import com.erdongj.pojo.ResultBean;
import com.erdongj.service.WalletService;
import com.erdongj.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宠宠币钱包控制层：签到、充值、记录。全部接口需要登录。
 */
@RestController
@RequestMapping("/wallet")
@RequireLogin
public class WalletController extends BaseController {

    @Autowired
    private WalletService walletService;

    /** 钱包概览：余额 + 今日签到状态 */
    @GetMapping("/overview")
    public ResultBean<Map<String, Object>> overview(@RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer uid) {
        return successful("查询成功", walletService.overview(uid));
    }

    /** 每日签到 */
    @PostMapping("/sign")
    public ResultBean<Map<String, Object>> sign(@RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer uid) {
        int coin = walletService.sign(uid);
        Map<String, Object> payload = new HashMap<>(2);
        payload.put("coin", coin);
        return successful("签到成功，宠宠币 +" + coin, payload);
    }

    /** 模拟充值（档位白名单校验在服务端） */
    @PostMapping("/recharge")
    public ResultBean<Map<String, Object>> recharge(@RequestBody @Valid RechargeDTO dto,
                                                    @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer uid) {
        int coin = walletService.recharge(uid, dto.getAmount());
        Map<String, Object> payload = new HashMap<>(2);
        payload.put("coin", coin);
        return successful("充值成功，宠宠币 +" + coin, payload);
    }

    /** 我的充值记录 */
    @GetMapping("/recharges")
    public ResultBean<List<Recharge>> myRecharges(@RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer uid) {
        List<Recharge> records = walletService.myRecharges(uid);
        if (records.isEmpty()) {
            return faile("暂无充值记录");
        }
        return successful("查询成功", records);
    }

    /** 充值记录分页（管理端） */
    @GetMapping("/admin/{c}")
    @RequireAdmin
    public ResultBean<com.erdongj.pojo.Pagination<Recharge>> adminRecharges(@PathVariable("c") int current) {
        com.erdongj.pojo.Pagination<Recharge> pagination = walletService.adminRecharges(current);
        if (pagination.getTotal() == 0) {
            return faile("暂无充值记录");
        }
        return successful("查询成功", pagination);
    }
}
