package io.leavesfly.tinyclaw.web.handler;

import com.sun.net.httpserver.HttpExchange;
import io.leavesfly.tinyclaw.web.WebUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 处理前端静态资源（从 classpath web/ 目录加载）。
 * 包含路径遍历攻击防护。
 */
public class StaticHandler {

    /**
     * 入口路由：对请求路径正规化并进行路径穿越防护处理，然后从 classpath 提供静态资源。
     */
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        path = normalizeStaticPath(path);

        if (isPathTraversalAttempt(path)) {
            WebUtils.sendError(exchange, 403, "Forbidden");
            return;
        }

        serveStaticResource(exchange, path);
    }

    /**
     * 将根路径 "/" 或空路径转换为 "index.html"，其他路径就射1不变。
     */
    private String normalizeStaticPath(String path) {
        if (WebUtils.PATH_ROOT.equals(path) || path.isEmpty()) {
            return WebUtils.PATH_INDEX;
        }
        return path;
    }

    /**
     * 检测路径是否包含 ".." 等路径穿越尝试。
     */
    private boolean isPathTraversalAttempt(String path) {
        return path.contains(WebUtils.PATH_PARENT);
    }

    /**
     * 从 classpath 的 web/ 目录加载对应资源，资源不存在时返回 404。
     */
    private void serveStaticResource(HttpExchange exchange, String path) throws IOException {
        String resourcePath = WebUtils.RESOURCE_PREFIX + path;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                WebUtils.sendError(exchange, 404, "Not Found");
                return;
            }
            sendStaticFile(exchange, is, path);
        }
    }

    /**
     * 读取资源内容，依据文件后缀设置 Content-Type 并写入响应。
     */
    private void sendStaticFile(HttpExchange exchange, InputStream is, String path) throws IOException {
        byte[] content = is.readAllBytes();
        String contentType = WebUtils.getContentType(path);
        exchange.getResponseHeaders().set(WebUtils.HEADER_CONTENT_TYPE, contentType);
        exchange.sendResponseHeaders(200, content.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }
}
