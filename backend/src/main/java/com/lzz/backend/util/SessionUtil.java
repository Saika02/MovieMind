package com.lzz.backend.util;

import com.lzz.backend.common.GlobalConstant;
import com.lzz.backend.exception.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionUtil {
    private SessionUtil() {
    }

    public static Long requireUserId(HttpSession session) {
        Object value = session == null ? null : session.getAttribute(GlobalConstant.SESSION_USER_ID);
        return parseUserId(value);
    }

    public static Long requireUserId(HttpServletRequest request) {
        Object value = request.getAttribute(GlobalConstant.SESSION_USER_ID);
        if (value != null) {
            return parseUserId(value);
        }
        return requireUserId(request.getSession(false));
    }

    public static Integer requireRole(HttpSession session) {
        Object value = session == null ? null : session.getAttribute(GlobalConstant.SESSION_ROLE);
        return parseRole(value);
    }

    public static Integer requireRole(HttpServletRequest request) {
        Object value = request.getAttribute(GlobalConstant.SESSION_ROLE);
        if (value != null) {
            return parseRole(value);
        }
        return requireRole(request.getSession(false));
    }

    public static void requireAdmin(HttpSession session) {
        Integer role = requireRole(session);
        if (!Integer.valueOf(GlobalConstant.ROLE_ADMIN).equals(role)) {
            throw new ServiceException("无管理员权限");
        }
    }

    public static void requireAdmin(HttpServletRequest request) {
        Integer role = requireRole(request);
        if (!Integer.valueOf(GlobalConstant.ROLE_ADMIN).equals(role)) {
            throw new ServiceException("无管理员权限");
        }
    }

    private static Long parseUserId(Object value) {
        if (value == null) {
            throw new ServiceException("未登录");
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (value instanceof String text) {
            return Long.valueOf(text);
        }
        throw new ServiceException("未登录");
    }

    private static Integer parseRole(Object value) {
        if (value == null) {
            throw new ServiceException("未登录");
        }
        if (value instanceof Integer intValue) {
            return intValue;
        }
        if (value instanceof Long longValue) {
            return longValue.intValue();
        }
        if (value instanceof String text) {
            return Integer.valueOf(text);
        }
        throw new ServiceException("未登录");
    }
}
