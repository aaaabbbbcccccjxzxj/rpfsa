package com.erdongj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erdongj.common.BizException;
import com.erdongj.dto.DonateCommitDTO;
import com.erdongj.mapper.DonateMapper;
import com.erdongj.mapper.FoodMapper;
import com.erdongj.mapper.PetMapper;
import com.erdongj.mapper.UserMapper;
import com.erdongj.pojo.Donate;
import com.erdongj.pojo.Food;
import com.erdongj.pojo.Message;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.Pet;
import com.erdongj.pojo.ResultBean;
import com.erdongj.pojo.User;
import com.erdongj.service.DonateService;
import com.erdongj.service.MessageService;
import com.erdongj.util.QueryFieldGuard;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: Erdong J
 * @Date: 2023/5/6 21:04
 * @Description:
 */
@Service
public class DonateServiceImpl implements DonateService {

    /** 管理端捐助记录每页条数 */
    private static final int PAGE_SIZE = 10;

    @Autowired
    private DonateMapper donateMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private FoodMapper foodMapper;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 完成一次捐助。
     * <p>
     * 这是本次改造的重点方法。改造前该流程由前端串行发起 4~8 个 HTTP 请求完成
     * （改宠物币、改用户币、循环改库存、写捐助记录），任一步失败都会留下
     * "扣了钱却没记录"或"库存扣了账没记"的脏数据，且金额、余额判断全在前端，可被篡改。
     * <p>
     * 现在收敛为一个事务方法，并遵循三条原则：
     * <ol>
     *   <li>金额由服务端按数据库中的单价重算，完全忽略前端传入的价格；</li>
     *   <li>库存与余额扣减都使用带条件的 UPDATE（{@code WHERE inventory >= ?}），
     *       由数据库保证不超卖、不透支；</li>
     *   <li>全部写操作在同一事务内，失败整体回滚。</li>
     * </ol>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void commit(DonateCommitDTO dto, Integer uid) {
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BizException(ResultBean.NOT_FOUND, "用户不存在");
        }
        Pet pet = petMapper.selectById(dto.getPetId());
        if (pet == null) {
            throw new BizException(ResultBean.NOT_FOUND, "宠物不存在");
        }

        int total = 0;
        List<Food> orderedItems = new ArrayList<>();
        for (DonateCommitDTO.Item item : dto.getItems()) {
            Food food = foodMapper.selectById(item.getFoodId());
            if (food == null) {
                throw new BizException("救助品不存在");
            }
            int count = item.getCount();

            // 带库存下限条件的原子扣减：库存不足时影响行数为 0
            int affected = foodMapper.update(null, new LambdaUpdateWrapper<Food>()
                    .setSql("inventory = inventory - " + count)
                    .eq(Food::getId, food.getId())
                    .ge(Food::getInventory, count));
            if (affected != 1) {
                throw new BizException("「" + food.getFoodname() + "」库存不足");
            }

            food.setInventory(food.getInventory() - count);
            food.setTempCount(count);
            orderedItems.add(food);
            total += food.getPrice() * count;
        }
        if (total <= 0) {
            throw new BizException("请至少选择一件救助品");
        }

        // 带余额下限条件的原子扣减：余额不足时影响行数为 0
        int deducted = userMapper.update(null, new LambdaUpdateWrapper<User>()
                .setSql("pet_coin = pet_coin - " + total)
                .eq(User::getId, uid)
                .ge(User::getPetCoin, total));
        if (deducted != 1) {
            throw new BizException("宠宠币不足，当前为 " + user.getPetCoin());
        }

