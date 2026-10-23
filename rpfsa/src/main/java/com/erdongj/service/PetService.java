package com.erdongj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.Pet;

import java.util.List;

/**
 * @Author: Erdong J
 * @Date: 2023/4/9 17:09
 * @Description:
 */
public interface PetService {

    /**
     * 查询所有收到宠宠币前10宠物
     *
     * @return
     */
    List<Pet> queryallpets();

    /**
     * 根据用户id返回该用户领养的所有动物
     *
     * @param uid
     * @return
     */
    List<Pet> querypetbyuid(Integer uid);

    /**
     * 根据ID查询宠物
     *
     * @param id
     * @return
     */
    Pet queryonebyID(Integer id);

    /**
     * 管理员根据ID删除宠物
     *
     * @param id
     * @return
     */
    boolean deleteonebyID(Integer id);

    /**
     * 添加一个宠物
     *
     * @param pet
     * @return
     */

    boolean addonepet(Pet pet);

    /**
     * 修改一个宠物
     *
     * @param pet
     * @return
     */
    boolean updateonepet(Pet pet);

    /**
     * 可按条件分页
     * @param current
     * @param qw
     * @return
     */
    Pagination<Pet> querypetsbypage(int current, QueryWrapper<Pet> qw);

    /**
     * 模糊查询,封装条件调分页查询
     *
     * @param current
     * @param column
     * @param condition
     * @return
     */
    Pagination<Pet> conditionquery(int current, String column, String condition);

    /**
     * 多条件组合筛选（用户端"流浪之家"）。
     * 所有参数均可为空，空表示该条件不生效。
     *
     * @param current   页码
     * @param classic   品种分类：0 狗 1 猫 2 其他
     * @param type      来源类型：0 平台流浪 1 私人上传
     * @param sex       性别：0 母 1 公 2 不详
     * @param minAge    最小年龄（含）
     * @param maxAge    最大年龄（含）
     * @param isVaccine 是否接种疫苗
     * @param isAdopt   领养状态：0 流浪中 1 已领养 2 审核中
     * @param keyword   名称/品种关键词
     */
    Pagination<Pet> filter(int current, Integer classic, Integer type, Integer sex,
                           Integer minAge, Integer maxAge, Integer isVaccine, Integer isAdopt,
                           String keyword);

    /**
     * 添加宠物相册照片（管理端）。
     *
     * @param pid 宠物 id
     * @param url 上传接口返回的文件名
     */
    boolean addPhoto(Integer pid, String url);

    /**
     * 删除宠物相册照片（管理端）。
     */
    boolean deletePhoto(Integer photoId);

}
