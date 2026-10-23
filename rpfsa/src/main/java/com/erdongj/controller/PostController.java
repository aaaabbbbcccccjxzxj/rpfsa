package com.erdongj.controller;

import com.erdongj.common.RequireAdmin;
import com.erdongj.common.RequireLogin;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.Post;
import com.erdongj.pojo.ResultBean;
import com.erdongj.service.PostService;
import com.erdongj.util.TokenUtil;
import com.erdongj.util.SimpleCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: Erdong J
 * @Date: 2023/4/28 15:46
 * @Description: 帖子 / 公告控制层
 */

@RestController
@RequestMapping("/post")
public class PostController extends BaseController {

    @Autowired
    private PostService postService;

    @Autowired
    private SimpleCache simpleCache;

    /** 热门帖子榜（按评论数，60s 缓存） */
    @GetMapping
    public ResultBean<List<Post>> get10posts() {
        List<Post> posts = simpleCache.get("rank:posts", postService::query10posts);
        return successful("排行榜前十", posts);
    }

    /**
     * 首页 / 公告列表，按 classic 分类（游客可访问）
     *
     * @param classic -1 表示全部
     */
    @GetMapping("/alltypeone/{cl}")
    public ResultBean<List<Post>> getall(@PathVariable("cl") int classic) {
        List<Post> postList = postService.queryall(classic);
        if (postList.isEmpty()) {
            return faile("暂无数据");
        }
        return successful("公告信息", postList);
    }

    @GetMapping("/{id}")
    public ResultBean<Post> getonepostbyid(@PathVariable Integer id,
                                           @RequestAttribute(value = TokenUtil.REQ_ATTR_UID, required = false) Integer currentUid) {
        Post post = postService.queryonepostbyid(id, currentUid);
        return post == null ? faile("不存在") : successful("查询成功", post);
    }

    /** 点赞/取消点赞（需登录）。点赞会给帖子作者 +2 宠宠币 */
    @PostMapping("/{id}/like")
    @RequireLogin
    public ResultBean<Boolean> toggleLike(@PathVariable Integer id,
                                          @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid) {
        boolean liked = postService.toggleLike(currentUid, id);
        return successful(liked ? "点赞成功，作者宠宠币 +2" : "已取消点赞", liked);
    }

    /** 我的帖子，只允许查自己 */
    @GetMapping("/my/{id}")
    @RequireLogin
    public ResultBean<List<Post>> getmypostsbyid(@PathVariable Integer id,
                                                 @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                                 @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (!TokenUtil.ROLE_ADMIN.equals(role) && !id.equals(currentUid)) {
            return fail(ResultBean.FORBIDDEN, "无权查看他人的帖子");
        }
        List<Post> postList = postService.queryallmypostbyid(id);
        if (postList.isEmpty()) {
            return faile("暂无发帖信息");
        }
        return successful("查询成功", postList);
    }

    /**
     * 帖子分页。
     * 两种形态：{@code /post/{c}/{t}} 与 {@code /post/{c}/{t}/{col}/{con}}，
     * 其中 type=0 为社区帖、type=1 为公告。
     */
    @GetMapping(path = {"/{c}/{t}", "/{c}/{t}/{col}/{con}"})
    public ResultBean<Pagination<Post>> getpostsbypage(
            @PathVariable("c") int current,
            @PathVariable("t") int type,
            @PathVariable(value = "col", required = false) String column,
            @PathVariable(value = "con", required = false) String condition) {
        Pagination<Post> pagination = postService.queryallpostsbypage(current, type, column, condition);
        if (pagination.getTotal() == 0) {
            return faile("暂无数据");
        }
        return successful("查询成功", pagination);
    }

    /**
     * 发帖 / 发公告。正文会经过 HTML 白名单清洗后再入库。
     * <p>
     * 普通用户发帖时强制 uid 为当前登录用户、type 为社区帖、取消置顶，
     * 避免伪造管理员身份发布"官方公告"。
     */
    @PostMapping
    public ResultBean<Void> addonepost(@RequestBody Post post,
                                       @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                       @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (TokenUtil.ROLE_ADMIN.equals(role)) {
            if (post.getUid() == null) {
                post.setUid(0);
            }
        } else {
            post.setUid(currentUid);
            post.setType(Post.TYPE_POST);
            post.setIsPinned(0);
        }
        post.setCommentCount(0);
        return postService.addonepost(post) ? successful("添加成功") : faile("添加失败");
    }

    /**
     * 删除帖子。普通用户只能删自己的，管理员可删任意。
     * 归属校验放在 Service 内，避免"前端传什么就删什么"。
     */
    @DeleteMapping("/{id}")
    @RequireLogin
    public ResultBean<Void> deleteonepost(@PathVariable Integer id,
                                          @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                          @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        boolean deleted = postService.deleteonepost(id, currentUid, TokenUtil.ROLE_ADMIN.equals(role));
        return deleted ? successful("删除成功") : faile("删除失败");
    }

    /**
     * 修改帖子（含评论数）。
     * 评论数现已由 CommentService 在评论增删时于同事务内维护，
     * 该接口只保留给管理端做数据修正。
     */
    @PatchMapping
    @RequireAdmin
    public ResultBean<Void> updatepost(@RequestBody Post post) {
        return postService.updatecommentcount(post) ? successful("修改成功") : faile("修改失败");
    }
}
