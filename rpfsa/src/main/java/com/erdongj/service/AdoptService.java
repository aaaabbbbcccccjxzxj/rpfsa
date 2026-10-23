package com.erdongj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.erdongj.dto.AdoptAuditDTO;
import com.erdongj.pojo.Adopt;
import com.erdongj.pojo.Pagination;

/**
 * @Author: Erdong J
 * @Date: 2023/4/23 09:48
 * @Description:
 */
public interface AdoptService {


    /**
     * 可按条件分页查询
     *
     * @param current
     * @param qw
     * @return
     */
    Pagination<Adopt> queryadoptsbypage(int current, QueryWrapper<Adopt> qw);

    /**
     * 产生条件查询,调用分页
     *
     * @param current
     * @param column
     * @param condition
     * @return
     */
    Pagination<Adopt> conditionquery(int current, String column, String condition);

    /**
     * 添加一张领养表
     *
     * @param adopt
     * @return
     */
    boolean addadopttable(Adopt adopt);

    /**
     * 用户取消自己的领养申请。
     * 仅"审核中"状态可取消；宠物状态在同一事务内退回"流浪中"。
     *
     * @param id  申请记录 id
     * @param uid 当前登录用户 id（只能取消本人的申请）
     */
    void cancel(Integer id, Integer uid);

    /**
     * 删除一张表
     *
     * @param id 领养表id
     * @return
     */
    boolean deleteone(Integer id);

    /**
     * 审核领养申请。
     * <p>
     * 在单个事务内完成三件事：写入审核结果与通知、流转宠物领养状态、累加用户领养数。
     * 替代原先由管理端前端串行发起的 3 个独立请求。
     *
     * @param dto 审核请求（申请 id、是否通过、通知内容）
     * @return 是否审核成功
     */
    boolean audit(AdoptAuditDTO dto);

    /**
     * 用户已读所有领养申请通知
     *
     * @return
     */
    boolean readalladopt(Integer uid);

    /**
     * 用户查询所有领养申请未读通知
     *
     * @param uid
     * @return
     */
    Long queryallonread(Integer uid);
}
