package com.erdongj.controller;

import com.erdongj.common.RequireLogin;
import com.erdongj.pojo.Favorite;
import com.erdongj.pojo.ResultBean;
import com.erdongj.service.FavoriteService;
import com.erdongj.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宠物收藏控制层。全部接口需要登录。
 */
@RestController
@RequestMapping("/favorite")
@RequireLogin
public class FavoriteController extends BaseController {

    @Autowired
    private FavoriteService favoriteService;

    /** 收藏/取消收藏 */
    @PostMapping("/{pid}")
    public ResultBean<Boolean> toggle(@PathVariable Integer pid,
                                      @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer uid) {
        boolean favored = favoriteService.toggle(uid, pid);
        return successful(favored ? "收藏成功" : "已取消收藏", favored);
    }

    /** 我的收藏列表 */
    @GetMapping("/my")
    public ResultBean<List<Favorite>> myFavorites(@RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer uid) {
        List<Favorite> favorites = favoriteService.myFavorites(uid);
        if (favorites.isEmpty()) {
            return faile("暂无收藏");
        }
        return successful("查询成功", favorites);
    }

    /** 是否已收藏某只宠物 */
    @GetMapping("/check/{pid}")
    public ResultBean<Boolean> check(@PathVariable Integer pid,
                                     @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer uid) {
        return successful("查询成功", favoriteService.checked(uid, pid));
    }
}
