package com.erdongj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.erdongj.mapper.AdminMapper;
import com.erdongj.mapper.AdoptMapper;
import com.erdongj.mapper.DonateMapper;
import com.erdongj.mapper.PetMapper;
import com.erdongj.mapper.PostMapper;
import com.erdongj.mapper.UserMapper;
import com.erdongj.pojo.Admin;
import com.erdongj.pojo.Adopt;
import com.erdongj.pojo.Donate;
import com.erdongj.pojo.Pet;
import com.erdongj.pojo.Post;
import com.erdongj.service.AdminService;
import com.erdongj.service.MessageService;
import com.erdongj.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 管理员服务层
 * @author: Erdong J
 * @create: 2023-03-08 21:25
 **/
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PetMapper petMapper;
    @Autowired
    private AdoptMapper adoptMapper;
    @Autowired
    private DonateMapper donateMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private MessageService messageService;
    @Autowired
    private PasswordUtil passwordUtil;

    /**
     * 管理员登录验证。
     * <p>
     * 改造前用 {@code eq(password, ...)} 在 SQL 里直接比对明文；
     * 现在改为按账号取出后由 {@link PasswordUtil} 校验，并兼容历史明文数据的自动升级。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean Login(String name, String password) {
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getAdminname, name));
        if (admin == null || !passwordUtil.matches(password, admin.getPassword())) {
            return false;
        }
        if (!passwordUtil.isEncoded(admin.getPassword())) {
            Admin upgrade = new Admin();
            upgrade.setAdminname(admin.getAdminname());
            upgrade.setPassword(passwordUtil.encode(password));
            adminMapper.updateById(upgrade);
        }
        return true;
    }

    /**
     * 后台管理员查看总信息。
     * <p>
     * 未读消息数原先在 Controller 里"查出所有用户再逐个 selectCount"（N+1），
     * 现由 MessageService 用一条聚合查询给出。
     */
    @Override
    public Map<String, Long> getallinfo() {
        Map<String, Long> map = new HashMap<>();
        // 用户数量
        map.put("userCount", userMapper.selectCount(null));
        // 流浪猫狗总数 / 类型分布（type：0=平台流浪，1=私人上传）
        map.put("petCount", petMapper.selectCount(null));
        map.put("type0petCount", petMapper.selectCount(new QueryWrapper<Pet>().eq("type", 0)));
        map.put("type1petCount", petMapper.selectCount(new QueryWrapper<Pet>().eq("type", 1)));
        // 领养状态分布（is_adopt：0=流浪中，1=已领养，2=审核中）
        map.put("state0petCount", petMapper.selectCount(new QueryWrapper<Pet>().eq("is_adopt", 0)));
        map.put("state1petCount", petMapper.selectCount(new QueryWrapper<Pet>().eq("is_adopt", 1)));
        map.put("state2petCount", petMapper.selectCount(new QueryWrapper<Pet>().eq("is_adopt", 2)));
        // 用户发帖数量（uid=0 为管理员发布）
        map.put("postCount", postMapper.selectCount(new QueryWrapper<Post>().ne("uid", 0)));
        // 领养申请：2=未处理，1=已通过
        map.put("adoptCount", adoptMapper.selectCount(new QueryWrapper<Adopt>().eq("state", 2)));
        map.put("haveAdoptCount", adoptMapper.selectCount(new QueryWrapper<Adopt>().eq("state", 1)));
        // 捐助分配：0=未完成分配，1=已完成分配
        map.put("donateCount", donateMapper.selectCount(new QueryWrapper<Donate>().eq("state", 0)));
        map.put("haveDonateCount", donateMapper.selectCount(new QueryWrapper<Donate>().eq("state", 1)));
        // 未读消息总数
        map.put("messageCount", messageService.countNoReadForAdminTotal());
        return map;
    }

    /**
     * 近 6 个月趋势：三条按月聚合查询（领养通过数 / 捐助总额 / 新增用户），
     * 再按月对齐补零，保证前端图表 X 轴连续。
     */
    @Override
    public Map<String, Object> getTrends() {
        // 近 6 个月（含当月）的起止
        java.time.LocalDate firstMonth = java.time.LocalDate.now().minusMonths(5).withDayOfMonth(1);
        java.util.List<String> months = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            months.add(firstMonth.plusMonths(i).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        String since = firstMonth.atStartOfDay().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String, Long> adoptByMonth = aggregateByMonth(adoptMapper, "adopt", "apply_time",
                "state = 1", since);
        Map<String, Long> userByMonth = aggregateByMonth(userMapper, "user", "create_time",
                null, since);
        Map<String, Long> donateByMonth = aggregateByMonth(donateMapper, "donate", "create_time",
                "state = 1", since, "IFNULL(SUM(total_price),0)");

        Map<String, Object> result = new HashMap<>();
        result.put("months", months);
        result.put("adoptCounts", months.stream().map(m -> adoptByMonth.getOrDefault(m, 0L)).collect(java.util.stream.Collectors.toList()));
        result.put("userCounts", months.stream().map(m -> userByMonth.getOrDefault(m, 0L)).collect(java.util.stream.Collectors.toList()));
        result.put("donateCoins", months.stream().map(m -> donateByMonth.getOrDefault(m, 0L)).collect(java.util.stream.Collectors.toList()));
        return result;
    }

    /** 按月聚合计数 */
    private Map<String, Long> aggregateByMonth(com.baomidou.mybatisplus.core.mapper.BaseMapper<?> mapper,
                                               String table, String timeColumn, String extraCondition,
                                               String since) {
        return aggregateByMonth(mapper, table, timeColumn, extraCondition, since, "COUNT(*)");
    }

    /**
     * 按月聚合。selectMaps 直接走 MyBatis-Plus 的原始 SQL 组装，
     * 月份格式化与聚合表达式均为服务端常量，无注入面。
     */
    private Map<String, Long> aggregateByMonth(com.baomidou.mybatisplus.core.mapper.BaseMapper<?> mapper,
                                               String table, String timeColumn, String extraCondition,
                                               String since, String aggregateExpr) {
        QueryWrapper<Object> qw = new QueryWrapper<>();
        qw.select("DATE_FORMAT(" + timeColumn + ", '%Y-%m') AS ym", aggregateExpr + " AS val")
                .ge(timeColumn, since);
        if (extraCondition != null) {
            qw.apply(extraCondition);
        }
        qw.groupBy("ym");
        @SuppressWarnings({"unchecked", "rawtypes"})
        java.util.List<Map<String, Object>> rows = ((com.baomidou.mybatisplus.core.mapper.BaseMapper) mapper).selectMaps(qw);
        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object ym = row.get("ym");
            Object val = row.get("val");
            if (ym != null && val instanceof Number) {
                result.put(ym.toString(), ((Number) val).longValue());
            }
        }
        return result;
    }

    @Override
    public java.util.List<java.util.List<String>> exportAdoptRows() {
        java.util.List<Adopt> adopts = adoptMapper.selectList(
                new QueryWrapper<Adopt>().orderByDesc("apply_time"));
        java.util.List<java.util.List<String>> rows = new java.util.ArrayList<>();
        rows.add(java.util.Arrays.asList("ID", "申请人", "昵称", "宠物", "状态", "申请时间", "地址", "申请理由", "审核备注"));
        for (Adopt a : adopts) {
            String state = a.getState() == null ? "" : a.getState() == 1 ? "已通过" : a.getState() == 0 ? "未通过" : "审核中";
            rows.add(java.util.Arrays.asList(
                    String.valueOf(a.getId()),
                    safe(a.getUsername()), safe(a.getNickname()), safe(a.getPetname()), state,
                    a.getApplyTime() == null ? "" : new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(a.getApplyTime()),
                    safe(a.getAdress()), safe(a.getReason()), safe(a.getPonReason())));
        }
        return rows;
    }

    @Override
    public java.util.List<java.util.List<String>> exportDonateRows() {
        java.util.List<Donate> donates = donateMapper.selectList(
                new QueryWrapper<Donate>().orderByDesc("create_time"));
        java.util.List<java.util.List<String>> rows = new java.util.ArrayList<>();
        rows.add(java.util.Arrays.asList("ID", "用户名", "昵称", "宠物", "总宠宠币", "状态", "捐助时间"));
        for (Donate d : donates) {
            rows.add(java.util.Arrays.asList(
                    String.valueOf(d.getId()),
                    safe(d.getUsername()), safe(d.getNickname()), safe(d.getPetname()),
                    String.valueOf(d.getTotalPrice()),
                    d.getState() != null && d.getState() == 1 ? "已完成分配" : "未完成分配",
                    d.getCreateTime() == null ? "" : new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d.getCreateTime())));
        }
        return rows;
    }

    /** CSV 单元格兜底：null 转空串，逗号/引号/换行做转义 */
    private String safe(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
