package com.erdongj.controller;

import com.erdongj.pojo.ResultBean;

/**
 * @Author: Erdong J
 * @Date: 2023/5/27 15:27
 * @Description: 所有控制类的基类
 */
public abstract class BaseController {

    /**
     * 成功并携带数据。
     * <p>
     * 保留原名 successful / faile（而非 success / fail）是为了与既有 Controller 保持一致，
     * 同时把原来的裸类型 {@code ResultBean} 换成泛型 {@code ResultBean<T>}，让响应体类型可推断。
     */
    protected <T> ResultBean<T> successful(String msg, T data) {
        return ResultBean.success(msg, data);
    }

    /** 成功但无数据返回 */
    protected ResultBean<Void> successful(String msg) {
        return ResultBean.success(msg);
    }

    /** 业务校验失败（HTTP 400 语义） */
    protected <T> ResultBean<T> faile(String msg) {
        return ResultBean.fail(msg);
    }

    /** 指定错误码的失败响应 */
    protected <T> ResultBean<T> fail(int code, String msg) {
        return ResultBean.fail(code, msg);
    }
}
