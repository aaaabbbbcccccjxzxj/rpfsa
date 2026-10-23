package com.erdongj.controller;

import com.erdongj.common.RequireAdmin;
import com.erdongj.common.RequireLogin;
import com.erdongj.pojo.Comment;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.ResultBean;
import com.erdongj.service.CommentService;
import com.erdongj.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Erdong J
 * @Date: 2023/5/3 17:30
 * @Description: 评论控制层
 */

@RestController
@RequestMapping("/comment")
public class CommentController extends BaseController {

    @Autowired
    private CommentService commentService;

    /**
     * 发表评论。
     * uid 以 token 为准，防止伪造他人身份发评论；
     * 评论数由 CommentService 在同一事务内维护，前端不再回写计数。
     */
    @PostMapping
    @RequireLogin
    public ResultBean<Void> addonecomment(@RequestBody Comment comment,
                                          @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid) {
        comment.setUid(currentUid);
        if (comment.getLevel() == null) {
            comment.setLevel(Comment.TOP_LEVEL);
        }
        return commentService.addonecomment(comment) ? successful("评论成功") : faile("评论失败");
    }

    /** 删除二级评论（只能删自己的，管理员除外） */
    @DeleteMapping("/{id}")
    @RequireLogin
    public ResultBean<Void> deletetype2comment(@PathVariable Integer id,
                                               @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                               @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        boolean deleted = commentService.deletetypetwocomment(id, currentUid,
                TokenUtil.ROLE_ADMIN.equals(role));
        return deleted ? successful("删除成功") : faile("删除失败");
    }

    /** 删除一级评论及其子评论 */
    @DeleteMapping("/t1/{id}")
    @RequireLogin
    public ResultBean<Integer> deletetype1comment(@PathVariable Integer id,
                                                  @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                                  @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        int deleted = commentService.deletetypeonecomment(id, currentUid,
                TokenUtil.ROLE_ADMIN.equals(role));
        return deleted == 0 ? faile("删除失败") : successful("删除成功", deleted);
    }

    /** 评论分页（管理端），支持按内容模糊搜索 */
    @GetMapping(path = {"/page/{c}", "/page/{c}/{col}/{con}"})
    @RequireAdmin
    public ResultBean<Pagination<Comment>> queryByPage(@PathVariable("c") int current,
                                                       @PathVariable(value = "col", required = false) String column,
                                                       @PathVariable(value = "con", required = false) String condition) {
        Pagination<Comment> pagination = commentService.queryCommentsByPage(current, column, condition);
        if (pagination.getTotal() == 0) {
            return faile("暂无评论数据");
        }
        return successful("查询成功", pagination);
    }
}
