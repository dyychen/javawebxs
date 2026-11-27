package com.example.canteen.controller;

import com.example.canteen.common.Result;
import com.example.canteen.common.UserSession;
import com.example.canteen.entity.CartItem;
import com.example.canteen.entity.User;
import com.example.canteen.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
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

    private final UserSession userSession;

    public CartController(CartService cartService, UserSession userSession) {
        this.cartService = cartService;
        this.userSession = userSession;
    }

    @GetMapping
    public Result<List<CartItem>> list(HttpServletRequest request) {
        Integer userId = requireUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        return Result.ok(cartService.list(userId));
    }

    /**
     * 加入购物车
     */
    @PostMapping
    public Result<Void> add(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Integer userId = requireUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        Integer dishId = Integer.valueOf(body.get("dishId").toString());
        Integer quantity = body.get("quantity") == null ? 1 : Integer.valueOf(body.get("quantity").toString());
        cartService.add(userId, dishId, quantity);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> updateQuantity(@PathVariable Integer id, @RequestParam Integer quantity,
                                       HttpServletRequest request) {
        if (requireUserId(request) == null) {
            return Result.fail(401, "请先登录");
        }
        cartService.updateQuantity(null, id, quantity);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Integer id, HttpServletRequest request) {
        if (requireUserId(request) == null) {
            return Result.fail(401, "请先登录");
        }
        cartService.remove(null, id);
        return Result.ok();
    }

    @DeleteMapping
    public Result<Void> clear(HttpServletRequest request) {
        Integer userId = requireUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        cartService.clear(userId);
        return Result.ok();
    }

    @GetMapping("/total")
    public Result<BigDecimal> total(HttpServletRequest request) {
        Integer userId = requireUserId(request);
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }
        return Result.ok(cartService.totalAmount(userId));
    }

    /**
     * 从会话中取当前用户编号，未登录返回 null
     */
    private Integer requireUserId(HttpServletRequest request) {
        User current = userSession.getUser(request);
        return current != null ? current.getId() : null;
    }
}
