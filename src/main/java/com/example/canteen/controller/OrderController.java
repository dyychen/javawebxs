package com.example.canteen.controller;

import com.example.canteen.common.Result;
import com.example.canteen.common.UserSession;
import com.example.canteen.entity.Order;
import com.example.canteen.entity.User;
import com.example.canteen.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
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

    private final UserSession userSession;

    public OrderController(OrderService orderService, UserSession userSession) {
        this.orderService = orderService;
        this.userSession = userSession;
    }

    /**
     * 从购物车下单（按窗口拆分订单），返回生成的订单列表
     */
    @PostMapping
    public Result<List<Order>> create(@RequestBody Map<String, Object> body,
                                      HttpServletRequest request) {
        User current = userSession.getUser(request);
        if (current == null) {
            return Result.fail(401, "请先登录");
        }
        String pickupTime = body.get("pickupTime") == null ? null : body.get("pickupTime").toString();
        String remark = body.get("remark") == null ? null : body.get("remark").toString();
        return Result.ok(orderService.createFromCart(current.getId(), pickupTime, remark));
    }

    /** 模拟支付 */
    @PostMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Integer id, HttpServletRequest request) {
        if (userSession.getUser(request) == null) {
            return Result.fail(401, "请先登录");
        }
        orderService.pay(id);
        return Result.ok();
    }

    /** 取消订单 */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Integer id, HttpServletRequest request) {
        if (userSession.getUser(request) == null) {
            return Result.fail(401, "请先登录");
        }
        orderService.cancel(id);
        return Result.ok();
    }

    /** 商家核验取餐码 */
    @PostMapping("/{id}/verify")
    public Result<Void> verify(@PathVariable Integer id, @RequestBody Map<String, String> body,
                               HttpServletRequest request) {
        if (userSession.getUser(request) == null) {
            return Result.fail(401, "请先登录");
        }
        orderService.verifyPickup(id, body.get("pickupCode"));
        return Result.ok();
    }

    /** 当前学生的订单 */
    @GetMapping("/my")
    public Result<List<Order>> my(@RequestParam(required = false) Integer userId,
                                  HttpServletRequest request) {
        User current = userSession.getUser(request);
        if (current == null) {
            return Result.fail(401, "请先登录");
        }
        return Result.ok(orderService.listForUser(current.getId()));
    }

    /**
     * 管理端订单查询：管理员看全部，商家看自己窗口的订单
     */
    @GetMapping
    public Result<List<Order>> list(@RequestParam(required = false) String role,
                                    @RequestParam(required = false) Integer userId,
                                    HttpServletRequest request) {
        User current = userSession.getUser(request);
        if (current == null) {
            return Result.fail(401, "请先登录");
        }
        String effectiveRole = role != null ? role : (current != null ? current.getRole() : null);
        Integer effectiveUserId = userId != null ? userId : (current != null ? current.getId() : null);
        if ("admin".equals(effectiveRole)) {
            return Result.ok(orderService.listAll());
        }
        if ("merchant".equals(effectiveRole) && effectiveUserId != null) {
            return Result.ok(orderService.listForMerchant(effectiveUserId));
        }
        if (effectiveUserId != null) {
            return Result.ok(orderService.listForUser(effectiveUserId));
        }
        return Result.ok(orderService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Integer id) {
        return Result.ok(orderService.detail(id));
    }
}
