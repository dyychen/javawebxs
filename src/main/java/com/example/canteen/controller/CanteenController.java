package com.example.canteen.controller;

import com.example.canteen.common.Result;
import com.example.canteen.entity.CanteenWindow;
import com.example.canteen.entity.Category;
import com.example.canteen.service.CanteenService;
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
 * 食堂窗口与菜品分类管理
 */
@RestController
@RequestMapping("/api")
public class CanteenController {

    private final CanteenService canteenService;

    public CanteenController(CanteenService canteenService) {
        this.canteenService = canteenService;
    }

    @GetMapping("/windows")
    public Result<List<CanteenWindow>> windows() {
        return Result.ok(canteenService.listWindows());
    }

    @GetMapping("/windows/merchant")
    public Result<List<CanteenWindow>> windowsByMerchant(@RequestParam Integer merchantId) {
        return Result.ok(canteenService.listWindowsByMerchant(merchantId));
    }

    @PostMapping("/windows")
    public Result<CanteenWindow> createWindow(@RequestBody CanteenWindow window) {
        canteenService.createWindow(window);
        return Result.ok(window);
    }

    @PutMapping("/windows/{id}")
    public Result<Void> updateWindow(@PathVariable Integer id, @RequestBody CanteenWindow window) {
        window.setId(id);
        canteenService.updateWindow(window);
        return Result.ok();
    }

    @DeleteMapping("/windows/{id}")
    public Result<Void> deleteWindow(@PathVariable Integer id) {
        canteenService.deleteWindow(id);
        return Result.ok();
    }

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.ok(canteenService.listCategories());
    }

    @PostMapping("/categories")
    public Result<Category> createCategory(@RequestBody Category category) {
        canteenService.createCategory(category);
        return Result.ok(category);
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Integer id) {
        canteenService.deleteCategory(id);
        return Result.ok();
    }
}
