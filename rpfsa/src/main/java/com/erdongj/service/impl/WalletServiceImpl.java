package com.erdongj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.erdongj.common.BizException;
import com.erdongj.mapper.RechargeMapper;
import com.erdongj.mapper.SignInMapper;
import com.erdongj.mapper.UserMapper;
import com.erdongj.pojo.Recharge;
import com.erdongj.pojo.SignIn;
import com.erdongj.pojo.User;
import com.erdongj.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 宠宠币钱包业务层。
 * <p>
 * 余额变更一律使用数据库侧原子自增（{@code pet_coin = pet_coin + ?}），
 * 不做"读出来改完写回"，并发下不会丢更新。
 */
@Service
public class WalletServiceImpl implements WalletService {

    /** 充值档位白名单（元 → 宠宠币 = 金额 × 10） */
    private static final Set<String> ALLOWED_AMOUNTS = Set.of("6", "12", "30", "68", "128");
    private static final BigDecimal COIN_PER_YUAN = BigDecimal.TEN;

    @Autowired
    private SignInMapper signInMapper;

    @Autowired
    private RechargeMapper rechargeMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 每日签到。幂等由 sign_in 表的 (uid, sign_date) 唯一约束兜底：
     * 并发或重复点击只会有一个事务插入成功，其余捕获主键冲突转为业务提示。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sign(Integer uid) {
        LocalDate today = LocalDate.now();
        try {
            signInMapper.insert(new SignIn(uid, today, SIGN_COIN));
        } catch (DuplicateKeyException e) {
            throw new BizException("今天已经签到过了，明天再来吧");
        }
        addCoin(uid, SIGN_COIN);
        return SIGN_COIN;
    }

    @Override
    public boolean signedToday(Integer uid) {
        Long count = signInMapper.selectCount(new LambdaQueryWrapper<SignIn>()
                .eq(SignIn::getUid, uid)
                .eq(SignIn::getSignDate, LocalDate.now()));
        return count != null && count > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int recharge(Integer uid, BigDecimal amount) {
        // 白名单档位校验：金额只能是指定档位，防止任意小数/负数伪造
        String key = amount.stripTrailingZeros().toPlainString();
        if (!ALLOWED_AMOUNTS.contains(key)) {
            throw new BizException("充值金额不在可选档位内");
        }
        int coin = amount.multiply(COIN_PER_YUAN).intValue();
        rechargeMapper.insert(new Recharge(uid, amount, coin));
        addCoin(uid, coin);
        return coin;
    }

    @Override
    public List<Recharge> myRecharges(Integer uid) {
        return rechargeMapper.selectList(new LambdaQueryWrapper<Recharge>()
                .eq(Recharge::getUid, uid)
                .orderByDesc(Recharge::getCreateTime)
                .last("limit 50"));
    }

    /** 管理端充值记录分页：批量装配充值人，避免逐行查询 */
    @Override
    public com.erdongj.pojo.Pagination<Recharge> adminRecharges(int current) {
        com.baomidou.mybatisplus.core.metadata.IPage<Recharge> pageInfo = rechargeMapper.selectPage(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(Math.max(current, 1), 10),
                new LambdaQueryWrapper<Recharge>().orderByDesc(Recharge::getCreateTime));
        List<Recharge> records = pageInfo.getRecords();
        if (!records.isEmpty()) {
            java.util.Set<Integer> uids = records.stream().map(Recharge::getUid)
                    .collect(java.util.stream.Collectors.toSet());
            Map<Integer, User> userMap = userMapper.selectBatchIds(uids).stream()
                    .collect(java.util.stream.Collectors.toMap(User::getId, java.util.function.Function.identity()));
            records.forEach(r -> r.setUser(userMap.get(r.getUid())));
        }
        return com.erdongj.pojo.Pagination.of(pageInfo, records);
    }

    @Override
    public Map<String, Object> overview(Integer uid) {
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        Map<String, Object> result = new HashMap<>(4);
        result.put("petCoin", user.getPetCoin());
        result.put("signedToday", signedToday(uid));
        result.put("signCoin", SIGN_COIN);
        return result;
    }

    /** 数据库侧原子加币 */
    private void addCoin(Integer uid, int coin) {
        int rows = userMapper.update(null, new LambdaUpdateWrapper<User>()
                .setSql("pet_coin = pet_coin + " + coin)
                .eq(User::getId, uid));
        if (rows != 1) {
            throw new BizException("用户不存在");
        }
    }
}
