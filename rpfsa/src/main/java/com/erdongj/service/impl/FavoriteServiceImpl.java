package com.erdongj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erdongj.common.BizException;
import com.erdongj.mapper.FavoriteMapper;
import com.erdongj.mapper.PetMapper;
import com.erdongj.pojo.Favorite;
import com.erdongj.pojo.Pet;
import com.erdongj.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 宠物收藏业务层。唯一约束 (uid, pid) 兜底并发重复收藏。
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private PetMapper petMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Integer uid, Integer pid) {
        Pet pet = petMapper.selectById(pid);
        if (pet == null) {
            throw new BizException("宠物不存在");
        }
        Long exists = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUid, uid)
                .eq(Favorite::getPid, pid));
        if (exists != null && exists > 0) {
            favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getUid, uid)
                    .eq(Favorite::getPid, pid));
            return false;
        }
        try {
            favoriteMapper.insert(new Favorite(uid, pid));
        } catch (DuplicateKeyException e) {
            // 并发双击：唯一约束拦截，视为已收藏
            return true;
        }
        return true;
    }

    @Override
    public List<Favorite> myFavorites(Integer uid) {
        List<Favorite> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUid, uid)
                .orderByDesc(Favorite::getCreateTime));
        if (favorites.isEmpty()) {
            return favorites;
        }
        Set<Integer> pids = favorites.stream().map(Favorite::getPid).collect(Collectors.toSet());
        Map<Integer, Pet> petMap = petMapper.selectBatchIds(pids).stream()
                .collect(Collectors.toMap(Pet::getId, Function.identity()));
        // 宠物可能已被删除，收藏记录保留但 pet 为 null，前端做兜底展示
        favorites.forEach(fav -> fav.setPet(petMap.get(fav.getPid())));
        favorites.removeIf(fav -> Objects.isNull(fav.getPet()));
        return favorites;
    }

    @Override
    public boolean checked(Integer uid, Integer pid) {
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUid, uid)
                .eq(Favorite::getPid, pid));
        return count != null && count > 0;
    }
}
