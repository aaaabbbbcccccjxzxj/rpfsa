package com.erdongj.controller;

import com.erdongj.common.RequireAdmin;
import com.erdongj.dto.AdminLoginDTO;
import com.erdongj.dto.UserAdminDTO;
import com.erdongj.pojo.Pagination;
import com.erdongj.pojo.ResultBean;
import com.erdongj.pojo.User;
import com.erdongj.service.AdminService;
import com.erdongj.service.UserService;
import com.erdongj.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 管理员交互层
 * @author: Erdong J
 * @create: 2023-03-08 21:35
 **/
@RestController
@RequestMapping("/admin")
public class AdminController extends BaseController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenUtil tokenUtil;

    /**
     * 管理员登录。
     * 返回 {@code {token, adminname}}；其余 /admin/** 接口均需携带该 token。
     */
    @PostMapping("/login")
    public ResultBean<Map<String, Object>> login(@RequestBody @Valid AdminLoginDTO dto) {
        boolean passed = adminService.Login(dto.getAdminname(), dto.getPassword());
        if (!passed) {
            return faile("管理员不存在或密码错误");
        }
        Map<String, Object> payload = new HashMap<>(4);
        payload.put("token", tokenUtil.issue(0, TokenUtil.ROLE_ADMIN));
        payload.put("adminname", dto.getAdminname());
        return successful("登录成功", payload);
    }

    /** 后台首页统计 */
    @GetMapping("/stats")
    @RequireAdmin
    public ResultBean<Map<String, Long>> getallinfo() {
        return successful("查询成功", adminService.getallinfo());
    }

    /** 近 6 个月运营趋势（领养通过 / 捐助总额 / 新增用户） */
    @GetMapping("/trends")
    @RequireAdmin
    public ResultBean<Map<String, Object>> getTrends() {
        return successful("查询成功", adminService.getTrends());
    }

    /** 导出领养记录 CSV（UTF-8 BOM，Excel 打开不乱码） */
    @GetMapping("/export/adopts")
    @RequireAdmin
    public ResponseEntity<byte[]> exportAdopts() {
        return csv("adopts.csv", adminService.exportAdoptRows());
    }

    /** 导出捐助记录 CSV */
    @GetMapping("/export/donates")
    @RequireAdmin
    public ResponseEntity<byte[]> exportDonates() {
        return csv("donates.csv", adminService.exportDonateRows());
    }

    /** 把行数据拼成 CSV 字节流（开头加 UTF-8 BOM，Excel 打开中文不乱码） */
    private ResponseEntity<byte[]> csv(String filename, java.util.List<java.util.List<String>> rows) {
        StringBuilder sb = new StringBuilder("﻿");
        for (java.util.List<String> row : rows) {
            sb.append(String.join(",", row)).append("\r\n");
        }
        byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(bytes);
    }

    // ------------------------------------------------------------------
    // 用户管理。原先挂在 /user 下与用户端接口混在一起，无法做角色隔离，
    // 现统一收敛到 /admin/users 并强制管理员身份。
    // ------------------------------------------------------------------

    @GetMapping("/users/{c}")
    @RequireAdmin
    public ResultBean<Pagination<User>> getusersbypage(@PathVariable("c") int current) {
        Pagination<User> pagination = userService.queryusersbypage(current, null);
        if (pagination.getTotal() == 0) {
            return faile("无用户信息");
        }
        return successful("查询成功", pagination);
    }

    @GetMapping("/users/{c}/{col}/{con}")
    @RequireAdmin
    public ResultBean<Pagination<User>> likequery(@PathVariable(value = "c") int current,
                                                  @PathVariable(value = "col") String column,
                                                  @PathVariable(value = "con") String condition) {
        Pagination<User> pagination = userService.likequery(current, column, condition);
        if (pagination.getTotal() == 0) {
            return faile("无符合结果");
        }
        return successful("查询成功", pagination);
    }

    @PostMapping("/users")
    @RequireAdmin
    public ResultBean<User> addoneuser(@RequestBody @Valid UserAdminDTO dto) {
        dto.setId(null);
        return successful("添加成功", userService.adminSave(dto));
    }

    @PatchMapping("/users")
    @RequireAdmin
    public ResultBean<User> updateoneuser(@RequestBody @Valid UserAdminDTO dto) {
        return successful("修改成功", userService.adminSave(dto));
    }

    @DeleteMapping("/users/{id}")
    @RequireAdmin
    public ResultBean<Void> deleteuserbyid(@PathVariable("id") Integer id) {
        return userService.deleteonebyID(id) ? successful("删除成功") : faile("删除失败");
    }
}
