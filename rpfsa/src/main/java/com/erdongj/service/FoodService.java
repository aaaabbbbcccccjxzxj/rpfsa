package com.erdongj.service;

import com.erdongj.pojo.Food;

import java.util.List;

/**
 * @Author: Erdong J
 * @Date: 2023/5/6 08:49
 * @Description:
 */
public interface FoodService {
    /**
     * 拿到所有的foods
     * @return
     */
    List<Food> queryallfoods();

    /**
     * 删除一个food
     * @param id
     * @return
     */
    boolean deleteonefood(Integer id);

    /**
     * 添加一个food
     * @param food
     * @return
     */
    boolean addonefood(Food food);

    /**
     * food的修改
     * @param food
     * @return
     */
    boolean updatefood(Food food);

}
