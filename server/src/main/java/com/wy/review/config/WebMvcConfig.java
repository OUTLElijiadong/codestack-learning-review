package com.wy.review.config;

import com.wy.review.interceptor.LoginInterceptor;
import com.wy.review.interceptor.RoleInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置：拦截器注册 + 上传文件静态资源映射
 *
 * 拦截顺序：LoginInterceptor（先校验 JWT 写 UserContext）→ RoleInterceptor（再按 @RequireRole 裁决角色）
 *
 * 静态资源：context-path 为 /api，因此上传图片最终访问地址为
 *   http://localhost:8080/api/uploads/yyyy/MM/uuid.png
 * 前端在 5173 端口通过 vite proxy 访问 /api/uploads/... 即可正常显示图片
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;
    private final RoleInterceptor roleInterceptor;

    @Value("${review.file.upload-path}")
    private String uploadPath;

    public WebMvcConfig(LoginInterceptor loginInterceptor, RoleInterceptor roleInterceptor) {
        this.loginInterceptor = loginInterceptor;
        this.roleInterceptor = roleInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 白名单：登录/注册/验证码/找回密码/上传文件静态访问 不需要登录；
        // 另放行打进 static 的前端页面与其静态资源（整个系统单进程部署时由本服务直接托管页面）
        String[] whiteList = {
                "/auth/login", "/auth/register", "/auth/captcha",
                "/auth/security-question", "/auth/reset-password",
                "/uploads/**", "/error",
                "/", "/index.html", "/assets/**", "/favicon.svg",
                "/login", "/register", "/forgot-password"
        };
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(whiteList)
                .order(1);
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(whiteList)
                .order(2);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 把 /uploads/** 映射到本地 uploads 目录（实际 URL 带 context-path：/api/uploads/**）
        // 统一用绝对路径，与 FileController 的写入目录保持一致
        String absolute = new File(uploadPath).getAbsolutePath();
        if (!absolute.endsWith(File.separator) && !absolute.endsWith("/")) {
            absolute += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolute);
    }
}
