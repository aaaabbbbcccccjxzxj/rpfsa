package com.erdongj.service;

import com.erdongj.pojo.Favorite;

import java.util.List;

/**
 * 宠物收藏。
 */
public interface FavoriteService {

    /**
     * 收藏/取消收藏（toggle）。
     *
     * @return true 表示本次操作后为"已收藏"，false 表示已取消
     */
    boolean toggle(Integer uid, Integer pid);

    /** 我的收藏列表（装配宠物信息，时间倒序） */
    List<Favorite> myFavorites(Integer uid);

    /** 是否已收藏某只宠物 */
    boolean checked(Integer uid, Integer pid);
}
