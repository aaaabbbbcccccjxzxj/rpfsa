package com.erdongj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erdongj.common.BizException;
import com.erdongj.mapper.CommentMapper;
import com.erdongj.mapper.PetMapper;
import com.erdongj.mapper.PostMapper;
import com.erdongj.mapper.UserMapper;
import com.erdongj.pojo.Comment;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.Pet;
import com.erdongj.pojo.Post;
import com.erdongj.pojo.ResultBean;
import com.erdongj.pojo.User;
import com.erdongj.service.CommentService;
import com.erdongj.util.QueryFieldGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: Erdong J
 * @Date: 2023/4/28 15:40
 * @Description:
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private UserMapper userMapper;

    /** 管理端评论列表每页条数 */
    private static final int PAGE_SIZE = 10;

    /**
     * 评论分页（管理端）。固定 4 次查询：评论分页、涉及用户、涉及帖子、涉及宠物。
     */
    @Override
    public Pagination<Comment> queryCommentsByPage(int current, String column, String condition) {
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        if (column != null && condition != null && !condition.isEmpty()) {
            QueryFieldGuard.Mode mode = QueryFieldGuard.check("comment", column);
            if (mode == QueryFieldGuard.Mode.LIKE) {
                qw.like(column, condition);
            } else {
                qw.eq(column, condition);
            }
        }
        qw.orderByDesc("create_time");
        IPage<Comment> pageInfo = commentMapper.selectPage(
                new Page<>(Math.max(current, 1), PAGE_SIZE), qw);
        List<Comment> records = pageInfo.getRecords();
        if (!records.isEmpty()) {
            // 评论者
            Set<Integer> uids = records.stream().map(Comment::getUid)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Integer, User> userMap = uids.isEmpty() ? Collections.emptyMap()
                    : userMapper.selectBatchIds(uids).stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));
            // 归属帖子 / 宠物标题
            Set<Integer> postIds = records.stream().map(Comment::getPoid)
                    .filter(id -> id != null && id != 0).collect(Collectors.toSet());
            Set<Integer> petIds = records.stream().map(Comment::getPid)
                    .filter(id -> id != null && id != 0).collect(Collectors.toSet());
            Map<Integer, Post> postMap = postIds.isEmpty() ? Collections.emptyMap()
                    : postMapper.selectBatchIds(postIds).stream()
                    .collect(Collectors.toMap(Post::getId, Function.identity()));
            Map<Integer, Pet> petMap = petIds.isEmpty() ? Collections.emptyMap()
                    : petMapper.selectBatchIds(petIds).stream()
                    .collect(Collectors.toMap(Pet::getId, Function.identity()));
            records.forEach(comment -> {
                comment.setUser(userMap.get(comment.getUid()));
                if (comment.getPoid() != null && comment.getPoid() != 0) {
                    Post post = postMap.get(comment.getPoid());
                    comment.setTargetTitle(post == null ? "帖子已删除" : "帖子：" + post.getTitle());
                } else {
                    Pet pet = petMap.get(comment.getPid());
                    comment.setTargetTitle(pet == null ? "宠物已删除" : "宠物：" + pet.getPetname());
                }
            });
        }
        return Pagination.of(pageInfo, records);
    }

    /**
     * 新增评论，并在同一事务内维护所属帖子 / 宠物的评论数。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addonecomment(Comment comment) {
        if (commentMapper.insert(comment) != 1) {
            return false;
        }
        adjustCommentCount(comment, 1);
        return true;
    }

    /**
     * 删除二级评论。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletetypetwocomment(Integer id, Integer operatorUid, boolean isAdmin) {
        Comment comment = requireOwnedComment(id, operatorUid, isAdmin);
        if (commentMapper.deleteById(id) != 1) {
            return false;
        }
        adjustCommentCount(comment, -1);
        return true;
    }

    /**
     * 删除一级评论，及其下挂的所有二级评论。
     *
     * @return 实际删除的条数（含子评论），供调用方展示提示
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletetypeonecomment(Integer id, Integer operatorUid, boolean isAdmin) {
        Comment comment = requireOwnedComment(id, operatorUid, isAdmin);
        int deleted = commentMapper.delete(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getId, id)
                .or()
                .eq(Comment::getLevel, id));
        if (deleted > 0) {
            adjustCommentCount(comment, -deleted);
        }
        return deleted;
    }

    /**
     * 取出评论并校验归属：非管理员只能操作自己发布的评论。
     */
    private Comment requireOwnedComment(Integer id, Integer operatorUid, boolean isAdmin) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BizException(ResultBean.NOT_FOUND, "评论不存在");
        }
        if (!isAdmin && !Objects.equals(comment.getUid(), operatorUid)) {
            throw new BizException(ResultBean.FORBIDDEN, "只能删除自己的评论");
        }
        return comment;
    }

    /**
     * 维护 comment_count。
     * <p>
     * 改造前由前端"读出当前值 ± 1 再回写"，属于 read-modify-write，
     * 并发或请求重试都会让计数漂移；现改为数据库侧的原子自增/自减，
     * 并用 GREATEST 兜底避免出现负数。
     * <p>
     * 评论要么挂在帖子（poid）上，要么挂在宠物（pid）上，两者必有其一非 0。
     */
    private void adjustCommentCount(Comment comment, int delta) {
        String expression = "comment_count = GREATEST(comment_count + (" + delta + "), 0)";
        if (comment.getPoid() != null && comment.getPoid() != 0) {
            postMapper.update(null, new LambdaUpdateWrapper<Post>()
                    .setSql(expression)
                    .eq(Post::getId, comment.getPoid()));
        } else if (comment.getPid() != null && comment.getPid() != 0) {
            petMapper.update(null, new LambdaUpdateWrapper<Pet>()
                    .setSql(expression)
                    .eq(Pet::getId, comment.getPid()));
        }
    }
}
