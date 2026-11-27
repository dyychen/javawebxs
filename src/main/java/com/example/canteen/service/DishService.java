package com.example.canteen.service;

import com.example.canteen.entity.Dish;
import com.example.canteen.mapper.DishMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishService {

    private final DishMapper dishMapper;

    public DishService(DishMapper dishMapper) {
        this.dishMapper = dishMapper;
    }

    public List<Dish> list(Integer windowId, Integer categoryId, String keyword, Integer merchantId) {
        return dishMapper.selectList(windowId, categoryId, keyword, merchantId);
    }

    public Dish getById(Integer id) {
        return dishMapper.selectById(id);
    }

    public void create(Dish dish) {
        if (dish.getStatus() == null) {
            dish.setStatus(1);
        }
        if (dish.getStock() == null) {
            dish.setStock(0);
        }
        dishMapper.insert(dish);
    }

    public void update(Dish dish) {
        dishMapper.update(dish);
    }

    public void delete(Integer id) {
        dishMapper.delete(id);
    }

    public void changeStock(Integer id, Integer delta) {
        dishMapper.updateStock(id, delta);
    }

    public List<Dish> topSales(int limit) {
        return dishMapper.selectTopSales(limit);
    }
}