        // 宠物收到同额宠宠币
        petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                .setSql("pet_coin = pet_coin + " + total)
                .eq(Pet::getId, pet.getId()));

        // 落库捐助记录：冗余的 username / nickname / petname 由服务端填充，不接受前端传入
        Donate donate = new Donate();
        donate.setUid(uid);
        donate.setPid(pet.getId());
        donate.setUsername(user.getUsername());
        donate.setNickname(user.getNickname());
        donate.setPetname(pet.getPetname());
        donate.setDonateList(writeItems(orderedItems));
        donate.setTotalPrice(total);
        donate.setState(Donate.STATE_PENDING);
        donate.setIsRead(Donate.READ);
        donateMapper.insert(donate);
    }

    /**
     * 兼容保留：管理员手工补录捐助记录。
     * 因为不涉及余额流转，这里只做入库。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addonedonate(Donate donate) {
        return donateMapper.insert(donate) == 1;
    }

    /**
     * 某用户的捐助记录。
     * 改造前对每条记录单独查宠物（N+1），现改为批量。
     */
    @Override
    public List<Donate> queryalldonatebyid(Integer id) {
        List<Donate> donates = donateMapper.selectList(
                new LambdaQueryWrapper<Donate>().eq(Donate::getUid, id));
        fillPet(donates);
        return donates;
    }

    /**
     * 捐助次数与金额排行榜。
     * 改造前对排名中每个用户单独查一次（N+1），现改为一次批量查询。
     */
    @Override
    public List<Map<String, Object>> query10user() {
        QueryWrapper<Donate> qw = new QueryWrapper<>();
        qw.select("uid", "count(uid) as count", "sum(total_price) as totalPrice");
        qw.groupBy("uid");
        qw.orderByDesc("sum(total_price)");
        qw.last("limit 10");
        List<Map<String, Object>> list = donateMapper.selectMaps(qw);
        if (list.isEmpty()) {
            return list;
        }
        Set<Integer> uids = list.stream()
                .map(row -> row.get("uid"))
                .filter(Objects::nonNull)
                .map(v -> ((Number) v).intValue())
                .collect(Collectors.toSet());
        Map<Integer, User> userMap = uids.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(uids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        list.forEach(row -> {
            Object uid = row.get("uid");
            if (uid instanceof Number) {
                row.put("user", userMap.get(((Number) uid).intValue()));
            }
        });
        return list;
    }

    @Override
    public Pagination<Donate> queryalldonatebypage(int current, QueryWrapper<Donate> qw) {
        IPage<Donate> pageInfo = donateMapper.selectPage(new Page<>(normalizePage(current), PAGE_SIZE), qw);
        List<Donate> donateList = pageInfo.getRecords();
        fillUserAndPet(donateList);
        return Pagination.of(pageInfo, donateList);
    }

    @Override
    public Pagination<Donate> conditionquery(int current, String column, String condition) {
        QueryFieldGuard.Mode mode = QueryFieldGuard.check("donate", column);
        QueryWrapper<Donate> qw = new QueryWrapper<>();
        if (mode == QueryFieldGuard.Mode.LIKE) {
            qw.like(column, condition);
        } else {
            qw.eq(column, condition);
        }
        return queryalldonatebypage(current, qw);
    }

    /** 管理员将捐助标记为"已完成分配"，同时给捐助人发站内通知 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatedonatestate(Donate donate) {
        Donate exists = donateMapper.selectById(donate.getId());
        if (exists == null) {
            throw new BizException(ResultBean.NOT_FOUND, "捐助记录不存在");
        }
        donate.setState(Donate.STATE_DONE);
        donate.setIsRead(Donate.UNREAD);
        boolean ok = donateMapper.updateById(donate) == 1;
        if (ok) {
            Pet pet = petMapper.selectById(exists.getPid());
            Message message = new Message();
            message.setUid(exists.getUid());
            message.setContent("💝 您对「" + (pet == null ? "小动物" : pet.getPetname())
                    + "」的捐助（共 " + exists.getTotalPrice() + " 宠宠币）已完成物资分配，感谢您的爱心！");
            message.setIsRead(Message.UNREAD);
            messageService.addonemessage(message);
        }
        return ok;
    }

    /**
     * 批量标记该用户的捐助通知为已读。
     * 改造前方法名误写为 readalladopt（从领养模块复制而来），且逐条 update，现一并修正。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean readalldonate(Integer uid) {
        donateMapper.update(null, new LambdaUpdateWrapper<Donate>()
                .set(Donate::getIsRead, Donate.READ)
                .eq(Donate::getUid, uid)
                .eq(Donate::getIsRead, Donate.UNREAD));
        return true;
    }

    @Override
    public Long queryallnoread(Integer uid) {
        return donateMapper.selectCount(new LambdaQueryWrapper<Donate>()
                .eq(Donate::getUid, uid)
                .eq(Donate::getIsRead, Donate.UNREAD));
    }

    private String writeItems(List<Food> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            throw new BizException("捐助清单序列化失败");
        }
    }

    private void fillPet(List<Donate> donates) {
        if (donates == null || donates.isEmpty()) {
            return;
        }
        Set<Integer> pids = donates.stream().map(Donate::getPid)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (pids.isEmpty()) {
            return;
        }
        Map<Integer, Pet> petMap = petMapper.selectBatchIds(pids).stream()
                .collect(Collectors.toMap(Pet::getId, Function.identity()));
        donates.forEach(donate -> donate.setPet(petMap.get(donate.getPid())));
    }

    private void fillUserAndPet(List<Donate> donates) {
        if (donates == null || donates.isEmpty()) {
            return;
        }
        Set<Integer> uids = donates.stream().map(Donate::getUid)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Integer> pids = donates.stream().map(Donate::getPid)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, User> userMap = uids.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(uids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Integer, Pet> petMap = pids.isEmpty() ? Collections.emptyMap()
                : petMapper.selectBatchIds(pids).stream()
                .collect(Collectors.toMap(Pet::getId, Function.identity()));
        donates.forEach(donate -> {
            donate.setUser(userMap.get(donate.getUid()));
            donate.setPet(petMap.get(donate.getPid()));
        });
    }

    private int normalizePage(int current) {
        return current < 1 ? 1 : current;
    }
}
