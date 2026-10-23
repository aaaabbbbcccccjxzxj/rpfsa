package com.erdongj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.erdongj.dto.UserAdminDTO;
import com.erdongj.dto.UserProfileDTO;
import com.erdongj.dto.UserRegisterDTO;
import com.erdongj.pojo.Adopt;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.User;

import java.util.List;

/**
 * @description: 用户业务层
 * @author: Erdong J
 * @create: 2023-03-16 21:31
 **/
public interface UserService {

    /**
     * 查询领养数量前10的用户
     *
     * @return
     */
    List<User> queryallusers();

    /**
     * 根据ID查询用户
     *
     * @param id
     * @return
     */
    User queryonebyID(Integer id);

    /**
     * 查询我的领养信息
     *
     * @param uid
     * @return
     */
    List<Adopt> querymyadopt(Integer uid);

    /**
     * 管理员根据ID删除用户（逻辑删除）
     *
     * @param id
     * @return
     */
    boolean deleteonebyID(Integer id);

    /**
     * 用户登录
     *
     * @param username
     * @param password
     * @return
     */
    User userlogin(String username, String password);

    /**
     * 用户注册
     *
     * @param dto
     * @return
     */
    boolean register(UserRegisterDTO dto);

    /**
     * 用户修改自己的资料（字段白名单由 UserProfileDTO 限定）
     *
     * @param uid
     * @param dto
     * @return 修改后的最新用户信息
     */
    User updateProfile(Integer uid, UserProfileDTO dto);

    /**
     * 管理员添加用户/管理员修改用户
     *
     * @param dto
     * @return
     */
    User adminSave(UserAdminDTO dto);

    /**
     * 用户分页查询
     *
     * @param current
     * @param qw
     * @return
     */
    Pagination<User> queryusersbypage(int current, QueryWrapper<User> qw);

    /**
     * 用户模糊查询
     *
     * @param current
     * @param column
     * @param condition
     * @return
     */
    Pagination<User> likequery(int current, String column, String condition);

    /**
     * 找回密码：为邮箱对应的账号生成验证码。
     * 演示环境无邮件通道，验证码随响应返回（由前端回显）；生产接入邮件服务后改为只提示"已发送"。
     *
     * @param email 注册邮箱
     * @return 验证码（演示模式）
     */
    String sendResetCode(String email);

    /**
     * 找回密码：凭有效验证码重置密码（BCrypt 入库，验证码一次性使用）。
     */
    void resetPassword(String email, String code, String newPassword);

}
