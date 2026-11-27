package com.example.canteen.controller;

import com.example.canteen.common.Result;
import com.example.canteen.common.UserSession;
import com.example.canteen.entity.User;
import com.example.canteen.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserSession userSession;

    public UserController(UserService userService, UserSession userSession) {
        this.userService = userService;
        this.userSession = userSession;
    }

    @GetMapping
    public Result<List<User>> list(HttpServletRequest request) {
        userSession.requireRole(request, "admin");
        return Result.ok(userService.list());
    }
}
