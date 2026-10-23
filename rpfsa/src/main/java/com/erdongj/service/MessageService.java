package com.erdongj.service;

import com.erdongj.pojo.Message;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author: Erdong J
 * @Date: 2023/5/9 15:25
 * @Description:
 */
public interface MessageService {
    /**
     * 添加一条message记录
     *
     * @param message
     * @return
     */
    boolean addonemessage(Message message);

    /**
     * 根据用户id查询所有该用户的message
     *
     * @param uid
     * @return
     */
    List<Message> queryallmessagebyid(Integer uid);

    /**
     * 查询所有未读的Count
     *
     * @param uid      该用户的的message
     * @param identity 以该用户还是管理员的身份查询未读
     * @return
     */
    Long queryallnoread(Integer uid, String identity);

    /**
     * 修改所有未读为已读
     *
     * @param uid      该用户的的message
     * @param identity 以该用户还是管理员的身份查询未读
     * @return
     */
    boolean allhaveread(Integer uid, String identity);

    /**
     * 批量统计多个用户"发给管理员"的未读消息数。
     * <p>
     * 改造前：管理端用户列表对每一行单独发一次请求（前端 N+1），
     * 后台统计接口也对每个用户循环查询（后端 N+1）。
     * 现改为一次 GROUP BY 聚合查询，按 uid 返回计数。
     *
     * @param uids 用户 id 集合
     * @return uid -> 未读条数；没有未读的用户不会出现在 map 中
     */
    Map<Integer, Long> countNoReadForAdmin(Collection<Integer> uids);

    /**
     * 统计全站"用户发给管理员"的未读消息总数。
     * <p>
     * 改造前：后台统计接口先查出所有用户，再逐个 {@code selectCount}（用户越多越慢）。
     * 现在是一条聚合 COUNT 查询。
     */
    long countNoReadForAdminTotal();
}
