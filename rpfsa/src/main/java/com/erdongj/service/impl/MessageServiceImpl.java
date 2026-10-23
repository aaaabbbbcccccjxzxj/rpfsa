package com.erdongj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.erdongj.mapper.MessageMapper;
import com.erdongj.pojo.Message;
import com.erdongj.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Erdong J
 * @Date: 2023/5/9 15:28
 * @Description:
 */
@Service
public class MessageServiceImpl implements MessageService {

    /** identity 取值：以"用户"视角收发消息 */
    private static final String IDENTITY_USER = "user";

    @Autowired
    private MessageMapper messageMapper;
    @Override
    public boolean addonemessage(Message message) {
        return messageMapper.insert(message) == 1;
    }

    @Override
    public List<Message> queryallmessagebyid(Integer uid) {
        LambdaQueryWrapper<Message> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Message::getUid,uid).orderByAsc(Message::getCreateTime);
        return messageMapper.selectList(lqw);
    }

    @Override
    public Long queryallnoread(Integer uid, String identity) {
        boolean asUser = IDENTITY_USER.equals(identity);
        LambdaQueryWrapper<Message> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Message::getUid, uid)
                .isNotNull(asUser, Message::getContent)
                .isNotNull(!asUser, Message::getUserReply)
                .eq(Message::getIsRead, Message.UNREAD);
        return messageMapper.selectCount(lqw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean allhaveread(Integer uid, String identity) {
        boolean asUser = IDENTITY_USER.equals(identity);
        // 改造前是"先查出来再逐条 updateById"（N 次 UPDATE），现改为一条批量 UPDATE
        LambdaUpdateWrapper<Message> luw = new LambdaUpdateWrapper<>();
        luw.set(Message::getIsRead, Message.READ)
                .eq(Message::getUid, uid)
                .eq(Message::getIsRead, Message.UNREAD)
                .isNotNull(asUser, Message::getContent)
                .isNotNull(!asUser, Message::getUserReply);
        messageMapper.update(null, luw);
        return true;
    }

    @Override
    public Map<Integer, Long> countNoReadForAdmin(Collection<Integer> uids) {
        Map<Integer, Long> result = new HashMap<>();
        if (uids == null || uids.isEmpty()) {
            return result;
        }
        // 一次聚合查询替代逐个用户 selectCount（消除后端 N+1）
        List<Map<String, Object>> rows = messageMapper.selectMaps(
                new QueryWrapper<Message>()
                        .select("uid", "count(*) as cnt")
                        .in("uid", uids)
                        .isNotNull("user_reply")
                        .eq("is_read", Message.UNREAD)
                        .groupBy("uid"));
        for (Map<String, Object> row : rows) {
            Object uid = row.get("uid");
            Object cnt = row.get("cnt");
            if (uid instanceof Number && cnt instanceof Number) {
                result.put(((Number) uid).intValue(), ((Number) cnt).longValue());
            }
        }
        return result;
    }

    @Override
    public long countNoReadForAdminTotal() {
        Long count = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .isNotNull(Message::getUserReply)
                .eq(Message::getIsRead, Message.UNREAD));
        return count == null ? 0L : count;
    }
}
