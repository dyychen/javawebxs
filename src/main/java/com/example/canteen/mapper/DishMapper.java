package com.example.canteen.mapper;

import com.example.canteen.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DishMapper {

    List<Dish> selectList(@Param("windowId") Integer windowId,
                          @Param("categoryId") Integer categoryId,
                          @Param("keyword") String keyword,
                          @Param("merchantId") Integer merchantId);

    Dish selectById(@Param("id") Integer id);

    int insert(Dish dish);

    int update(Dish dish);

    int delete(@Param("id") Integer id);

    int updateStock(@Param("id") Integer id, @Param("delta") Integer delta);

    int increaseSales(@Param("id") Integer id, @Param("count") Integer count);

    List<Dish> selectTopSales(@Param("limit") Integer limit);
}
