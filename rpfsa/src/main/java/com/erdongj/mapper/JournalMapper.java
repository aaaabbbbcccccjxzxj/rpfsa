package com.erdongj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erdongj.pojo.Journal;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Erdong J
 * @Date: 2023/6/3 15:48
 * @Description:
 */
@Mapper
public interface JournalMapper extends BaseMapper<Journal> {
}
