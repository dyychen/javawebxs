package com.example.canteen.mapper;

import com.example.canteen.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {

    List<CartItem> selectByUserId(@Param("userId") Integer userId);

    CartItem selectByUserAndDish(@Param("userId") Integer userId,
                                 @Param("dishId") Integer dishId);

    int insert(CartItem item);

    int updateQuantity(@Param("id") Integer id, @Param("quantity") Integer quantity);

    int deleteById(@Param("id") Integer id);

    int deleteByUserId(@Param("userId") Integer userId);

    int countByUserId(@Param("userId") Integer userId);
}
