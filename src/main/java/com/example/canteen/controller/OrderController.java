package com.example.canteen.controller;

import com.example.canteen.common.Result;
import com.example.canteen.entity.Order;
import com.example.canteen.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 订单：下单、支付模拟、取消、取餐码核验、订单查询
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 从购物车下单（按窗口拆分订单），返回生成的订单列表
     */
    @PostMapping
    public Result<List<Order>> create(@RequestBody Map<String, Object> body) {
        Integer userId = Integer.valueOf(body.get("userId").toString());
        String pickupTime = body.get("pickupTime") == null ? null : body.get("pickupTime").toString();
        String remark = body.get("remark") == null ? null : body.get("remark").toString();
        return Result.ok(orderService.createFromCart(userId, pickupTime, remark));
    }

    /** 模拟支付 */
    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Integer id) {
        orderService.pay(id);
        return Result.ok();
    }

    /** 取消订单 */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Integer id) {
        orderService.cancel(id);
        return Result.ok();
    }

    /** 商家核验取餐码 */
    @PostMapping("/{id}/verify")
    public Result<Void> verify(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        orderService.verifyPickup(id, body.get("pickupCode"));
        return Result.ok();
    }

    /** 当前学生的订单 */
    @GetMapping("/my")
    public Result<List<Order>> my(@RequestParam Integer userId) {
        return Result.ok(orderService.listForUser(userId));
    }

    /**
     * 管理端订单查询：管理员看全部，商家看自己窗口的订单
     */
    @GetMapping
    public Result<List<Order>> list(@RequestParam(required = false) String role,
                                    @RequestParam(required = false) Integer userId) {
        if ("admin".equals(role)) {
            return Result.ok(orderService.listAll());
        }
        if ("merchant".equals(role) && userId != null) {
            return Result.ok(orderService.listForMerchant(userId));
        }
        if (userId != null) {
            return Result.ok(orderService.listForUser(userId));
        }
        return Result.ok(orderService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Integer id) {
        return Result.ok(orderService.detail(id));
    }
}
