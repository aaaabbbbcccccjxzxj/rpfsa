package com.erdongj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erdongj.common.BizException;
import com.erdongj.dto.AdoptAuditDTO;
import com.erdongj.mapper.AdoptMapper;
import com.erdongj.mapper.PetMapper;
import com.erdongj.mapper.UserMapper;
import com.erdongj.pojo.Adopt;
import com.erdongj.pojo.Message;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.Pet;
import com.erdongj.pojo.ResultBean;
import com.erdongj.pojo.User;
import com.erdongj.service.AdoptService;
import com.erdongj.service.MessageService;
import com.erdongj.util.QueryFieldGuard;
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
 * @Date: 2023/4/23 09:54
 * @Description:
 */
@Service
public class AdoptServiceImpl implements AdoptService {

    /** 管理端领养申请列表每页条数 */
    private static final int PAGE_SIZE = 10;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PetMapper petMapper;
    @Autowired
    private AdoptMapper adoptMapper;
    @Autowired
    private MessageService messageService;

    /**
     * 领养申请分页。
     * <p>
     * 改造前对每行单独查用户、单独查宠物（N+1），且上报的 size=12 与实际每页 10 条不符；
     * 现改为两次批量查询 + 内存装配，size 由 IPage 回填。
     */
    @Override
    public Pagination<Adopt> queryadoptsbypage(int current, QueryWrapper<Adopt> qw) {
        IPage<Adopt> pageInfo = adoptMapper.selectPage(new Page<>(normalizePage(current), PAGE_SIZE), qw);
        List<Adopt> adoptList = pageInfo.getRecords();
        assemble(adoptList);
        return Pagination.of(pageInfo, adoptList);
    }

    @Override
    public Pagination<Adopt> conditionquery(int current, String column, String condition) {
        // 原写法把"是否加条件"藏进了 like(boolean, column, value) 的布尔参数里，
        // 且 col 未做白名单校验。现由守卫统一校验并给出匹配模式。
        QueryFieldGuard.Mode mode = QueryFieldGuard.check("adopt", column);
        QueryWrapper<Adopt> qw = new QueryWrapper<>();
        if (mode == QueryFieldGuard.Mode.LIKE) {
            qw.like(column, condition);
        } else {
            qw.eq(column, condition);
        }
        return queryadoptsbypage(current, qw);
    }

