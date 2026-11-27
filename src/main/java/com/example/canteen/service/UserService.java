package com.example.canteen.service;

import com.example.canteen.entity.User;
import com.example.canteen.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 登录验证，成功返回用户信息（不含密码），失败返回 null
     */
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return null;
        }
        user.setPassword(null);
        return user;
    }

    /**
     * 注册为学生账号
     */
    public User register(String username, String password, String realName, String phone) {
        if (userMapper.countByUsername(username) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("student");
        user.setRealName(realName);
        user.setPhone(phone);
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    public List<User> list() {
        List<User> users = userMapper.selectAll();
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    public User getById(Integer id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }
}
