package com.erdongj.service;

import com.erdongj.pojo.Journal;

import java.util.List;

/**
 * @Author: Erdong J
 * @Date: 2023/6/3 15:49
 * @Description:
 */
public interface JournalService {

    /**
     * 用户添加一条动态
     * @param journal
     * @return
     */
    boolean addonejournal(Journal journal);

    /**
     * 用户删除一条动态
     * @param id
     * @return
     */
    boolean deleteonejournal(int id);

    /**
     * 根据petid查询该pet所有动态
     * @param pid
     * @return
     */
    List<Journal> queryonepetbyid(int pid);
}
