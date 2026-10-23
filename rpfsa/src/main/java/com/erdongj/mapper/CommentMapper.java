package com.erdongj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erdongj.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

/**
 * @Author: Erdong J
 * @Date: 2023/4/28 15:26
 * @Description:
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
