package com.erdongj.service;

import com.erdongj.pojo.Comment;
import com.erdongj.pojo.Pagination;

/**
 * @Author: Erdong J
 * @Date: 2023/4/28 15:37
 * @Description:
 */
public interface CommentService {

    /**
     * 添加一个评论
     * @param comment
     * @return
     */
    boolean addonecomment(Comment comment);

    /**
     * 根据id删除一个二级评论（带归属校验：非管理员只能删自己的）
     *
     * @param id          评论 id
     * @param operatorUid 操作者 uid
     * @param isAdmin     操作者是否为管理员
     */
    boolean deletetypetwocomment(Integer id, Integer operatorUid, boolean isAdmin);

    /**
     * 根据id删除一个一级评论,一级评论下可能有二级评论（带归属校验）
     *
     * @param id          评论 id
     * @param operatorUid 操作者 uid
     * @param isAdmin     操作者是否为管理员
     * @return 实际删除条数（含子评论）
     */
    int deletetypeonecomment(Integer id, Integer operatorUid, boolean isAdmin);

    /**
     * 评论分页（管理端）。装配评论者与归属对象标题（帖子/宠物）。
     *
     * @param current   页码
     * @param column    可选条件列（白名单校验）
     * @param condition 可选条件值
     */
    Pagination<Comment> queryCommentsByPage(int current, String column, String condition);
}
