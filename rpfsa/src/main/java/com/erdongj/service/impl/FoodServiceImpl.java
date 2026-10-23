package com.erdongj.service.impl;

import com.erdongj.mapper.FoodMapper;
import com.erdongj.pojo.Food;
import com.erdongj.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: Erdong J
 * @Date: 2023/5/6 08:59
 * @Description:
 */
@Service
public class FoodServiceImpl implements FoodService {

    @Autowired
    private FoodMapper foodMapper;


    @Override
    public List<Food> queryallfoods() {
        return foodMapper.selectList(null);
    }

    @Override
    @Transactional
    public boolean deleteonefood(Integer id) {
        return foodMapper.deleteById(id) == 1;
    }

    @Override
    @Transactional
    public boolean addonefood(Food food) {
        return foodMapper.insert(food) == 1;
    }

    @Override
    @Transactional
    public boolean updatefood(Food food) {
        return foodMapper.updateById(food) == 1;
    }
}
