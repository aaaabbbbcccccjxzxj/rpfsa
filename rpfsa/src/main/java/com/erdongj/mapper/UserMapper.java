package com.erdongj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erdongj.pojo.Pet;
import com.erdongj.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @description: 用户接口
 * @author: Erdong J
 * @create: 2023-03-09 10:46
 **/
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户id查询该用户所领养的宠物
     * @param id
     * @return
     */

}
