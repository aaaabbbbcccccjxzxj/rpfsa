package com.erdongj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erdongj.pojo.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 管理员mapper
 * @author: Erdong J
 * @create: 2023-03-08 19:36
 **/

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}
