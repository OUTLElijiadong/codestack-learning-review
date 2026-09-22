package com.wy.review.common;

/**
 * 当前登录用户上下文（ThreadLocal）
 * LoginInterceptor 在 preHandle 写入，afterCompletion 清除；
 * Service 层通过 UserContext.get() 获取当前登录人，
 * 坚决不信任前端传来的 userId，防止横向越权
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 获取当前登录人ID，未登录时抛 401（拦截器已保证不会走到这） */
    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user.getId();
    }

    public static String getRole() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return user.getRole();
    }

    public static void remove() {
        HOLDER.remove();
    }
}
