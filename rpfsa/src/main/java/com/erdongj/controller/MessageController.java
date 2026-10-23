package com.erdongj.controller;

import com.erdongj.common.RequireLogin;
import com.erdongj.pojo.Message;
import com.erdongj.pojo.ResultBean;
import com.erdongj.service.MessageService;
import com.erdongj.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: Erdong J
 * @Date: 2023/5/9 15:30
 * @Description:
 */

@RestController
@RequestMapping("/message")
public class MessageController extends BaseController {

    @Autowired
    private MessageService messageService;

    /**
     * 发送消息（用户咨询 / 管理员回复）。
     * <p>
     * 普通用户只能以自己的身份发咨询（uid 强制为当前用户、内容只能走 userReply 字段）；
     * 管理员回复时可以指定目标用户，但只能写 content 字段。
     * 这样两个方向的消息不会互相冒充。
     */
    @PostMapping
    @RequireLogin
    public ResultBean<Void> addonemessage(@RequestBody Message message,
                                          @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                          @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (TokenUtil.ROLE_ADMIN.equals(role)) {
            message.setUserReply(null);
        } else {
            message.setUid(currentUid);
            message.setContent(null);
        }
        message.setId(null);
        message.setIsRead(Message.UNREAD);
        return messageService.addonemessage(message) ? successful("发送成功") : faile("发送失败");
    }

    /** 某用户的完整会话记录 */
    @GetMapping("/{uid}")
    @RequireLogin
    public ResultBean<List<Message>> getallmessagesbyid(@PathVariable Integer uid,
                                                        @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                                        @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (!TokenUtil.ROLE_ADMIN.equals(role) && !uid.equals(currentUid)) {
            return fail(ResultBean.FORBIDDEN, "无权查看他人会话");
        }
        return successful("查询成功", messageService.queryallmessagebyid(uid));
    }

    /** 未读消息数 */
    @GetMapping("/noread/{uid}/{identity}")
    @RequireLogin
    public ResultBean<Long> getnoreadbyid(@PathVariable Integer uid,
                                          @PathVariable String identity,
                                          @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                          @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (!TokenUtil.ROLE_ADMIN.equals(role) && !uid.equals(currentUid)) {
            return fail(ResultBean.FORBIDDEN, "无权查看他人的未读消息");
        }
        return successful("查询成功", messageService.queryallnoread(uid, identity));
    }

    /** 全部标记已读 */
    @PatchMapping("/{uid}/{identity}")
    @RequireLogin
    public ResultBean<Void> readallmessage(@PathVariable Integer uid,
                                           @PathVariable String identity,
                                           @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                           @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (!TokenUtil.ROLE_ADMIN.equals(role) && !uid.equals(currentUid)) {
            return fail(ResultBean.FORBIDDEN, "无权操作他人的消息");
        }
        messageService.allhaveread(uid, identity);
        return successful("已读所有消息");
    }
}
