package com.erdongj.service;

import java.util.Map;

/**
 * @description: 管理员服务层
 * @author: Erdong J
 * @create: 2023-03-08 21:21
 **/
public interface AdminService {

    /**
     * 管理员登录验证
     *
     * @param name
     * @param password
     * @return
     */
    boolean Login(String name, String password);

    /**
     * 管理员首页信息详情
     *
     * @return
     */
    Map<String, Long> getallinfo();

    /**
     * 近 6 个月运营趋势：每月领养通过数、捐助总额（宠宠币）、新增用户数。
     *
     * @return {months:[...], adoptCounts:[...], donateCoins:[...], userCounts:[...]}
     */
    Map<String, Object> getTrends();

    /** 导出全部领养记录（CSV 行数据，含表头） */
    java.util.List<java.util.List<String>> exportAdoptRows();

    /** 导出全部捐助记录（CSV 行数据，含表头） */
    java.util.List<java.util.List<String>> exportDonateRows();
}
