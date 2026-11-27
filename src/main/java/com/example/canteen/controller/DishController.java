package com.example.canteen.controller;

import com.example.canteen.common.Result;
import com.example.canteen.entity.Dish;
import com.example.canteen.service.DishService;
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

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping
    public Result<List<Dish>> list(@RequestParam(required = false) Integer windowId,
                                   @RequestParam(required = false) Integer categoryId,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer merchantId) {
        return Result.ok(dishService.list(windowId, categoryId, keyword, merchantId));
    }

    @GetMapping("/{id}")
    public Result<Dish> detail(@PathVariable Integer id) {
        return Result.ok(dishService.getById(id));
    }

    @PostMapping
    public Result<Dish> create(@RequestBody Dish dish) {
        dishService.create(dish);
        return Result.ok(dish);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Dish dish) {
        dish.setId(id);
        dishService.update(dish);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        dishService.delete(id);
        return Result.ok();
    }

    /**
     * 调整库存：delta 正数补货，负数扣减
     */
    @PostMapping("/{id}/stock")
    public Result<Void> changeStock(@PathVariable Integer id, @RequestParam Integer delta) {
        dishService.changeStock(id, delta);
        return Result.ok();
    }

    @GetMapping("/rank/top")
    public Result<List<Dish>> topSales(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(dishService.topSales(limit));
    }
}
