package com.erdongj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erdongj.pojo.Pet;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Erdong J
 * @Date: 2023/4/9 17:08
 * @Description:
 */
@Mapper
public interface PetMapper extends BaseMapper<Pet> {
}
