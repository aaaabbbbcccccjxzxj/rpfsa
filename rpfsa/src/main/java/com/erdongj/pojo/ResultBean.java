package com.erdongj.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 统一返回结果集合
 * @author: Erdong J
 * @create: 2023-03-17 21:38
 **/
@Data
@NoArgsConstructor
public class ResultBean<T> {

    /** 业务处理成功 */
    public static final int SUCCESS = 200;
    /** 参数校验或业务规则不通过 */
    public static final int FAIL = 400;
    /** 未登录 / 登录态失效 */
    public static final int UNAUTHORIZED = 401;
    /** 已登录但无权限 */
    public static final int FORBIDDEN = 403;
    /** 资源不存在 */
    public static final int NOT_FOUND = 404;
    /** 服务端异常 */
    public static final int ERROR = 500;

    private int code;
    private String msg;
    private T data;

    public ResultBean(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ResultBean<T> success(String msg, T data) {
        return new ResultBean<>(SUCCESS, msg, data);
    }

    public static <T> ResultBean<T> success(String msg) {
        return new ResultBean<>(SUCCESS, msg, null);
    }

    public static <T> ResultBean<T> fail(String msg) {
        return new ResultBean<>(FAIL, msg, null);
    }

    public static <T> ResultBean<T> fail(int code, String msg) {
        return new ResultBean<>(code, msg, null);
    }
}
