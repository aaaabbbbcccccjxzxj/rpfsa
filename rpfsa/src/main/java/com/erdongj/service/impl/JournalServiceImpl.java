package com.erdongj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.erdongj.mapper.JournalMapper;
import com.erdongj.mapper.PetMapper;
import com.erdongj.mapper.UserMapper;
import com.erdongj.pojo.Journal;
import com.erdongj.pojo.Pet;
import com.erdongj.pojo.User;
import com.erdongj.service.JournalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: Erdong J
 * @Date: 2023/6/3 15:55
 * @Description:
 */
@Service
public class JournalServiceImpl implements JournalService {

    /** 领养人发布回访日记的宠宠币奖励 */
    private static final int DIARY_REWARD_COIN = 5;

    @Autowired
    private JournalMapper journalMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PetMapper petMapper;

    /**
     * 新增领养动态。
     * 若发布者就是该宠物的领养人（pet.uid = 发布者且已领养成功），
     * 视为"回访打卡"，在同一事务内奖励 {@value #DIARY_REWARD_COIN} 宠宠币。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addonejournal(Journal journal) {
        if (journalMapper.insert(journal) != 1) {
            return false;
        }
        Pet pet = petMapper.selectById(journal.getPid());
        boolean isOwnerVisitBack = pet != null
                && Objects.equals(pet.getUid(), journal.getUid())
                && Objects.equals(pet.getIsAdopt(), Pet.ADOPT_DONE);
        if (isOwnerVisitBack) {
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .setSql("pet_coin = pet_coin + " + DIARY_REWARD_COIN)
                    .eq(User::getId, journal.getUid()));
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteonejournal(int id) {
        return journalMapper.deleteById(id) == 1;
    }

    /**
     * 某只宠物的领养动态。
     * <p>
     * 改造前对每条动态分别查一次 user、查一次 pet（N+1）；由于 pid 固定，
     * 宠物其实只需要查一次，用户按 uid 去重后批量查。现固定为 2 次查询。
     */
    @Override
    public List<Journal> queryonepetbyid(int pid) {
        List<Journal> journalList = journalMapper.selectList(new LambdaQueryWrapper<Journal>()
                .eq(Journal::getPid, pid)
                .orderByDesc(Journal::getCreateTime));
        if (journalList.isEmpty()) {
            return journalList;
        }
        Pet pet = petMapper.selectById(pid);
        Set<Integer> uids = journalList.stream()
                .map(Journal::getUid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, User> userMap = uids.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(uids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        journalList.forEach(journal -> {
            journal.setUser(userMap.get(journal.getUid()));
            journal.setPet(pet);
        });
        return journalList;
    }
}
