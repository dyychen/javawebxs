package com.example.canteen.controller;

import com.example.canteen.common.Result;
import com.example.canteen.common.UserSession;
import com.example.canteen.entity.User;
import com.example.canteen.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    private final UserSession userSession;

    public AuthController(UserService userService, UserSession userSession) {
        this.userService = userService;
        this.userSession = userSession;
    }

    /**
     * 登录：账号 + 密码，返回用户信息（不含密码）
     * 登录成功后把用户信息写入 HttpSession，后续请求通过会话识别身份
     */
    @PostMapping("/login")
    public Result<User> login(HttpServletRequest request, @RequestBody Map<String, String> body) {
        User user = userService.login(body.get("username"), body.get("password"));
        if (user == null) {
            return Result.fail(401, "用户名或密码错误");
        }
        userSession.login(request, user);
        return Result.ok(user);
    }

    /**
     * 注册学生账号
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        return Result.ok(userService.register(
                user.getUsername(), user.getPassword(), user.getRealName(), user.getPhone()));
    }

    /**
     * 从会话中读取当前登录用户
     */
    @GetMapping("/me")
    public Result<User> me(HttpServletRequest request) {
        User user = userSession.getUser(request);
        if (user == null) {
            return Result.fail(401, "未登录");
        }
        return Result.ok(user);
    }

    /**
     * 退出登录，销毁会话
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        userSession.logout(request);
        return Result.ok();
    }
}
