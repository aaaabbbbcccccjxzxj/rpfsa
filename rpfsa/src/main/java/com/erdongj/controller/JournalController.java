package com.erdongj.controller;

import com.erdongj.common.RequireLogin;
import com.erdongj.pojo.Journal;
import com.erdongj.pojo.ResultBean;
import com.erdongj.service.JournalService;
import com.erdongj.util.HtmlSanitizer;
import com.erdongj.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: Erdong J
 * @Date: 2023/6/3 16:05
 * @Description: 领养日记控制层
 */
@RestController
@RequestMapping("/journal")
public class JournalController extends BaseController {

    @Autowired
    private JournalService journalService;

    /**
     * 新增领养动态。uid 以 token 为准，正文走 HTML 白名单过滤。
     */
    @PostMapping
    @RequireLogin
    public ResultBean<Void> addonejournal(@RequestBody Journal journal,
                                          @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid) {
        journal.setUid(currentUid);
        journal.setDiary(HtmlSanitizer.clean(journal.getDiary()));
        journal.setTitle(HtmlSanitizer.clean(journal.getTitle()));
        return journalService.addonejournal(journal) ? successful("添加成功") : faile("添加失败");
    }

    /** 删除领养动态（管理端） */
    @DeleteMapping("/{id}")
    @RequireLogin
    public ResultBean<Void> deleteonejournal(@PathVariable int id) {
        return journalService.deleteonejournal(id) ? successful("删除成功") : faile("删除失败");
    }

    /** 某只宠物的领养动态（游客可访问） */
    @GetMapping("/{pid}")
    public ResultBean<List<Journal>> queryalljournal(@PathVariable int pid) {
        List<Journal> list = journalService.queryonepetbyid(pid);
        if (list.isEmpty()) {
            return faile("暂无动态记录");
        }
        return successful("查询成功", list);
    }
}