    /**
     * 提交领养申请。
     * <p>
     * 改造前：前端先 POST /adopt 建表，再 PATCH /pet 把宠物置为"审核中" —— 两步不原子，
     * 中途失败会出现"有申请记录但宠物仍是流浪中"的脏数据。
     * 现在合并为一个事务；同时 username / nickname / petname 由服务端从库里取，
     * 前端传入的这几个冗余字段一律忽略。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addadopttable(Adopt adopt) {
        Pet pet = petMapper.selectById(adopt.getPid());
        if (pet == null) {
            throw new BizException(ResultBean.NOT_FOUND, "宠物不存在");
        }
        if (Objects.equals(pet.getIsAdopt(), Pet.ADOPT_DONE)) {
            throw new BizException("该宠物已被领养");
        }
        if (Objects.equals(pet.getIsAdopt(), Pet.ADOPT_AUDITING)) {
            throw new BizException("该宠物已有待处理的领养申请");
        }
        User applicant = userMapper.selectById(adopt.getUid());
        if (applicant == null) {
            throw new BizException("申请人不存在");
        }

        adopt.setUsername(applicant.getUsername());
        adopt.setNickname(applicant.getNickname());
        adopt.setPetname(pet.getPetname());
        adopt.setState(Adopt.STATE_PENDING);
        adopt.setIsRead(Adopt.READ);
        if (adoptMapper.insert(adopt) != 1) {
            return false;
        }

        Pet petUpdate = new Pet();
        petUpdate.setId(pet.getId());
        petUpdate.setIsAdopt(Pet.ADOPT_AUDITING);
        petUpdate.setUid(adopt.getUid());
        petMapper.updateById(petUpdate);
        return true;
    }

    /**
     * 取消领养申请：仅本人 + 审核中可取消，宠物在同一事务内退回流浪中。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Integer id, Integer uid) {
        Adopt adopt = adoptMapper.selectById(id);
        if (adopt == null) {
            throw new BizException(ResultBean.NOT_FOUND, "申请记录不存在");
        }
        if (!Objects.equals(adopt.getUid(), uid)) {
            throw new BizException(ResultBean.FORBIDDEN, "只能取消本人的申请");
        }
        if (!Objects.equals(adopt.getState(), Adopt.STATE_PENDING)) {
            throw new BizException("该申请已审核，无法取消");
        }
        // 逻辑删除申请记录（is_del 由 MyBatis-Plus 逻辑删除配置处理）
        if (adoptMapper.deleteById(id) != 1) {
            throw new BizException("取消失败，请重试");
        }
        Pet petUpdate = new Pet();
        petUpdate.setId(adopt.getPid());
        petUpdate.setIsAdopt(Pet.ADOPT_STRAY);
        petUpdate.setUid(Pet.ADOPT_STRAY);
        petMapper.updateById(petUpdate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteone(Integer id) {
        return adoptMapper.deleteById(id) == 1;
    }

    /**
     * 审核领养申请。
     * <p>
     * 改造前由管理端前端串行发 3 个请求（改 adopt、改 pet、改 user 领养数），
     * 任一步失败都会留下不一致数据。现在收敛到单个事务方法内完成全部状态流转。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(AdoptAuditDTO dto) {
        Adopt adopt = adoptMapper.selectById(dto.getId());
        if (adopt == null) {
            throw new BizException(ResultBean.NOT_FOUND, "申请记录不存在");
        }
        if (!Objects.equals(adopt.getState(), Adopt.STATE_PENDING)) {
            throw new BizException("该申请已处理，请勿重复审核");
        }
        boolean passed = Objects.equals(dto.getState(), Adopt.STATE_PASSED);

        // 1) 申请记录：写入审核结果与通知内容，并标记为"用户未读"
        Adopt adoptUpdate = new Adopt();
        adoptUpdate.setId(dto.getId());
        adoptUpdate.setState(dto.getState());
        adoptUpdate.setPonReason(dto.getPonReason());
        adoptUpdate.setIsRead(Adopt.UNREAD);
        adoptMapper.updateById(adoptUpdate);

        // 2) 宠物：通过则归属申请人，否则退回流浪中
        Pet petUpdate = new Pet();
        petUpdate.setId(adopt.getPid());
        petUpdate.setIsAdopt(dto.getState());
        petUpdate.setUid(passed ? adopt.getUid() : Pet.ADOPT_STRAY);
        petMapper.updateById(petUpdate);

        // 3) 用户领养计数：数据库侧原子自增，避免读改写丢更新
        if (passed) {
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .setSql("how_adopt = how_adopt + 1")
                    .eq(User::getId, adopt.getUid()));
        }

        // 4) 站内通知：审核结果进消息中心，用户打开即有未读提醒
        String notice = passed
                ? "🎉 领养申请已通过！您申请的「" + adopt.getPetname() + "」审核通过，平台将与您联系接宠事宜。"
                  + "领养后 7 天内记得发布回访日记哦，发布日记还可获得宠宠币奖励。审核备注："
                  + (dto.getPonReason() == null ? "无" : dto.getPonReason())
                : "很抱歉，您对「" + adopt.getPetname() + "」的领养申请未通过审核。审核备注："
                  + (dto.getPonReason() == null ? "无" : dto.getPonReason());
        Message message = new Message();
        message.setUid(adopt.getUid());
        message.setContent(notice);
        message.setIsRead(Message.UNREAD);
        messageService.addonemessage(message);
        return true;
    }

    /**
     * 批量把该用户的通知标记为已读。
     * 改造前是"查出来再逐条 updateById"（N 次 UPDATE），现为一条批量 UPDATE。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean readalladopt(Integer uid) {
        adoptMapper.update(null, new LambdaUpdateWrapper<Adopt>()
                .set(Adopt::getIsRead, Adopt.READ)
                .eq(Adopt::getUid, uid)
                .eq(Adopt::getIsRead, Adopt.UNREAD));
        return true;
    }

    @Override
    public Long queryallonread(Integer uid) {
        return adoptMapper.selectCount(new LambdaQueryWrapper<Adopt>()
                .eq(Adopt::getUid, uid)
                .eq(Adopt::getIsRead, Adopt.UNREAD));
    }

    /** 批量为申请记录装配申请人与宠物 */
    private void assemble(List<Adopt> adoptList) {
        if (adoptList == null || adoptList.isEmpty()) {
            return;
        }
        Set<Integer> uids = adoptList.stream().map(Adopt::getUid)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Integer> pids = adoptList.stream().map(Adopt::getPid)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Integer, User> userMap = uids.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(uids).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Integer, Pet> petMap = pids.isEmpty() ? Collections.emptyMap()
                : petMapper.selectBatchIds(pids).stream()
                .collect(Collectors.toMap(Pet::getId, Function.identity()));
        adoptList.forEach(adopt -> {
            adopt.setUser(userMap.get(adopt.getUid()));
            adopt.setPet(petMap.get(adopt.getPid()));
        });
    }

    private int normalizePage(int current) {
        return current < 1 ? 1 : current;
    }
}
