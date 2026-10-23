package com.erdongj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.erdongj.dto.DonateCommitDTO;
import com.erdongj.pojo.Donate;
import com.erdongj.pojo.Pagination;

import java.util.List;
import java.util.Map;

/**
 * @Author: Erdong J
 * @Date: 2023/5/6 21:03
 * @Description:
 */
public interface DonateService {

    /**
     * 添加一条捐助记录
     * @param donate
     * @return
     */
    boolean addonedonate(Donate donate);

    /**
     * 完成一次捐助。
     * <p>
     * 服务端重新计价、扣余额、扣库存、给宠物加币、写入捐助记录，全部在单个事务内完成。
     * 前端只提交"救助哪只宠物、选了哪些救助品各几件"，不提交金额。
     *
     * @param dto 捐助请求
     * @param uid 捐助人 id（取自 token，不接受前端传入）
     */
    void commit(DonateCommitDTO dto, Integer uid);

    /**
     * 根据用户id查询该用户所有捐助记录
     * @param id
     * @return
     */
    List<Donate> queryalldonatebyid(Integer id);

    /**
     * 查询捐助次数与总额前十名的用户
     * @return
     */
    List<Map<String, Object>> query10user();

    /**
     * 可按条件分页查询
     * @param current
     * @param qw
     * @return
     */
    Pagination<Donate> queryalldonatebypage(int current, QueryWrapper<Donate> qw);

    /**
     * 创建条件
     * @param current
     * @param column
     * @param condition
     * @return
     */
    Pagination<Donate> conditionquery(int current, String column, String condition);

    /**
     * 管理员更新捐助品已分配状态
     * @param donate
     * @return
     */
    boolean updatedonatestate(Donate donate);

    /**
     * 用户读取所有未读的捐助通知。
     * （方法名原先误写为 readalladopt，从领养模块复制而来，此处一并修正）
     *
     * @param uid 用户 id
     */
    boolean readalldonate(Integer uid);

    /**
     * 用户拿到未读通知的Count数
     * @param uid
     * @return
     */
    Long queryallnoread(Integer uid);

}
