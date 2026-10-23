package com.erdongj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erdongj.common.BizException;
import com.erdongj.mapper.CommentMapper;
import com.erdongj.mapper.PetMapper;
import com.erdongj.mapper.PetPhotoMapper;
import com.erdongj.mapper.UserMapper;
import com.erdongj.pojo.Comment;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.Pet;
import com.erdongj.pojo.PetPhoto;
import com.erdongj.pojo.ResultBean;
import com.erdongj.pojo.User;
import com.erdongj.service.PetService;
import com.erdongj.util.QueryFieldGuard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: Erdong J
 * @Date: 2023/4/9 17:13
 * @Description:
 */
@Service
public class PetServiceImpl implements PetService {

    /** 前端宠物列表每页条数 */
    private static final int PAGE_SIZE = 12;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PetPhotoMapper petPhotoMapper;

    /**
     * 宠物前10排行榜（按收到的宠宠币排序）。
     * 只取榜单需要的列，不带描述与评论树。
     */
    @Override
    public List<Pet> queryallpets() {
        return petMapper.selectList(new LambdaQueryWrapper<Pet>()
                .select(Pet::getId, Pet::getPetname, Pet::getPhoto, Pet::getPetCoin,
                        Pet::getClassic, Pet::getIsAdopt)
                .orderByDesc(Pet::getPetCoin)
                .last("limit 10"));
    }

    /**
     * 查看用户相关的宠物：自己上传的 + 自己领养成功的。
     * <p>
     * 改造前写的是 {@code eq(uid).eq(type,1).or().eq(uid).eq(isAdopt,1)}，
     * 依赖 OR 的优先级恰好符合预期，语义含糊；现用 and/or 显式分组表达。
     */
    @Override
    public List<Pet> querypetbyuid(Integer uid) {
        return petMapper.selectList(new LambdaQueryWrapper<Pet>()
                .and(w -> w.eq(Pet::getUid, uid).eq(Pet::getType, Pet.TYPE_PRIVATE))
                .or(w -> w.eq(Pet::getUid, uid).eq(Pet::getIsAdopt, Pet.ADOPT_DONE)));
    }

