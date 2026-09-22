package com.wy.review.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wy.review.common.LoginUser;
import com.wy.review.common.Result;
import com.wy.review.common.ResultCode;
import com.wy.review.common.UserContext;
import com.wy.review.utils.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * 登录拦截器（第一道）：
 * 从 Authorization: Bearer <token> 头解析 JWT，
 * 成功则把 LoginUser 写入 UserContext；失败直接返回 401 JSON
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String header = request.getHeader("Authorization");
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }
        if (token == null || token.isEmpty()) {
            writeUnauthorized(response);
            return false;
        }
        try {
            LoginUser loginUser = jwtUtil.parse(token);
            UserContext.set(loginUser);
            return true;
        } catch (Exception e) {
            // 签名错误 / token 过期
            writeUnauthorized(response);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 必须清理 ThreadLocal，防止 Tomcat 线程复用导致串号
        UserContext.remove();
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(ResultCode.UNAUTHORIZED)));
    }
}
