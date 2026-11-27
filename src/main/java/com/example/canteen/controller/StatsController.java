package com.example.canteen.controller;

import com.example.canteen.common.Result;
import com.example.canteen.common.UserSession;
import com.example.canteen.entity.Order;
import com.example.canteen.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 营业统计与数据导出
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final OrderService orderService;
    private final UserSession userSession;

    public StatsController(OrderService orderService, UserSession userSession) {
        this.orderService = orderService;
        this.userSession = userSession;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(HttpServletRequest request) {
        userSession.requireRole(request, "admin", "merchant");
        return Result.ok(orderService.statistics());
    }

    /**
     * 导出订单 CSV
     */
    @GetMapping("/export")
    public Result<String> export(HttpServletRequest request) {
        userSession.requireRole(request, "admin");
        List<Order> orders = orderService.listAll();
        StringBuilder sb = new StringBuilder();
        sb.append("订单号,编号,学生,窗口,金额,状态,取餐码,预约取餐时间,下单时间\n");
        for (Order o : orders) {
            sb.append(o.getOrderNo()).append(',')
                    .append(o.getId()).append(',')
                    .append(nullToEmpty(o.getUserName())).append(',')
                    .append(nullToEmpty(o.getWindowName())).append(',')
                    .append(o.getAmount()).append(',')
                    .append(statusText(o.getStatus())).append(',')
                    .append(nullToEmpty(o.getPickupCode())).append(',')
                    .append(nullToEmpty(o.getPickupTime())).append(',')
                    .append(o.getCreateTime()).append('\n');
        }
        return Result.ok(sb.toString());
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String statusText(String status) {
        switch (status == null ? "" : status) {
            case "PENDING_PAY": return "待支付";
            case "PAID": return "待取餐";
            case "PICKED": return "已完成";
            case "CANCELLED": return "已取消";
            default: return status;
        }
    }
}
