package com.example.canteen.service;

import com.example.canteen.entity.CartItem;
import com.example.canteen.entity.Dish;
import com.example.canteen.mapper.CartMapper;
import com.example.canteen.mapper.DishMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {

    private final CartMapper cartMapper;
    private final DishMapper dishMapper;

    public CartService(CartMapper cartMapper, DishMapper dishMapper) {
        this.cartMapper = cartMapper;
        this.dishMapper = dishMapper;
    }

    public List<CartItem> list(Integer userId) {
        return cartMapper.selectByUserId(userId);
    }

    /**
     * 加入购物车：同一菜品累加数量，并校验库存
     */
    public void add(Integer userId, Integer dishId, Integer quantity) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || dish.getStatus() != 1) {
            throw new IllegalArgumentException("菜品不存在或已下架");
        }
        CartItem exist = cartMapper.selectByUserAndDish(userId, dishId);
        int target = (exist == null ? 0 : exist.getQuantity()) + quantity;
        if (target > dish.getStock()) {
            throw new IllegalArgumentException("库存不足，当前库存 " + dish.getStock());
        }
        if (exist == null) {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setDishId(dishId);
            item.setQuantity(quantity);
            cartMapper.insert(item);
        } else {
            cartMapper.updateQuantity(exist.getId(), target);
        }
    }

    public void updateQuantity(Integer userId, Integer itemId, Integer quantity) {
        cartMapper.updateQuantity(itemId, quantity);
    }

    public void remove(Integer userId, Integer itemId) {
        cartMapper.deleteById(itemId);
    }

    public void clear(Integer userId) {
        cartMapper.deleteByUserId(userId);
    }

    public BigDecimal totalAmount(Integer userId) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartMapper.selectByUserId(userId)) {
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }
}
