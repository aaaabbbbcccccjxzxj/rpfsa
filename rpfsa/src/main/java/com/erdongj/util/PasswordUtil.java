package com.erdongj.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码工具：BCrypt 加密 + 与历史明文数据的渐进式兼容。
 * <p>
 * 改造前：密码明文入库（{@code varchar(20)}），登录用 {@code eq(password, ...)} 直接比对。
 * <p>
 * 渐进式迁移策略：老数据的 password 列装不下 60 位的 BCrypt 密文，因此
 * {@link #matches} 在识别到非 BCrypt 格式时按明文比对，
 * 调用方比对成功后立即用 {@link #encode} 升级入库。存量用户无需重置密码即可平滑过渡。
 */
@Component
public class PasswordUtil {

    private static final String BCRYPT_PREFIX = "$2";

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isEncoded(storedPassword)) {
            return encoder.matches(rawPassword, storedPassword);
        }
        // 历史明文数据：用常量时间比较，避免时序侧信道
        return constantTimeEquals(rawPassword, storedPassword);
    }

    /** 判断库中密码是否已是 BCrypt 密文 */
    public boolean isEncoded(String storedPassword) {
        return storedPassword != null && storedPassword.startsWith(BCRYPT_PREFIX);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }
}
