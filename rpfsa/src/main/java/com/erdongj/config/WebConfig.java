package com.erdongj.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Web 层统一配置：拦截器注册、上传目录映射、跨域策略。
 * <p>
 * 改造前：11 个 Controller 各自标注 {@code @CrossOrigin}（等价于允许任意来源），
 * 上传的文件写进 {@code classpath:}（打包成 jar 后直接失效）与源码目录（污染产物）。
 * 现在统一收敛到这一处。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** 无需登录即可访问的路径 */
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/user/login", "/user/register", "/admin/login", "/error",
            // 找回密码：游客场景，不能要求登录
            "/user/password/code", "/user/password/reset",
            "/swagger-ui.html", "/swagger-ui/**", "/swagger-resources/**", "/v2/api-docs",
            "/webjars/**", "/doc.html",
            "/favicon.ico", "/logo.png", "/index.html", "/assets/**");

    private final AuthInterceptor authInterceptor;
    private final String uploadDir;
    private final List<String> allowedOrigins;

    public WebConfig(AuthInterceptor authInterceptor,
                     @Value("${app.upload.dir}") String uploadDir,
                     @Value("${app.cors.allowed-origins:}") String origins) {
        this.authInterceptor = authInterceptor;
        this.uploadDir = uploadDir;
        this.allowedOrigins = Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(PUBLIC_PATHS);
    }

    /**
     * 把外部上传目录挂载到 /img/**。
     * 图片不再写入 classpath，也不再"复制一份到 src/main/resources"，重启与打包都安全。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File dir = Paths.get(uploadDir).toAbsolutePath().normalize().toFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("上传目录无法创建：" + dir.getAbsolutePath());
        }
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:" + dir.getAbsolutePath() + File.separator);
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (allowedOrigins.isEmpty()) {
            return;
        }
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
