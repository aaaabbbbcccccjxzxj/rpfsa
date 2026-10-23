package com.erdongj.config;

import com.erdongj.common.RequireAdmin;
import com.erdongj.common.RequireLogin;
import com.erdongj.pojo.ResultBean;
import com.erdongj.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 登录与角色校验拦截器。
 * <p>
 * 改造前：后端完全信任调用方，任何人可以直接调用 {@code DELETE /user/1}，登录态只存在于前端。
 * <p>
 * 改造后的策略（贴合原有业务，不改变游客体验）：
 * <ol>
 *   <li>OPTIONS 预检与静态资源直接放行；</li>
 *   <li>GET 请求默认放行 —— 游客可以浏览宠物、帖子、公告；</li>
 *   <li>标注 {@link RequireLogin} 的接口必须登录；</li>
 *   <li>标注 {@link RequireAdmin} 的接口必须是管理员；</li>
 *   <li>其余写操作（POST / PUT / PATCH / DELETE）默认要求登录。</li>
 * </ol>
 * 校验通过后把 uid / role 写入 request attribute，供 Controller 通过
 * {@code @RequestAttribute} 取用，避免"前端传 uid 就信 uid"。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenUtil tokenUtil;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(TokenUtil tokenUtil, ObjectMapper objectMapper) {
        this.tokenUtil = tokenUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Class<?> handlerType = handlerMethod.getBeanType();

        boolean needAdmin = handlerMethod.hasMethodAnnotation(RequireAdmin.class)
                || handlerType.isAnnotationPresent(RequireAdmin.class);
        boolean needLogin = handlerMethod.hasMethodAnnotation(RequireLogin.class)
                || handlerType.isAnnotationPresent(RequireLogin.class);

        // 只要带了合法 token 就先解析出来，游客场景下也允许为空
        TokenUtil.TokenPayload payload = tokenUtil.parse(resolveToken(request));
        if (payload != null) {
            request.setAttribute(TokenUtil.REQ_ATTR_UID, payload.getUid());
            request.setAttribute(TokenUtil.REQ_ATTR_ROLE, payload.getRole());
        }

        boolean isReadOnly = HttpMethod.GET.matches(request.getMethod())
                || HttpMethod.HEAD.matches(request.getMethod());
        if (isReadOnly && !needAdmin && !needLogin) {
            return true;
        }
        if (payload == null) {
            return reject(response, ResultBean.UNAUTHORIZED, "请先登录");
        }
        if (needAdmin && !payload.isAdmin()) {
            return reject(response, ResultBean.FORBIDDEN, "需要管理员权限");
        }
        return true;
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).trim();
        }
        // 兼容表单直传场景
        String fallback = request.getHeader("X-Token");
        return fallback == null ? null : fallback.trim();
    }

    private boolean reject(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(code);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ResultBean.fail(code, msg)));
        return false;
    }
}
