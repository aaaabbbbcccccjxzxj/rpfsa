package com.erdongj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erdongj.pojo.Post;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Erdong J
 * @Date: 2023/4/28 15:25
 * @Description:
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
