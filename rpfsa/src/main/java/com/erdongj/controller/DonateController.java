package com.erdongj.controller;

import com.erdongj.common.RequireAdmin;
import com.erdongj.common.RequireLogin;
import com.erdongj.dto.DonateCommitDTO;
import com.erdongj.pojo.Donate;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.ResultBean;
import com.erdongj.service.DonateService;
import com.erdongj.util.TokenUtil;
import com.erdongj.util.SimpleCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @Author: Erdong J
 * @Date: 2023/5/6 21:05
 * @Description: 捐助控制层
 */
@RestController
@RequestMapping("/donate")
public class DonateController extends BaseController {

    @Autowired
    private DonateService donateService;

    @Autowired
    private SimpleCache simpleCache;

    /**
     * 提交捐助。
     * <p>
     * 前端只提交 {@code {petId, items:[{foodId, count}]}}，金额与库存校验全部在服务端完成。
     * 这是替换原"前端串联 4~8 个请求"的关键改造。
     */
    @PostMapping("/commit")
    @RequireLogin
    public ResultBean<Void> commit(@RequestBody @Valid DonateCommitDTO dto,
                                   @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid) {
        donateService.commit(dto, currentUid);
        return successful("捐助成功,感谢您的爱心！");
    }

    /** 管理员手工补录捐助记录（不涉及余额流转） */
    @PostMapping
    @RequireAdmin
    public ResultBean<Void> addonedonate(@RequestBody Donate donate) {
        return donateService.addonedonate(donate) ? successful("捐助成功,感谢您的爱心！") : faile("捐助失败");
    }

    /** 捐助排行榜（游客可访问，60s 缓存） */
    @GetMapping
    public ResultBean<List<Map<String, Object>>> query10donates() {
        List<Map<String, Object>> maps = simpleCache.get("rank:donates", donateService::query10user);
        return maps.isEmpty() ? faile("暂无数据") : successful("查询成功", maps);
    }

    /** 某用户的捐助记录（只能查自己，管理员除外） */
    @GetMapping("/{id}")
    @RequireLogin
    public ResultBean<List<Donate>> queryall(@PathVariable("id") Integer id,
                                             @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                             @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (!TokenUtil.ROLE_ADMIN.equals(role) && !id.equals(currentUid)) {
            return fail(ResultBean.FORBIDDEN, "无权查看他人的捐助记录");
        }
        List<Donate> donateList = donateService.queryalldonatebyid(id);
        if (donateList.isEmpty()) {
            return faile("暂无捐献记录");
        }
        return successful("查询成功", donateList);
    }

    /** 当前用户的未读捐助通知数 */
    @GetMapping("/noread/{uid}")
    @RequireLogin
    public ResultBean<Long> getallnoread(@PathVariable Integer uid,
                                         @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                         @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (!TokenUtil.ROLE_ADMIN.equals(role) && !uid.equals(currentUid)) {
            return fail(ResultBean.FORBIDDEN, "无权查看他人的通知");
        }
        return successful("查询成功", donateService.queryallnoread(uid));
    }

    /** 捐助记录分页（管理端） */
    @GetMapping("/page/{c}")
    @RequireAdmin
    public ResultBean<Pagination<Donate>> queryallbypage(@PathVariable("c") int current) {
        Pagination<Donate> pageInfo = donateService.queryalldonatebypage(current, null);
        if (pageInfo.getTotal() == 0) {
            return faile("暂无捐助记录");
        }
        return successful("查询成功", pageInfo);
    }

    /** 捐助记录条件查询（管理端） */
    @GetMapping("/{c}/{col}/{con}")
    @RequireAdmin
    public ResultBean<Pagination<Donate>> conditionquery(@PathVariable("c") int current,
                                                         @PathVariable("col") String column,
                                                         @PathVariable("con") String condition) {
        Pagination<Donate> pagination = donateService.conditionquery(current, column, condition);
        if (pagination.getTotal() == 0) {
            return faile("无符合结果");
        }
        return successful("查询成功", pagination);
    }

    /** 标记捐助已完成分配（管理端） */
    @PatchMapping
    @RequireAdmin
    public ResultBean<Void> updatestate(@RequestBody Donate donate) {
        return donateService.updatedonatestate(donate) ? successful("通知已发送") : faile("出现错误");
    }

    /** 批量标记本人的捐助通知为已读 */
    @PatchMapping("/{id}")
    @RequireLogin
    public ResultBean<Void> haveread(@PathVariable Integer id,
                                     @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                     @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (!TokenUtil.ROLE_ADMIN.equals(role) && !id.equals(currentUid)) {
            return fail(ResultBean.FORBIDDEN, "无权操作他人的通知");
        }
        donateService.readalldonate(id);
        return successful("所有消息已读成功");
    }
}
