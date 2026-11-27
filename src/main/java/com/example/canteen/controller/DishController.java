package com.example.canteen.controller;

import com.example.canteen.common.Result;
import com.example.canteen.common.AccessDeniedException;
import com.example.canteen.common.UserSession;
import com.example.canteen.entity.User;
import com.example.canteen.entity.Dish;
import com.example.canteen.service.CanteenService;
import com.example.canteen.service.DishService;
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

import java.util.List;

/**
 * 菜品管理：浏览、新增、修改、下架、库存调整
 */
@RestController
@RequestMapping("/api/dishes")
public class DishController {

    private final DishService dishService;
    private final UserSession userSession;
    private final CanteenService canteenService;

    public DishController(DishService dishService, UserSession userSession, CanteenService canteenService) {
        this.dishService = dishService;
        this.userSession = userSession;
        this.canteenService = canteenService;
    }

    @GetMapping
    public Result<List<Dish>> list(@RequestParam(required = false) Integer windowId,
                                   @RequestParam(required = false) Integer categoryId,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer merchantId,
                                   HttpServletRequest request) {
        userSession.requireUser(request);
        return Result.ok(dishService.list(windowId, categoryId, keyword, merchantId));
    }

    @GetMapping("/{id}")
    public Result<Dish> detail(@PathVariable Integer id, HttpServletRequest request) {
        userSession.requireUser(request);
        return Result.ok(dishService.getById(id));
    }

    @PostMapping
    public Result<Dish> create(@RequestBody Dish dish, HttpServletRequest request) {
        requireMerchantAccess(dish.getWindowId(), request);
        dishService.create(dish);
        return Result.ok(dish);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Dish dish, HttpServletRequest request) {
        Dish existing = dishService.getById(id);
        if (existing == null) {
            return Result.fail(404, "菜品不存在");
        }
        requireMerchantAccess(existing.getWindowId(), request);
        if (dish.getWindowId() != null && !existing.getWindowId().equals(dish.getWindowId())) {
            requireMerchantAccess(dish.getWindowId(), request);
        }
        dish.setId(id);
        dishService.update(dish);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id, HttpServletRequest request) {
        Dish existing = dishService.getById(id);
        if (existing == null) {
            return Result.fail(404, "菜品不存在");
        }
        requireMerchantAccess(existing.getWindowId(), request);
        dishService.delete(id);
        return Result.ok();
    }

    /**
     * 调整库存：delta 正数补货，负数扣减
     */
    @PostMapping("/{id}/stock")
    public Result<Void> changeStock(@PathVariable Integer id, @RequestParam Integer delta,
                                    HttpServletRequest request) {
        Dish existing = dishService.getById(id);
        if (existing == null) {
            return Result.fail(404, "菜品不存在");
        }
        requireMerchantAccess(existing.getWindowId(), request);
        dishService.changeStock(id, delta);
        return Result.ok();
    }

    @GetMapping("/rank/top")
    public Result<List<Dish>> topSales(@RequestParam(defaultValue = "10") int limit,
                                       HttpServletRequest request) {
        userSession.requireUser(request);
        return Result.ok(dishService.topSales(limit));
    }

    private void requireMerchantAccess(Integer windowId, HttpServletRequest request) {
        User current = userSession.requireUser(request);
        if ("admin".equals(current.getRole())) {
            return;
        }
        userSession.requireRole(request, "merchant");
        boolean ownsWindow = canteenService.listWindowsByMerchant(current.getId()).stream()
                .anyMatch(window -> window.getId().equals(windowId));
        if (!ownsWindow) {
            throw new AccessDeniedException("无权管理该窗口的菜品");
        }
    }
}