    /**
     * 宠物详情 + 两级评论树。
     * <p>
     * 改造前：对每条一级评论查一次二级评论、再对每条二级评论查一次用户，
     * 400 条评论会产生 400+ 次查询。现在固定为 4 次查询：
     * 宠物 1 次、一级评论 1 次、二级评论 1 次、涉及用户 1 次，
     * 再在内存里用 Map 完成装配。
     */
    @Override
    public Pet queryonebyID(Integer id) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            return null;
        }
        // 装配相册（按排序号升序）
        pet.setPhotos(petPhotoMapper.selectList(new LambdaQueryWrapper<PetPhoto>()
                .eq(PetPhoto::getPid, id)
                .orderByAsc(PetPhoto::getSort)
                .orderByAsc(PetPhoto::getId)));
        List<Comment> topLevel = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPid, id)
                .eq(Comment::getLevel, Comment.TOP_LEVEL));
        if (topLevel.isEmpty()) {
            pet.setComments(Collections.emptyList());
            return pet;
        }

        List<Integer> parentIds = topLevel.stream().map(Comment::getId).collect(Collectors.toList());
        List<Comment> children = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .in(Comment::getLevel, parentIds));

        // 一次性取出评论者与宠物主人，避免逐条 selectById
        Set<Integer> uids = new HashSet<>();
        topLevel.forEach(c -> uids.add(c.getUid()));
        children.forEach(c -> uids.add(c.getUid()));
        boolean privatePet = Objects.equals(pet.getType(), Pet.TYPE_PRIVATE);
        if (privatePet && pet.getUid() != null) {
            uids.add(pet.getUid());
        }
        uids.remove(null);
        Map<Integer, User> userMap = uids.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(uids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Integer, List<Comment>> childrenMap = children.stream()
                .collect(Collectors.groupingBy(Comment::getLevel));
        for (Comment comment : topLevel) {
            comment.setUser(userMap.get(comment.getUid()));
            List<Comment> subComments = childrenMap.getOrDefault(comment.getId(), Collections.emptyList());
            subComments.forEach(sub -> sub.setUser(userMap.get(sub.getUid())));
            comment.setCommentList(subComments);
        }
        if (privatePet) {
            pet.setUser(userMap.get(pet.getUid()));
        }
        pet.setComments(topLevel);
        return pet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteonebyID(Integer id) {
        return petMapper.deleteById(id) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addonepet(Pet pet) {
        return petMapper.insert(pet) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateonepet(Pet pet) {
        return petMapper.updateById(pet) == 1;
    }

    /**
     * 宠物分页。
     * <p>
     * 两点优化：① 私人上传的宠物改为批量取主人，而非逐行 selectOne；
     * ② 分页元数据 size 由 IPage 给出，修正原先"实际 12 条却上报 10"的错误。
     */
    @Override
    public Pagination<Pet> querypetsbypage(int current, QueryWrapper<Pet> qw) {
        IPage<Pet> pageInfo = petMapper.selectPage(new Page<>(normalizePage(current), PAGE_SIZE), qw);
        List<Pet> pets = pageInfo.getRecords();
        fillOwner(pets);
        return Pagination.of(pageInfo, pets);
    }

    @Override
    public Pagination<Pet> conditionquery(int current, String column, String condition) {
        QueryFieldGuard.Mode mode = QueryFieldGuard.check("pet", column);
        QueryWrapper<Pet> qw = new QueryWrapper<>();
        if (mode == QueryFieldGuard.Mode.LIKE) {
            qw.like(column, condition);
        } else {
            qw.eq(column, condition);
        }
        return querypetsbypage(current, qw);
    }

    /**
     * 多条件组合筛选。所有条件来自白名单化的 QueryWrapper 组装，不存在拼 SQL 注入面。
     */
    @Override
    public Pagination<Pet> filter(int current, Integer classic, Integer type, Integer sex,
                                  Integer minAge, Integer maxAge, Integer isVaccine, Integer isAdopt,
                                  String keyword) {
        QueryWrapper<Pet> qw = new QueryWrapper<>();
        qw.eq(classic != null, "classic", classic)
                .eq(type != null, "type", type)
                .eq(sex != null, "sex", sex)
                .eq(isVaccine != null, "is_vaccine", isVaccine)
                .eq(isAdopt != null, "is_adopt", isAdopt)
                .ge(minAge != null, "age", minAge)
                .le(maxAge != null, "age", maxAge);
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            qw.and(w -> w.like("petname", kw).or().like("variety", kw));
        }
        qw.orderByDesc("create_time");
        return querypetsbypage(current, qw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPhoto(Integer pid, String url) {
        if (petMapper.selectById(pid) == null) {
            throw new BizException(ResultBean.NOT_FOUND, "宠物不存在");
        }
        PetPhoto photo = new PetPhoto();
        photo.setPid(pid);
        photo.setUrl(url);
        photo.setSort(0);
        return petPhotoMapper.insert(photo) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePhoto(Integer photoId) {
        return petPhotoMapper.deleteById(photoId) == 1;
    }

    /** 批量为"私人上传"的宠物装配主人信息 */
    private void fillOwner(List<Pet> pets) {
        if (pets == null || pets.isEmpty()) {
            return;
        }
        Set<Integer> uids = pets.stream()
                .filter(p -> Objects.equals(p.getType(), Pet.TYPE_PRIVATE))
                .map(Pet::getUid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (uids.isEmpty()) {
            return;
        }
        Map<Integer, User> userMap = userMapper.selectBatchIds(uids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        pets.forEach(pet -> {
            if (Objects.equals(pet.getType(), Pet.TYPE_PRIVATE)) {
                pet.setUser(userMap.get(pet.getUid()));
            }
        });
    }

    private int normalizePage(int current) {
        return current < 1 ? 1 : current;
    }
}
