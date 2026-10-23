package com.erdongj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erdongj.common.BizException;
import com.erdongj.mapper.CommentMapper;
import com.erdongj.mapper.PostLikeMapper;
import com.erdongj.mapper.PostMapper;
import com.erdongj.mapper.UserMapper;
import com.erdongj.pojo.Comment;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.Post;
import com.erdongj.pojo.PostLike;
import com.erdongj.pojo.ResultBean;
import com.erdongj.pojo.User;
import com.erdongj.service.PostService;
import com.erdongj.util.HtmlSanitizer;
import com.erdongj.util.QueryFieldGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
 * @Date: 2023/4/28 15:35
 * @Description:
 */
@Service
public class PostServiceImpl implements PostService {

    /** 每页条数，前后端统一以此为准（分页元数据由 IPage 回填） */
    private static final int PAGE_SIZE = 12;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostLikeMapper postLikeMapper;

    /**
     * 新增帖子 / 公告。
     * <p>
     * 正文经 HTML 白名单过滤后入库：{@code POST /post} 可被直接调用（绕过 Quill 编辑器），
     * 不过滤则存在 {@code <img onerror=...>} 之类的存储型 XSS 风险。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addonepost(Post post) {
        post.setContent(HtmlSanitizer.clean(post.getContent()));
        return postMapper.insert(post) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatecommentcount(Post post) {
        return postMapper.updateById(post) == 1;
    }

    /**
     * 删除帖子。带归属校验：非管理员只能删除自己发布的帖子。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteonepost(Integer id, Integer operatorUid, boolean isAdmin) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BizException(ResultBean.NOT_FOUND, "帖子不存在");
        }
        if (!isAdmin && !Objects.equals(post.getUid(), operatorUid)) {
            throw new BizException(ResultBean.FORBIDDEN, "只能删除自己发布的帖子");
        }
        return postMapper.deleteById(id) == 1;
    }

    /**
     * 帖子详情 + 两级评论树。
     * <p>
     * 改造前：内层循环里对每条评论重复执行 {@code postMapper.selectById(id)}（同一条帖子被查 N 次），
     * 且逐条查用户。现在固定 4 次查询：帖子、一级评论、二级评论、涉及用户。
     */
    @Override
    public Post queryonepostbyid(Integer id, Integer currentUid) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            return null;
        }
        // 当前用户是否已点赞（游客不查）
        if (currentUid != null) {
            Long liked = postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                    .eq(PostLike::getUid, currentUid)
                    .eq(PostLike::getPoid, id));
            post.setLiked(liked != null && liked > 0);
        }
        if (post.getUid() != null && post.getUid() != 0) {
            post.setUser(userMapper.selectById(post.getUid()));
        }

        List<Comment> topLevel = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPoid, id)
                .eq(Comment::getLevel, Comment.TOP_LEVEL));
        if (topLevel.isEmpty()) {
            post.setComments(Collections.emptyList());
            return post;
        }

        List<Integer> parentIds = topLevel.stream().map(Comment::getId).collect(Collectors.toList());
        List<Comment> children = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .in(Comment::getLevel, parentIds));

        Set<Integer> uids = topLevel.stream().map(Comment::getUid).collect(Collectors.toSet());
        children.forEach(c -> uids.add(c.getUid()));
        uids.remove(null);
        Map<Integer, User> userMap = uids.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(uids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Integer, List<Comment>> childrenMap = children.stream()
                .collect(Collectors.groupingBy(Comment::getLevel));
        for (Comment comment : topLevel) {
            comment.setUser(userMap.get(comment.getUid()));
            comment.setPost(post);
            List<Comment> subComments = childrenMap.getOrDefault(comment.getId(), Collections.emptyList());
            subComments.forEach(sub -> {
                sub.setUser(userMap.get(sub.getUid()));
                sub.setPost(post);
            });
            comment.setCommentList(subComments);
        }
        post.setComments(topLevel);
        return post;
    }

    /**
     * 我的帖子列表。
     * 改造前对每条帖子单独查评论、对每条评论单独查用户（两层 N+1），现改为批量查询后内存装配。
     */
    @Override
    public List<Post> queryallmypostbyid(Integer id) {
        List<Post> postList = postMapper.selectList(
                new LambdaQueryWrapper<Post>().eq(Post::getUid, id));
        if (postList.isEmpty()) {
            return postList;
        }
        List<Integer> postIds = postList.stream().map(Post::getId).collect(Collectors.toList());
        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().in(Comment::getPoid, postIds));
        if (comments.isEmpty()) {
            postList.forEach(post -> post.setComments(Collections.emptyList()));
            return postList;
        }
        Set<Integer> uids = comments.stream().map(Comment::getUid).collect(Collectors.toSet());
        uids.remove(null);
        Map<Integer, User> userMap = uids.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(uids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Integer, List<Comment>> commentsByPost = comments.stream()
                .collect(Collectors.groupingBy(Comment::getPoid));
        postList.forEach(post -> {
            List<Comment> list = commentsByPost.getOrDefault(post.getId(), Collections.emptyList());
            list.forEach(comment -> comment.setUser(userMap.get(comment.getUid())));
            post.setComments(list);
        });
        return postList;
    }

    /**
     * 首页 / 公告列表。classic 为 -1 时查全部（首页一次取回再前端分类）。
     */
    @Override
    public List<Post> queryall(int classic) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("type", Post.TYPE_NOTICE).orderByDesc("create_time")
                .eq(classic == 0, "classic", 0)
                .eq(classic == 1, "classic", 1)
                .eq(classic == 2, "classic", 2);
        return postMapper.selectList(qw);
    }

    /**
     * 帖子分页。type=0 为社区帖（需要带作者与评论），type=1 为公告。
     */
    @Override
    public Pagination<Post> queryallpostsbypage(int current, int type, String column, String condition) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        if (column != null && condition != null && !condition.isEmpty()) {
            QueryFieldGuard.Mode mode = QueryFieldGuard.check("post", column);
            switch (mode) {
                case LIKE:
                    qw.like(column, condition);
                    break;
                case NE:
                    qw.ne(column, condition);
                    break;
                default:
                    qw.eq(column, condition);
            }
        }
        qw.eq("type", type).orderByDesc("create_time");
        IPage<Post> pageInfo = postMapper.selectPage(new Page<>(normalizePage(current), PAGE_SIZE), qw);
        List<Post> postList = pageInfo.getRecords();
        if (type == Post.TYPE_POST) {
            assemble(postList);
        }
        return Pagination.of(pageInfo, postList);
    }

    /**
     * 点赞/取消点赞。三个写操作在同一事务：
     * post_like 记录、post.like_count 原子增减、作者宠宠币 +2（自己的帖子不奖励）。
     * 重复点赞由 (uid, poid) 唯一约束兜底。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Integer uid, Integer poid) {
        Post post = postMapper.selectById(poid);
        if (post == null) {
            throw new BizException(ResultBean.NOT_FOUND, "帖子不存在");
        }
        Long exists = postLikeMapper.selectCount(new LambdaQueryWrapper<PostLike>()
                .eq(PostLike::getUid, uid)
                .eq(PostLike::getPoid, poid));
        boolean liked = exists != null && exists > 0;
        if (liked) {
            postLikeMapper.delete(new LambdaQueryWrapper<PostLike>()
                    .eq(PostLike::getUid, uid)
                    .eq(PostLike::getPoid, poid));
            postMapper.update(null, new LambdaUpdateWrapper<Post>()
                    .setSql("like_count = like_count - 1")
                    .eq(Post::getId, poid)
                    .gt(Post::getLikeCount, 0));
            return false;
        }
        try {
            postLikeMapper.insert(new PostLike(uid, poid));
        } catch (DuplicateKeyException e) {
            // 并发双击：唯一约束拦截，视为已点赞
            return true;
        }
        postMapper.update(null, new LambdaUpdateWrapper<Post>()
                .setSql("like_count = like_count + 1")
                .eq(Post::getId, poid));
        // 作者奖励 +2 宠宠币（管理员帖子 uid=0、自己的帖子不加）
        if (post.getUid() != null && post.getUid() != 0 && !Objects.equals(post.getUid(), uid)) {
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .setSql("pet_coin = pet_coin + 2")
                    .eq(User::getId, post.getUid()));
        }
        return true;
    }

    /**
     * 热门帖子榜（按评论数）。只取榜单需要的列，不带富文本正文与评论树，
     * 响应体从原先的数百 KB 收敛到几 KB。
     */
    @Override
    public List<Post> query10posts() {
        List<Post> postList = postMapper.selectList(new LambdaQueryWrapper<Post>()
                .select(Post::getId, Post::getTitle, Post::getUid, Post::getClassic,
                        Post::getCommentCount, Post::getLikeCount, Post::getCreateTime)
                .ne(Post::getUid, 0)
                .orderByDesc(Post::getCommentCount)
                .last("limit 10"));
        // 只装配作者昵称/用户名，不带评论
        Set<Integer> authorIds = postList.stream()
                .map(Post::getUid)
                .filter(uid -> uid != null && uid != 0)
                .collect(Collectors.toSet());
        Map<Integer, User> authorMap = authorIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        postList.forEach(post -> post.setUser(authorMap.get(post.getUid())));
        return postList;
    }

    /**
     * 批量为帖子装配作者与评论。
     * 改造前是"每帖一次用户查询 + 每帖一次评论查询 + 每条评论再查一次帖子"，
     * 现固定为 4 次查询（帖子由调用方已有）。
     */
    private void assemble(List<Post> postList) {
        if (postList == null || postList.isEmpty()) {
            return;
        }
        Set<Integer> authorIds = postList.stream()
                .map(Post::getUid)
                .filter(uid -> uid != null && uid != 0)
                .collect(Collectors.toSet());
        Map<Integer, User> authorMap = authorIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        postList.forEach(post -> post.setUser(authorMap.get(post.getUid())));

        List<Integer> postIds = postList.stream().map(Post::getId).collect(Collectors.toList());
        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().in(Comment::getPoid, postIds));
        if (comments.isEmpty()) {
            postList.forEach(post -> post.setComments(Collections.emptyList()));
            return;
        }
        Set<Integer> commenterIds = comments.stream().map(Comment::getUid).collect(Collectors.toSet());
        commenterIds.remove(null);
        Map<Integer, User> commenterMap = commenterIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(commenterIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Integer, List<Comment>> commentsByPost = comments.stream()
                .collect(Collectors.groupingBy(Comment::getPoid));
        postList.forEach(post -> {
            List<Comment> list = commentsByPost.getOrDefault(post.getId(), Collections.emptyList());
            list.forEach(comment -> {
                comment.setUser(commenterMap.get(comment.getUid()));
                comment.setPost(post);
            });
            post.setComments(list);
        });
    }

    private int normalizePage(int current) {
        return current < 1 ? 1 : current;
    }
}
