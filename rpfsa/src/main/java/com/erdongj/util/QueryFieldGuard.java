package com.erdongj.util;

import com.erdongj.common.BizException;
import com.erdongj.pojo.ResultBean;

import java.util.HashMap;
import java.util.Map;

/**
 * 查询字段白名单守卫。
 * <p>
 * 改造前的接口形如 {@code GET /pet/{c}/{col}/{con}}，{@code col} 由前端自由传入并
 * 直接交给 MyBatis-Plus 的 {@code QueryWrapper.like(column, value)}。由于 MyBatis-Plus
 * 对列名做的是字符串拼接（只有 value 是参数化绑定），这构成列名注入面。
 * <p>
 * 改造后：所有按列查询必须先经此守卫，非白名单字段一律拒绝，
 * 与原始 SQL 条件（{@link Mode}）一并由守卫给出，避免各处重复写 if-else。
 */
public final class QueryFieldGuard {

    /** 条件拼接方式 */
    public enum Mode {
        /** 模糊匹配 */
        LIKE,
        /** 精确匹配 */
        EQ,
        /** 不等于 */
        NE
    }

    private static final Map<String, Map<String, Mode>> ALLOW = new HashMap<>();

    static {
        ALLOW.put("user", Map.of(
                "username", Mode.LIKE,
                "nickname", Mode.LIKE,
                "phone", Mode.LIKE,
                "email", Mode.LIKE));
        ALLOW.put("pet", Map.of(
                "petname", Mode.LIKE,
                "variety", Mode.LIKE,
                "classic", Mode.EQ,
                "type", Mode.EQ,
                "is_adopt", Mode.EQ));
        ALLOW.put("post", Map.of(
                "title", Mode.LIKE,
                "uid", Mode.NE,
                "classic", Mode.EQ));
        ALLOW.put("adopt", Map.of(
                "username", Mode.LIKE,
                "nickname", Mode.LIKE,
                "petname", Mode.LIKE));
        ALLOW.put("donate", Map.of(
                "username", Mode.LIKE,
                "nickname", Mode.LIKE,
                "petname", Mode.LIKE));
        ALLOW.put("comment", Map.of(
                "content", Mode.LIKE));
        ALLOW.put("recharge", Map.of(
                "uid", Mode.EQ));
    }

    private QueryFieldGuard() {
    }

    /**
     * 校验字段合法性并返回应使用的条件模式。
     *
     * @param domain 领域标识（user / pet / post / adopt / donate）
     * @param column 前端传入的列名
     * @throws BizException 字段不在白名单内
     */
    public static Mode check(String domain, String column) {
        Map<String, Mode> fields = ALLOW.get(domain);
        Mode mode = fields == null ? null : fields.get(column);
        if (mode == null) {
            throw new BizException(ResultBean.FAIL, "不支持的查询字段");
        }
        return mode;
    }
}
