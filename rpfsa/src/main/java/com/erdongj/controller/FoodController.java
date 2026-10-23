package com.erdongj.controller;

import com.erdongj.common.RequireAdmin;
import com.erdongj.pojo.Food;
import com.erdongj.pojo.ResultBean;
import com.erdongj.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: Erdong J
 * @Date: 2023/5/6 09:02
 * @Description: 救助品控制层
 */

@RestController
@RequestMapping("/food")
public class FoodController extends BaseController {

    @Autowired
    private FoodService foodService;

    /** 救助品清单（游客可访问，捐助弹层需要展示） */
    @GetMapping
    public ResultBean<List<Food>> getallfoods() {
        List<Food> foodList = foodService.queryallfoods();
        if (foodList.isEmpty()) {
            return faile("平台暂无救助品");
        }
        return successful("查询成功", foodList);
    }

    /** 新增救助品（管理端） */
    @PostMapping
    @RequireAdmin
    public ResultBean<Void> addonefood(@RequestBody Food food) {
        return foodService.addonefood(food) ? successful("添加成功！") : faile("添加失败");
    }

    /** 删除救助品（管理端） */
    @DeleteMapping("/{id}")
    @RequireAdmin
    public ResultBean<Void> deleteonefood(@PathVariable Integer id) {
        return foodService.deleteonefood(id) ? successful("删除成功！") : faile("删除失败");
    }

    /** 修改救助品（管理端） */
    @PatchMapping
    @RequireAdmin
    public ResultBean<Void> updatefood(@RequestBody Food food) {
        return foodService.updatefood(food) ? successful("修改成功！") : faile("修改失败");
    }
}
