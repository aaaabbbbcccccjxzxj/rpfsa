package com.erdongj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erdongj.pojo.Food;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Erdong J
 * @Date: 2023/5/6 08:48
 * @Description:
 */
@Mapper
public interface FoodMapper extends BaseMapper<Food> {
}
