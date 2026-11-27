package com.example.canteen.mapper;

import com.example.canteen.entity.Order;
import com.example.canteen.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OrderMapper {

    int insert(Order order);

    Order selectById(@Param("id") Integer id);

    List<Order> selectByUserId(@Param("userId") Integer userId);

    List<Order> selectAll();

    List<Order> selectByMerchantId(@Param("merchantId") Integer merchantId);

    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    int updatePay(@Param("id") Integer id, @Param("pickupCode") String pickupCode);

    int insertItem(OrderItem item);

    List<OrderItem> selectItems(@Param("orderId") Integer orderId);

    BigDecimal sumPaidAmount();

    Integer countByStatus(@Param("status") String status);

    Integer countAll();

    List<java.util.Map<String, Object>> selectSalesRanking();
}
