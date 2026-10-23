package com.erdongj.controller;

import com.erdongj.common.RequireAdmin;
import com.erdongj.common.RequireLogin;
import com.erdongj.dto.AdoptAuditDTO;
import com.erdongj.pojo.Adopt;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.ResultBean;
import com.erdongj.service.AdoptService;
import com.erdongj.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Author: Erdong J
 * @Date: 2023/4/23 11:15
 * @Description: 领养申请控制层
 */

@RestController
@RequestMapping("/adopt")
public class AdoptController extends BaseController {

    @Autowired
    private AdoptService adoptService;

    /**
     * 提交领养申请。
     * uid 以 token 为准；宠物状态流转已合并进该事务，前端无需再调 PATCH /pet。
     */
    @PostMapping
    @RequireLogin
    public ResultBean<Void> createadopttable(@RequestBody Adopt adopt,
                                             @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid) {
        adopt.setUid(currentUid);
        return adoptService.addadopttable(adopt) ? successful("申请成功！请注意查看通知") : faile("申请失败");
    }

    /** 领养申请分页（管理端） */
    @GetMapping("/{c}")
    @RequireAdmin
    public ResultBean<Pagination<Adopt>> getadoptsbypage(@PathVariable("c") int current) {
        Pagination<Adopt> pageInfo = adoptService.queryadoptsbypage(current, null);
        if (pageInfo.getTotal() == 0) {
            return faile("暂无申请信息");
        }
        return successful("查询成功", pageInfo);
    }

    /** 领养申请条件查询（管理端） */
    @GetMapping("/{c}/{col}/{con}")
    @RequireAdmin
    public ResultBean<Pagination<Adopt>> conditionquery(@PathVariable("c") int current,
                                                        @PathVariable("col") String column,
                                                        @PathVariable("con") String condition) {
        Pagination<Adopt> pagination = adoptService.conditionquery(current, column, condition);
        if (pagination.getTotal() == 0) {
            return faile("无查询结果");
        }
        return successful("查询成功", pagination);
    }

    /** 当前用户未读的领养通知数 */
    @GetMapping("/noread/{uid}")
    @RequireLogin
    public ResultBean<Long> getallnoread(@PathVariable Integer uid,
                                         @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                         @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (!TokenUtil.ROLE_ADMIN.equals(role) && !uid.equals(currentUid)) {
            return fail(ResultBean.FORBIDDEN, "无权查看他人的通知");
        }
        return successful("查询成功", adoptService.queryallonread(uid));
    }

    /**
     * 审核领养申请（管理端）。
     * 单个事务内完成"改申请 + 改宠物 + 累加领养数 + 站内通知"，不再由前端串联多个请求。
     */
    @PatchMapping
    @RequireAdmin
    public ResultBean<Void> audit(@RequestBody @Valid AdoptAuditDTO dto) {
        return adoptService.audit(dto) ? successful("通知已发送") : faile("出现错误");
    }

    /** 用户取消自己的领养申请（仅审核中可取消） */
    @DeleteMapping("/cancel/{id}")
    @RequireLogin
    public ResultBean<Void> cancel(@PathVariable Integer id,
                                   @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid) {
        adoptService.cancel(id, currentUid);
        return successful("申请已取消");
    }

    /** 批量标记本人的领养通知为已读 */
    @PatchMapping("/{uid}")
    @RequireLogin
    public ResultBean<Void> readall(@PathVariable Integer uid,
                                    @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                    @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (!TokenUtil.ROLE_ADMIN.equals(role) && !uid.equals(currentUid)) {
            return fail(ResultBean.FORBIDDEN, "无权操作他人的通知");
        }
        return adoptService.readalladopt(uid) ? successful("消息已读") : faile("出现错误");
    }
}
