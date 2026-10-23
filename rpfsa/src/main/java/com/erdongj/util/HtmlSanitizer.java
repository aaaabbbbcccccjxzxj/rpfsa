package com.erdongj.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * 富文本白名单过滤。
 * <p>
 * 帖子 / 日记 / 公告的正文由 Quill 编辑器产生，以 HTML 形式入库、前端用 {@code v-html} 渲染。
 * 由于 {@code POST /post} 可以被直接调用（绕过编辑器），需在入库前做白名单清洗，
 * 防止 {@code <img onerror=...>} 之类的存储型 XSS。
 * <p>
 * 保留了 class / style 属性，以免破坏 Quill 输出的排版效果（{@code ql-align-center} 等）。
 */
public final class HtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.relaxed()
            .addTags("s", "hr", "figure", "figcaption")
            .addAttributes(":all", "class", "style")
            .addAttributes("a", "target", "rel")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https");

    private HtmlSanitizer() {
    }

    public static String clean(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        return Jsoup.clean(html, SAFELIST);
    }
}
