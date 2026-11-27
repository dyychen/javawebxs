package com.example.canteen.service;

import com.example.canteen.entity.CartItem;
import com.example.canteen.entity.Dish;
import com.example.canteen.entity.Order;
import com.example.canteen.entity.OrderItem;
import com.example.canteen.mapper.CartMapper;
import com.example.canteen.mapper.DishMapper;
import com.example.canteen.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final CartMapper cartMapper;
    private final DishMapper dishMapper;

    public OrderService(OrderMapper orderMapper, CartMapper cartMapper, DishMapper dishMapper) {
        this.orderMapper = orderMapper;
        this.cartMapper = cartMapper;
        this.dishMapper = dishMapper;
    }

    /**
     * 从购物车下单：按窗口分组生成订单，扣库存、累计销量、清空购物车
     */
    @Transactional
    public List<Order> createFromCart(Integer userId, String pickupTime, String remark) {
        List<CartItem> cartItems = cartMapper.selectByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("购物车为空，无法下单");
        }

        // 按窗口分组，一个窗口生成一个订单
        Map<Integer, List<CartItem>> groupByWindow = cartItems.stream()
                .collect(Collectors.groupingBy(CartItem::getWindowId, LinkedHashMap::new, Collectors.toList()));

        List<Order> created = new ArrayList<>();
        for (Map.Entry<Integer, List<CartItem>> entry : groupByWindow.entrySet()) {
            Integer windowId = entry.getKey();
            List<CartItem> items = entry.getValue();

            BigDecimal amount = BigDecimal.ZERO;
            for (CartItem item : items) {
                Dish dish = dishMapper.selectById(item.getDishId());
                if (dish == null || dish.getStatus() != 1) {
                    throw new IllegalArgumentException("菜品不存在或已下架：" + item.getDishName());
                }
                if (dish.getStock() < item.getQuantity()) {
                    throw new IllegalArgumentException("库存不足：" + dish.getName() + "，剩余 " + dish.getStock());
                }
                amount = amount.add(dish.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setWindowId(windowId);
            order.setAmount(amount);
            order.setStatus("PENDING_PAY");
            order.setPickupCode(generatePickupCode());
            order.setPickupTime(pickupTime);
            order.setRemark(remark);
            orderMapper.insert(order);

            for (CartItem item : items) {
                Dish dish = dishMapper.selectById(item.getDishId());
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setDishId(item.getDishId());
                orderItem.setDishName(dish.getName());
                orderItem.setPrice(dish.getPrice());
                orderItem.setQuantity(item.getQuantity());
                orderMapper.insertItem(orderItem);

                dishMapper.updateStock(dish.getId(), -item.getQuantity());
                dishMapper.increaseSales(dish.getId(), item.getQuantity());
            }
            created.add(order);
        }

        cartMapper.deleteByUserId(userId);
        return created;
    }

    /** 模拟支付 */
    public void pay(Integer orderId) {
        Order order = mustGet(orderId);
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new IllegalArgumentException("订单状态不是待支付，无法支付");
        }
        orderMapper.updatePay(orderId);
    }

    /** 取消订单（仅待支付可取消），并回补库存 */
    @Transactional
    public void cancel(Integer orderId) {
        Order order = mustGet(orderId);
        if (!"PENDING_PAY".equals(order.getStatus())) {
            throw new IllegalArgumentException("订单已支付或已完成，无法取消");
        }
        List<OrderItem> items = orderMapper.selectItems(orderId);
        for (OrderItem item : items) {
            dishMapper.updateStock(item.getDishId(), item.getQuantity());
            dishMapper.increaseSales(item.getDishId(), -item.getQuantity());
        }
        orderMapper.updateStatus(orderId, "CANCELLED");
    }

    /** 商家核验取餐码，完成取餐 */
    public void verifyPickup(Integer orderId, String pickupCode) {
        Order order = mustGet(orderId);
        if (!"PAID".equals(order.getStatus())) {
            throw new IllegalArgumentException("订单尚未支付，不能核验");
        }
        if (pickupCode == null || !pickupCode.equals(order.getPickupCode())) {
            throw new IllegalArgumentException("取餐码不正确");
        }
        orderMapper.updateStatus(orderId, "PICKED");
    }

    public Order detail(Integer orderId) {
        Order order = mustGet(orderId);
        order.setItems(orderMapper.selectItems(orderId));
        return order;
    }

    public List<Order> listForUser(Integer userId) {
        return withItems(orderMapper.selectByUserId(userId));
    }

    public List<Order> listForMerchant(Integer merchantId) {
        return withItems(orderMapper.selectByMerchantId(merchantId));
    }

    public List<Order> listAll() {
        return withItems(orderMapper.selectAll());
    }

    /**
     * 营业统计：订单数、营业收入、销量排行、热门菜品
     */
    public Map<String, Object> statistics() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalOrders", orderMapper.countAll());
        map.put("pendingPay", orderMapper.countByStatus("PENDING_PAY"));
        map.put("paid", orderMapper.countByStatus("PAID"));
        map.put("picked", orderMapper.countByStatus("PICKED"));
        map.put("cancelled", orderMapper.countByStatus("CANCELLED"));
        map.put("revenue", orderMapper.sumPaidAmount());
        map.put("salesRanking", orderMapper.selectSalesRanking());
        return map;
    }

    private List<Order> withItems(List<Order> orders) {
        for (Order order : orders) {
            order.setItems(orderMapper.selectItems(order.getId()));
        }
        return orders;
    }

    private Order mustGet(Integer orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在：" + orderId);
        }
        return order;
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(100, 999);
        return "C" + time + random;
    }

    private String generatePickupCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1000000));
    }
}
