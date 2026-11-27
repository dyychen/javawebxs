package com.example.canteen.mapper;

import com.example.canteen.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User selectById(@Param("id") Integer id);

    User selectByUsername(@Param("username") String username);

    List<User> selectAll();

    int insert(User user);

    int countByUsername(@Param("username") String username);
}
