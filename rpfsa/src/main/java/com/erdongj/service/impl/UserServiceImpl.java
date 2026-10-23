package com.erdongj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erdongj.common.BizException;
import com.erdongj.dto.UserAdminDTO;
import com.erdongj.dto.UserProfileDTO;
import com.erdongj.dto.UserRegisterDTO;
import com.erdongj.mapper.AdoptMapper;
import com.erdongj.mapper.PetMapper;
import com.erdongj.mapper.UserMapper;
import com.erdongj.mapper.VerifyCodeMapper;
import com.erdongj.pojo.Adopt;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.Pet;
import com.erdongj.pojo.ResultBean;
import com.erdongj.pojo.User;
import com.erdongj.pojo.VerifyCode;
import com.erdongj.service.MessageService;
import com.erdongj.service.UserService;
import com.erdongj.util.PasswordUtil;
import com.erdongj.util.QueryFieldGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @description: 用户业务层
 * @author: Erdong J
 * @create: 2023-03-16 21:37
 **/
@Service
public class UserServiceImpl implements UserService {

    /** 管理端用户列表每页条数（与前端分页元数据保持一致） */
    private static final int PAGE_SIZE = 10;

    /** 新用户默认评论权限：1=允许 */
    private static final int DEFAULT_COMMENT_PERMISSION = 1;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdoptMapper adoptMapper;

    @Autowired
    private MessageService messageService;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private VerifyCodeMapper verifyCodeMapper;

