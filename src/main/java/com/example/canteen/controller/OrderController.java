package com.example.canteen.controller;

import com.example.canteen.common.Result;
import com.example.canteen.common.AccessDeniedException;
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
        User current = userSession.requireUser(request);
        Order order = orderService.detail(id);
        requireOrderAccess(order, current, "支付");
        orderService.pay(id);
        return Result.ok();
    }

    /** 取消订单 */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Integer id, HttpServletRequest request) {
        User current = userSession.requireUser(request);
        Order order = orderService.detail(id);
        requireOrderAccess(order, current, "取消");
        orderService.cancel(id);
        return Result.ok();
    }

    /** 商家核验取餐码 */
    @PostMapping("/{id}/verify")
    public Result<Void> verify(@PathVariable Integer id, @RequestBody Map<String, String> body,
                               HttpServletRequest request) {
        User current = userSession.requireUser(request);
        if ("merchant".equals(current.getRole())) {
            List<Order> merchantOrders = orderService.listForMerchant(current.getId());
            boolean ownsWindow = merchantOrders.stream()
                    .anyMatch(item -> item.getId().equals(id));
            if (!ownsWindow) {
                throw new AccessDeniedException("无权核验该窗口的订单");
            }
        } else if (!"admin".equals(current.getRole())) {
            throw new AccessDeniedException("仅商家或管理员可核验订单");
        }
        orderService.verifyPickup(id, body.get("pickupCode"));
        return Result.ok();
    }

    /** 当前学生的订单 */
    @GetMapping("/my")
    public Result<List<Order>> my(HttpServletRequest request) {
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
    public Result<List<Order>> list(HttpServletRequest request) {
        User current = userSession.getUser(request);
        if (current == null) {
            return Result.fail(401, "请先登录");
        }
        if ("admin".equals(current.getRole())) {
            return Result.ok(orderService.listAll());
        }
        if ("merchant".equals(current.getRole())) {
            return Result.ok(orderService.listForMerchant(current.getId()));
        }
        return Result.ok(orderService.listForUser(current.getId()));
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Integer id, HttpServletRequest request) {
        User current = userSession.requireUser(request);
        Order order = orderService.detail(id);
        requireOrderAccess(order, current, "查看");
        return Result.ok(order);
    }

    private void requireOrderAccess(Order order, User current, String action) {
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if ("admin".equals(current.getRole())) {
            return;
        }
        if (current.getId().equals(order.getUserId())) {
            return;
        }
        if ("merchant".equals(current.getRole())) {
            List<Order> merchantOrders = orderService.listForMerchant(current.getId());
            boolean ownsWindow = merchantOrders.stream()
                .anyMatch(item -> item.getId().equals(order.getId()));
            if (ownsWindow) {
                return;
            }
        }
        throw new AccessDeniedException("无权" + action + "该订单");
    }
}
