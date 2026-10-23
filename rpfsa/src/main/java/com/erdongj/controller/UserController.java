package com.erdongj.controller;

import com.erdongj.common.RequireLogin;
import com.erdongj.dto.LoginDTO;
import com.erdongj.dto.ResetCodeDTO;
import com.erdongj.dto.ResetPasswordDTO;
import com.erdongj.dto.UserProfileDTO;
import com.erdongj.dto.UserRegisterDTO;
import com.erdongj.pojo.Adopt;
import com.erdongj.pojo.ResultBean;
import com.erdongj.pojo.User;
import com.erdongj.service.UserService;
import com.erdongj.util.TokenUtil;
import com.erdongj.util.SimpleCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 用户端控制层
 * @author: Erdong J
 * @create: 2023-03-17 21:09
 **/
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private SimpleCache simpleCache;

    /** 领养榜：领养数量前 10 的用户（游客可访问，60s 缓存） */
    @GetMapping
    public ResultBean<List<User>> get10users() {
        return successful("查询成功", simpleCache.get("rank:users", userService::queryallusers));
    }

    /**
     * 我的领养记录。
     * 路径保留 uid 以兼容既有前端，但服务端会校验它必须是自己（管理员除外）。
     */
    @GetMapping("/myadopt/{uid}")
    @RequireLogin
    public ResultBean<List<Adopt>> getmyadopt(@PathVariable Integer uid,
                                              @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                              @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (!TokenUtil.ROLE_ADMIN.equals(role) && !uid.equals(currentUid)) {
            return fail(ResultBean.FORBIDDEN, "无权查看他人的领养记录");
        }
        List<Adopt> adoptList = userService.querymyadopt(uid);
        if (adoptList.isEmpty()) {
            return faile("暂无领养");
        }
        return successful("查询成功", adoptList);
    }

    @GetMapping("/{id}")
    public ResultBean<User> getuserbyid(@PathVariable Integer id) {
        User user = userService.queryonebyID(id);
        return user == null ? faile("用户不存在") : successful("查询成功", user);
    }

    /**
     * 用户注册。
     * 改造前与"管理员新增用户"共用 {@code POST /user}，现已拆分。
     */
    @PostMapping("/register")
    public ResultBean<Void> register(@RequestBody @Valid UserRegisterDTO dto) {
        return userService.register(dto) ? successful("注册成功") : faile("注册失败");
    }

    /**
     * 用户登录。
     * 返回 {@code {token, user}}，前端后续请求携带 {@code Authorization: Bearer <token>}。
     * user 中已不含 password（见 User#password 上的 @JsonIgnore）。
     */
    @PostMapping("/login")
    public ResultBean<Map<String, Object>> userlogin(@RequestBody @Valid LoginDTO dto) {
        User user = userService.userlogin(dto.getUsername(), dto.getPassword());
        if (user == null) {
            return faile("对不起,用户名不存在或密码错误");
        }
        Map<String, Object> payload = new HashMap<>(4);
        payload.put("token", tokenUtil.issue(user.getId(), TokenUtil.ROLE_USER));
        payload.put("user", user);
        return successful("登录成功", payload);
    }

    /**
     * 修改自己的资料。
     * uid 取自 token 而非请求体，杜绝"改别人资料"与"自行提权/改积分"。
     */
    @PatchMapping
    @RequireLogin
    public ResultBean<User> updateProfile(@RequestBody @Valid UserProfileDTO dto,
                                          @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer uid) {
        return successful("修改成功", userService.updateProfile(uid, dto));
    }

    /**
     * 找回密码：发送验证码（游客可访问）。
     * 演示环境无邮件通道，验证码随响应返回由前端回显。
     */
    @PostMapping("/password/code")
    public ResultBean<Map<String, Object>> sendResetCode(@RequestBody @Valid ResetCodeDTO dto) {
        String code = userService.sendResetCode(dto.getEmail());
        Map<String, Object> payload = new HashMap<>(2);
        payload.put("code", code);
        return successful("验证码已生成（演示模式直接回显，10 分钟内有效）", payload);
    }

    /** 找回密码：凭验证码重置（游客可访问） */
    @PostMapping("/password/reset")
    public ResultBean<Void> resetPassword(@RequestBody @Valid ResetPasswordDTO dto) {
        userService.resetPassword(dto.getEmail(), dto.getCode(), dto.getNewPassword());
        return successful("密码重置成功，请使用新密码登录");
    }
}
