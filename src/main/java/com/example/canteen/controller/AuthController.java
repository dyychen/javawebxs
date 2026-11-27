package com.example.canteen.controller;

import com.example.canteen.common.Result;
import com.example.canteen.entity.User;
import com.example.canteen.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 登录：账号 + 密码，返回用户信息（不含密码）
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> body) {
        User user = userService.login(body.get("username"), body.get("password"));
        if (user == null) {
            return Result.fail(401, "用户名或密码错误");
        }
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
}
