package com.wy.review.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wy.review.annotation.RequireRole;
import com.wy.review.common.LoginUser;
import com.wy.review.common.Result;
import com.wy.review.common.ResultCode;
import com.wy.review.common.UserContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 角色权限拦截器（第二道）：
 * 读取 Controller 方法上的 @RequireRole 注解做角色裁决。
 * 方法未标注注解 → 任意登录用户可访问；标注了 → role 不在名单内返回 403
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 方法注解优先；方法未标注时看类级注解（类上统一限定角色的场景）
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            // 未标注角色注解：登录即可访问
            return true;
        }
        LoginUser user = UserContext.get();
        if (user == null) {
            return true; // 登录拦截器已处理
        }
        boolean allowed = Arrays.asList(requireRole.value()).contains(user.getRole());
        if (!allowed) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(objectMapper.writeValueAsString(Result.fail(ResultCode.FORBIDDEN)));
            return false;
        }
        return true;
    }
}
