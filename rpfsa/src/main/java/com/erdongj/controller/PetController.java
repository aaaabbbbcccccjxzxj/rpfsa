package com.erdongj.controller;

import com.erdongj.common.RequireAdmin;
import com.erdongj.common.RequireLogin;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.Pet;
import com.erdongj.pojo.ResultBean;
import com.erdongj.service.PetService;
import com.erdongj.util.TokenUtil;
import com.erdongj.util.SimpleCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: Erdong J
 * @Date: 2023/4/10 10:09
 * @Description: 宠物控制层
 */

@RestController
@RequestMapping("/pet")
public class PetController extends BaseController {

    @Autowired
    private PetService petService;

    @Autowired
    private SimpleCache simpleCache;

    /** 宠物分页（游客可访问） */
    @GetMapping("/page/{c}")
    public ResultBean<Pagination<Pet>> getpetsbypage(@PathVariable("c") int current) {
        Pagination<Pet> pagination = petService.querypetsbypage(current, null);
        if (pagination.getTotal() == 0) {
            return faile("无宠物信息");
        }
        return successful("查询成功", pagination);
    }

    /**
     * 按字段检索（游客可访问）。
     * column 会经过 QueryFieldGuard 白名单校验，非白名单字段直接拒绝。
     */
    @GetMapping("/{c}/{col}/{con}")
    public ResultBean<Pagination<Pet>> likequery(@PathVariable(value = "c") int current,
                                                 @PathVariable(value = "col") String column,
                                                 @PathVariable(value = "con") String condition) {
        Pagination<Pet> pagination = petService.conditionquery(current, column, condition);
        if (pagination.getTotal() == 0) {
            return faile("无符合结果");
        }
        return successful("查询成功", pagination);
    }

    /** 宠宠币排行前 10（60s 缓存） */
    @GetMapping
    public ResultBean<List<Pet>> get10pets() {
        return successful("排行前十", simpleCache.get("rank:pets", petService::queryallpets));
    }

    /**
     * 多条件组合筛选（游客可访问）。
     * 所有参数均为可选，空表示该条件不生效。
     */
    @GetMapping("/filter")
    public ResultBean<Pagination<Pet>> filter(@RequestParam(defaultValue = "1") int current,
                                              @RequestParam(required = false) Integer classic,
                                              @RequestParam(required = false) Integer type,
                                              @RequestParam(required = false) Integer sex,
                                              @RequestParam(required = false) Integer minAge,
                                              @RequestParam(required = false) Integer maxAge,
                                              @RequestParam(required = false) Integer isVaccine,
                                              @RequestParam(required = false) Integer isAdopt,
                                              @RequestParam(required = false) String keyword) {
        Pagination<Pet> pagination = petService.filter(current, classic, type, sex,
                minAge, maxAge, isVaccine, isAdopt, keyword);
        if (pagination.getTotal() == 0) {
            return faile("无符合结果");
        }
        return successful("查询成功", pagination);
    }

    /** 宠物相册添加照片（管理端） */
    @PostMapping("/photo")
    @RequireAdmin
    public ResultBean<Void> addPhoto(@RequestBody java.util.Map<String, Object> body) {
        Object pid = body.get("pid");
        Object url = body.get("url");
        if (!(pid instanceof Number) || url == null || url.toString().trim().isEmpty()) {
            return faile("参数不完整");
        }
        return petService.addPhoto(((Number) pid).intValue(), url.toString().trim())
                ? successful("添加成功") : faile("添加失败");
    }

    /** 宠物相册删除照片（管理端） */
    @DeleteMapping("/photo/{id}")
    @RequireAdmin
    public ResultBean<Void> deletePhoto(@PathVariable("id") Integer id) {
        return petService.deletePhoto(id) ? successful("删除成功") : faile("删除失败");
    }

    @GetMapping("/{id}")
    public ResultBean<Pet> getonebyid(@PathVariable Integer id) {
        Pet pet = petService.queryonebyID(id);
        return pet == null ? faile("无结果") : successful("查询成功", pet);
    }

    /** 我的猫狗（上传的 + 领养的），只允许查自己 */
    @GetMapping("/mypets/{id}")
    @RequireLogin
    public ResultBean<List<Pet>> getmypetsbyid(@PathVariable int id,
                                               @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                               @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (!TokenUtil.ROLE_ADMIN.equals(role) && !Integer.valueOf(id).equals(currentUid)) {
            return fail(ResultBean.FORBIDDEN, "无权查看他人的宠物");
        }
        List<Pet> petList = petService.querypetbyuid(id);
        if (petList.isEmpty()) {
            return faile("暂无宠物数据");
        }
        return successful("查询成功", petList);
    }

    /** 删除宠物（管理端功能） */
    @DeleteMapping("/{id}")
    @RequireAdmin
    public ResultBean<Void> deletepetbyid(@PathVariable("id") Integer id) {
        return petService.deleteonebyID(id) ? successful("删除成功") : faile("删除失败");
    }

    /**
     * 添加宠物：用户上传私人猫狗与管理端登记平台流浪宠物共用。
     * <p>
     * 非管理员提交时强制把来源置为"私人上传"、主人置为当前登录用户，
     * 避免伪造 type / uid 冒领或越权登记。
     */
    @PostMapping
    public ResultBean<Void> addonepet(@RequestBody Pet pet,
                                      @RequestAttribute(TokenUtil.REQ_ATTR_UID) Integer currentUid,
                                      @RequestAttribute(TokenUtil.REQ_ATTR_ROLE) String role) {
        if (!TokenUtil.ROLE_ADMIN.equals(role)) {
            pet.setType(Pet.TYPE_PRIVATE);
            pet.setUid(currentUid);
            pet.setIsAdopt(Pet.ADOPT_STRAY);
            pet.setPetCoin(0);
            pet.setCommentCount(0);
        }
        return petService.addonepet(pet) ? successful("添加成功") : faile("添加失败");
    }

    /**
     * 修改宠物信息（管理端功能）。
     * <p>
     * 用户端原先用于"回写评论数"和"申请领养时改状态"的调用已下沉到后端，
     * 评论数由 CommentService 维护、领养状态由 AdoptService 维护，前端不再直接改。
     */
    @PatchMapping
    @RequireAdmin
    public ResultBean<Void> updateonepet(@RequestBody Pet pet) {
        return petService.updateonepet(pet) ? successful("修改成功") : faile("修改失败");
    }
}
