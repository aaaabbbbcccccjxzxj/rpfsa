package com.erdongj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erdongj.pojo.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Erdong J
 * @Date: 2023/5/9 15:25
 * @Description:
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
