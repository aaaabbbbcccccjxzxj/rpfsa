package com.erdongj.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 无状态签名 Token（HMAC-SHA256）。
 * <p>
 * 不引入 JWT 依赖，用 JDK 自带的 Mac 实现同等效果：
 * {@code base64url(uid|role|expireAt) + "." + hexHmac}。
 * 服务端无需存储会话，重启不掉线，同时具备防篡改能力。
 */
@Component
public class TokenUtil {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ADMIN = "admin";

    public static final String REQ_ATTR_UID = "currentUid";
    public static final String REQ_ATTR_ROLE = "currentRole";

    private static final String ALGORITHM = "HmacSHA256";
    private static final long TTL_MILLIS = 7L * 24 * 60 * 60 * 1000;

    private final byte[] secret;

    public TokenUtil(@Value("${app.token.secret}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 签发 token。
     *
     * @param uid  用户主键；管理员固定传 0
     * @param role {@link #ROLE_USER} 或 {@link #ROLE_ADMIN}
     */
    public String issue(Integer uid, String role) {
        String payload = uid + "|" + role + "|" + (System.currentTimeMillis() + TTL_MILLIS);
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + sign(encoded);
    }

    /**
     * 校验并解析 token。
     *
     * @return 解析结果；token 缺失、被篡改或已过期时返回 {@code null}
     */
    public TokenPayload parse(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        int dot = token.lastIndexOf('.');
        if (dot <= 0 || dot == token.length() - 1) {
            return null;
        }
        String encoded = token.substring(0, dot);
        String signature = token.substring(dot + 1);
        if (!constantTimeEquals(sign(encoded), signature)) {
            return null;
        }
        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
        String[] parts = payload.split("\\|");
        if (parts.length != 3) {
            return null;
        }
        try {
            if (Long.parseLong(parts[2]) < System.currentTimeMillis()) {
                return null;
            }
            return new TokenPayload(Integer.parseInt(parts[0]), parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            char[] hex = new char[raw.length * 2];
            for (int i = 0; i < raw.length; i++) {
                hex[i * 2] = Character.forDigit((raw[i] >> 4) & 0xF, 16);
                hex[i * 2 + 1] = Character.forDigit(raw[i] & 0xF, 16);
            }
            return new String(hex);
        } catch (Exception e) {
            throw new IllegalStateException("token 签名失败", e);
        }
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

    /** token 载荷 */
    public static class TokenPayload {
        private final Integer uid;
        private final String role;

        public TokenPayload(Integer uid, String role) {
            this.uid = uid;
            this.role = role;
        }

        public Integer getUid() {
            return uid;
        }

        public String getRole() {
            return role;
        }

        public boolean isAdmin() {
            return ROLE_ADMIN.equals(role);
        }
    }
}
