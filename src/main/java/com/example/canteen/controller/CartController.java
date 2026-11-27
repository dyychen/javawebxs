package com.example.canteen.controller;

import com.example.canteen.common.Result;
import com.example.canteen.entity.CartItem;
import com.example.canteen.service.CartService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Result<List<CartItem>> list(@RequestParam Integer userId) {
        return Result.ok(cartService.list(userId));
    }

    /**
     * 加入购物车
     */
    @PostMapping
    public Result<Void> add(@RequestBody Map<String, Object> body) {
        Integer userId = Integer.valueOf(body.get("userId").toString());
        Integer dishId = Integer.valueOf(body.get("dishId").toString());
        Integer quantity = body.get("quantity") == null ? 1 : Integer.valueOf(body.get("quantity").toString());
        cartService.add(userId, dishId, quantity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> updateQuantity(@PathVariable Integer id, @RequestParam Integer quantity) {
        cartService.updateQuantity(null, id, quantity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Integer id) {
        cartService.remove(null, id);
        return Result.ok();
    }

    @DeleteMapping
    public Result<Void> clear(@RequestParam Integer userId) {
        cartService.clear(userId);
        return Result.ok();
    }

    @GetMapping("/total")
    public Result<BigDecimal> total(@RequestParam Integer userId) {
        return Result.ok(cartService.totalAmount(userId));
    }
}
