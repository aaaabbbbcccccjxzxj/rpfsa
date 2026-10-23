package com.erdongj.controller;

import com.erdongj.common.BizException;
import com.erdongj.common.RequireLogin;
import com.erdongj.pojo.ResultBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * @description: 图片上传
 * @author: Erdong J
 * @create: 2023-03-14 22:18
 * <p>
 * 改造前存在四个问题：
 * <ol>
 *   <li>扩展名未校验，只截取原后缀直接落盘，可上传 .jsp / .html / .svg；</li>
 *   <li>写入 {@code classpath:}，打成 jar 运行后必然失败；</li>
 *   <li>往 {@code src/main/resources/static/img/} 再复制一份，污染源码目录；</li>
 *   <li>用毫秒时间戳命名，高并发下会互相覆盖，且 originalFilename 为 null 时 NPE。</li>
 * </ol>
 * 现在统一改为：外部可配置目录 + 扩展名白名单 + UUID 命名 + 大小限制 + 防目录穿越。
 **/
@RestController
@RequireLogin
public class UploadController {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    private static final long MAX_SIZE = 2 * 1024 * 1024L;

    private final Path uploadDir;

    public UploadController(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * 上传图片，返回新文件名（前端拿到后拼到 /img/ 下访问）。
     *
     * @param file 表单字段名固定为 file
     * @return 生成的唯一文件名，如 {@code 3f2a9c...d1.jpg}
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException("图片大小不能超过 2MB");
        }
        String extension = resolveExtension(file.getOriginalFilename());

        try {
            Files.createDirectories(uploadDir);
            String newName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path target = uploadDir.resolve(newName).normalize();
            // 双保险：即使文件名被构造成相对路径，也拒绝写出到上传目录之外
            if (!target.startsWith(uploadDir)) {
                throw new BizException("非法的文件名");
            }
            file.transferTo(target.toFile());
            return newName;
        } catch (IOException e) {
            throw new BizException(ResultBean.ERROR, "图片保存失败，请稍后重试");
        }
    }

    /**
     * 从原始文件名解析扩展名并做白名单校验。
     * 不校验 MIME 类型是因为它由客户端提供、可伪造；扩展名白名单 + 静态资源目录不解析脚本
     * 已经能阻断"上传可执行文件"这一风险。
     */
    private String resolveExtension(String originalFilename) {
        if (originalFilename == null) {
            throw new BizException("文件名不能为空");
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            throw new BizException("文件缺少扩展名");
        }
        String extension = originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BizException("仅支持 jpg / jpeg / png / gif / webp / bmp 格式的图片");
        }
        return extension;
    }
}