    /** 领养榜前 10：只取榜单展示需要的列，不带手机号/邮箱等隐私字段 */
    @Override
    public List<User> queryallusers() {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .select(User::getId, User::getUsername, User::getNickname,
                        User::getPhoto, User::getHowAdopt)
                .orderByDesc(User::getHowAdopt)
                .last("limit 10"));
    }

    @Override
    public User queryonebyID(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 我的领养记录。
     * <p>
     * 改造前：对每条申请单独 {@code petMapper.selectById} 装配宠物（N+1）。
     * 现在一次性批量取出再按 id 映射。
     */
    @Override
    public List<Adopt> querymyadopt(Integer uid) {
        List<Adopt> adoptList = adoptMapper.selectList(
                new LambdaQueryWrapper<Adopt>().eq(Adopt::getUid, uid));
        if (adoptList.isEmpty()) {
            return adoptList;
        }
        Set<Integer> petIds = adoptList.stream()
                .map(Adopt::getPid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, Pet> petMap = petIds.isEmpty()
                ? Map.of()
                : petMapper.selectBatchIds(petIds).stream()
                .collect(Collectors.toMap(Pet::getId, Function.identity()));
        adoptList.forEach(adopt -> adopt.setPet(petMap.get(adopt.getPid())));
        return adoptList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteonebyID(Integer id) {
        // 逻辑删除：保留历史帖子 / 评论 / 捐助记录的外键可追溯性
        return userMapper.deleteById(id) == 1;
    }

    /**
     * 用户登录。
     * <p>
     * 密码统一走 {@link PasswordUtil#matches}：兼容历史明文数据，并在首次校验通过后
     * 自动把明文升级为 BCrypt 密文，实现无感迁移。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User userlogin(String username, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null || !passwordUtil.matches(password, user.getPassword())) {
            return null;
        }
        if (!passwordUtil.isEncoded(user.getPassword())) {
            User upgrade = new User();
            upgrade.setId(user.getId());
            upgrade.setPassword(passwordUtil.encode(password));
            userMapper.updateById(upgrade);
            user.setPassword(null);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(UserRegisterDTO dto) {
        Long exists = userMapper.selectCount(
                new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (exists != null && exists > 0) {
            throw new BizException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordUtil.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        // 权限与积分由服务端给定初始值，不接受前端传入
        user.setComPermiss(DEFAULT_COMMENT_PERMISSION);
        user.setPetCoin(0);
        user.setHowAdopt(0);
        return userMapper.insert(user) == 1;
    }

    /**
     * 用户修改自己的资料。可改字段由 {@link UserProfileDTO} 白名单限定，
     * 权限位与积分无法经由本方法变更。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateProfile(Integer uid, UserProfileDTO dto) {
        User exists = userMapper.selectById(uid);
        if (exists == null) {
            throw new BizException(ResultBean.NOT_FOUND, "用户不存在");
        }
        if (dto.getUsername() != null && !dto.getUsername().equals(exists.getUsername())) {
            Long dup = userMapper.selectCount(
                    new QueryWrapper<User>().eq("username", dto.getUsername()));
            if (dup != null && dup > 0) {
                throw new BizException("用户名已被占用");
            }
        }
        User update = new User();
        update.setId(uid);
        update.setUsername(dto.getUsername());
        update.setNickname(dto.getNickname());
        update.setEmail(dto.getEmail());
        update.setPhone(dto.getPhone());
        update.setPhoto(dto.getPhoto());
        userMapper.updateById(update);
        return userMapper.selectById(uid);
    }

    /**
     * 管理员新增或修改用户（id 为空表示新增）。
     * 这是唯一允许变更 {@code comPermiss} / {@code petCoin} 的入口。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User adminSave(UserAdminDTO dto) {
        if (dto.getId() == null) {
            if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
                throw new BizException("用户名不能为空");
            }
            Long dup = userMapper.selectCount(
                    new QueryWrapper<User>().eq("username", dto.getUsername()));
            if (dup != null && dup > 0) {
                throw new BizException("用户名已存在");
            }
            if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
                throw new BizException("请为新用户设置初始密码");
            }
            User user = new User();
            user.setUsername(dto.getUsername());
            user.setPassword(passwordUtil.encode(dto.getPassword()));
            user.setNickname(dto.getNickname());
            user.setEmail(dto.getEmail());
            user.setPhone(dto.getPhone());
            user.setPhoto(dto.getPhoto());
            user.setComPermiss(dto.getComPermiss() == null ? DEFAULT_COMMENT_PERMISSION : dto.getComPermiss());
            user.setPetCoin(dto.getPetCoin() == null ? 0 : dto.getPetCoin());
            user.setHowAdopt(0);
            userMapper.insert(user);
            return user;
        }
        User update = new User();
        update.setId(dto.getId());
        update.setUsername(dto.getUsername());
        update.setNickname(dto.getNickname());
        update.setEmail(dto.getEmail());
        update.setPhone(dto.getPhone());
        update.setPhoto(dto.getPhoto());
        update.setComPermiss(dto.getComPermiss());
        update.setPetCoin(dto.getPetCoin());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            update.setPassword(passwordUtil.encode(dto.getPassword()));
        }
        userMapper.updateById(update);
        return userMapper.selectById(dto.getId());
    }

    @Override
    public Pagination<User> queryusersbypage(int current, QueryWrapper<User> qw) {
        IPage<User> page = userMapper.selectPage(new Page<>(normalizePage(current), PAGE_SIZE), qw);
        List<User> records = page.getRecords();
        fillNoReadCount(records);
        // size 由 IPage 提供，避免手写魔数与真实页大小不一致
        return Pagination.of(page, records);
    }

    @Override
    public Pagination<User> likequery(int current, String column, String condition) {
        QueryFieldGuard.Mode mode = QueryFieldGuard.check("user", column);
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (mode == QueryFieldGuard.Mode.LIKE) {
            qw.like(column, condition);
        } else {
            qw.eq(column, condition);
        }
        return queryusersbypage(current, qw);
    }

    /**
     * 为当前页用户填充"发给管理员的未读消息数"。
     * <p>
     * 改造前：管理端对表格每一行单独发一次 HTTP 请求去取未读数（前端 N+1），
     * 一页 10 行就是 11 个请求。现在由分页接口一次聚合查询带回。
     */
    private void fillNoReadCount(List<User> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        List<Integer> uids = users.stream().map(User::getId).collect(Collectors.toList());
        Map<Integer, Long> counts = messageService.countNoReadForAdmin(uids);
        users.forEach(user -> {
            Long count = counts.get(user.getId());
            if (count != null && count > 0) {
                user.setNoReadCount(count.intValue());
            }
        });
    }

    private int normalizePage(int current) {
        return current < 1 ? 1 : current;
    }

    /**
     * 找回密码：生成 6 位数字验证码。
     * 同一邮箱的旧验证码全部作废，只保留最新一条可用；有效期 10 分钟。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String sendResetCode(String email) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (exists == null || exists == 0) {
            throw new BizException("该邮箱未注册");
        }
        // 作废该邮箱所有未使用验证码
        verifyCodeMapper.update(null, new LambdaUpdateWrapper<VerifyCode>()
                .set(VerifyCode::getUsed, 1)
                .eq(VerifyCode::getEmail, email)
                .eq(VerifyCode::getUsed, 0));
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        verifyCodeMapper.insert(new VerifyCode(email, code));
        return code;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String email, String code, String newPassword) {
        VerifyCode verifyCode = verifyCodeMapper.selectOne(new LambdaQueryWrapper<VerifyCode>()
                .eq(VerifyCode::getEmail, email)
                .eq(VerifyCode::getCode, code)
                .eq(VerifyCode::getUsed, 0)
                .orderByDesc(VerifyCode::getId)
                .last("limit 1"));
        if (verifyCode == null) {
            throw new BizException("验证码错误或已失效");
        }
        if (verifyCode.getExpireTime().before(new java.util.Date())) {
            throw new BizException("验证码已过期，请重新获取");
        }
        User update = new User();
        update.setPassword(passwordUtil.encode(newPassword));
        int rows = userMapper.update(update, new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (rows != 1) {
            throw new BizException("该邮箱未注册");
        }
        // 验证码一次性使用
        VerifyCode used = new VerifyCode();
        used.setId(verifyCode.getId());
        used.setUsed(1);
        verifyCodeMapper.updateById(used);
    }
}
