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

    private static Long parseUserId(Object value) {
        if (value == null) {
            throw new ServiceException("未登录");
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            return Long.valueOf((String) value);
        }
        throw new ServiceException("未登录");
    }
}
