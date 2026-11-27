package com.example.canteen.common;

import com.example.canteen.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

/**
 * 基于 HttpSession 的登录会话管理。
 */
@Component
public class UserSession {

    public static final String ATTRIBUTE = "loginUser";

    /**
     * 获取当前登录用户，未登录返回 null
     */
    public User getUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(ATTRIBUTE);
        return value instanceof User ? (User) value : null;
    }

    /**
     * 登录成功，把用户信息放入会话
     */
    public void login(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(ATTRIBUTE, user);
    }

    /**
     * 退出登录，销毁会话
     */
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
