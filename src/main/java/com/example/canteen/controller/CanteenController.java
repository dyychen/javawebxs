package com.example.canteen.controller;

import com.example.canteen.common.Result;
import com.example.canteen.common.UserSession;
import com.example.canteen.entity.CanteenWindow;
import com.example.canteen.entity.Category;
import com.example.canteen.entity.User;
import com.example.canteen.service.CanteenService;
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
 * 食堂窗口与菜品分类管理
 */
@RestController
@RequestMapping("/api")
public class CanteenController {

    private final CanteenService canteenService;
    private final UserSession userSession;

    public CanteenController(CanteenService canteenService, UserSession userSession) {
        this.canteenService = canteenService;
        this.userSession = userSession;
    }

    @GetMapping("/windows")
    public Result<List<CanteenWindow>> windows(HttpServletRequest request) {
        userSession.requireUser(request);
        return Result.ok(canteenService.listWindows());
    }

    @GetMapping("/windows/merchant")
    public Result<List<CanteenWindow>> windowsByMerchant(@RequestParam(required = false) Integer merchantId,
                                                         HttpServletRequest request) {
        User current = userSession.requireUser(request);
        if ("admin".equals(current.getRole())) {
            return Result.ok(merchantId == null
                    ? canteenService.listWindows()
                    : canteenService.listWindowsByMerchant(merchantId));
        }
        userSession.requireRole(request, "merchant");
        return Result.ok(canteenService.listWindowsByMerchant(current.getId()));
    }

    @PostMapping("/windows")
    public Result<CanteenWindow> createWindow(@RequestBody CanteenWindow window, HttpServletRequest request) {
        userSession.requireRole(request, "admin");
        canteenService.createWindow(window);
        return Result.ok(window);
    }

    @PutMapping("/windows/{id}")
    public Result<Void> updateWindow(@PathVariable Integer id, @RequestBody CanteenWindow window,
                                     HttpServletRequest request) {
        userSession.requireRole(request, "admin");
        if (canteenService.getWindow(id) == null) {
            return Result.fail(404, "窗口不存在");
        }
        window.setId(id);
        canteenService.updateWindow(window);
        return Result.ok();
    }

    @DeleteMapping("/windows/{id}")
    public Result<Void> deleteWindow(@PathVariable Integer id, HttpServletRequest request) {
        userSession.requireRole(request, "admin");
        if (canteenService.getWindow(id) == null) {
            return Result.fail(404, "窗口不存在");
        }
        canteenService.deleteWindow(id);
        return Result.ok();
    }

    @GetMapping("/categories")
    public Result<List<Category>> categories(HttpServletRequest request) {
        userSession.requireUser(request);
        return Result.ok(canteenService.listCategories());
    }

    @PostMapping("/categories")
    public Result<Category> createCategory(@RequestBody Category category, HttpServletRequest request) {
        userSession.requireRole(request, "admin");
        canteenService.createCategory(category);
        return Result.ok(category);
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Integer id, HttpServletRequest request) {
        userSession.requireRole(request, "admin");
        Category category = canteenService.listCategories().stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (category == null) {
            return Result.fail(404, "分类不存在");
        }
        Object dishCount = category.getDishCount();
        int count = dishCount instanceof Number ? ((Number) dishCount).intValue() : 0;
        if (count > 0) {
            return Result.fail(400, "该分类下仍有菜品，无法删除");
        }
        canteenService.deleteCategory(id);
        return Result.ok();
    }
}
