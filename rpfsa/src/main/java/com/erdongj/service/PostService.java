package com.erdongj.service;

import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.Post;

import java.util.List;

/**
 * @Author: Erdong J
 * @Date: 2023/4/28 15:29
 * @Description:
 */
public interface PostService {
    //帖子的话，只有增加，删除，查询方法

    /**
     * 添加一个帖子
     * @param post
     * @return
     */
    boolean addonepost(Post post);

    /**
     * 根据postid找到对应post,修改该post评论数
     * @param post
     * @return
     */
    boolean updatecommentcount(Post post);

    /**
     * 删除一个帖子（带归属校验）
     *
     * @param id          帖子 id
     * @param operatorUid 操作者 uid
     * @param isAdmin     操作者是否为管理员
     * @return 是否删除成功
     */
    boolean deleteonepost(Integer id, Integer operatorUid, boolean isAdmin);

    /**
     * 查询一个帖子（含评论树）。
     *
     * @param id         帖子 id
     * @param currentUid 当前登录用户 id（游客为 null），用于装配"是否已点赞"
     * @return
     */
    Post queryonepostbyid(Integer id, Integer currentUid);

    /**
     * 点赞/取消点赞（toggle）。
     * 点赞时给帖子作者 +2 宠宠币（自己的帖子不加）；计数全部走数据库原子操作。
     *
     * @return true 表示本次操作后为"已点赞"
     */
    boolean toggleLike(Integer uid, Integer poid);

    /**
     * 根据用户id查询该用户所有帖子
     * @param id
     * @return
     */
    List<Post> queryallmypostbyid(Integer id);


    /**
     * 拿到所有的公告post
     * @return
     */
    List<Post> queryall(int classic);
    /**
     * 分页查询所有帖子
     * @param current
     * @param type
     * @return
     */
    Pagination<Post> queryallpostsbypage(int current, int type, String column, String condition);

    /**
     * 查询前评论数前十的帖子
     * @return
     */
    List<Post> query10posts();
}
