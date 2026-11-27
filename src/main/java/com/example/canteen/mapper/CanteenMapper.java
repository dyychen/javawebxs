package com.example.canteen.mapper;

import com.example.canteen.entity.CanteenWindow;
import com.example.canteen.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CanteenMapper {

    List<CanteenWindow> selectWindows();

    CanteenWindow selectWindowById(@Param("id") Integer id);

    List<CanteenWindow> selectWindowsByMerchant(@Param("merchantId") Integer merchantId);

    int insertWindow(CanteenWindow window);

    int updateWindow(CanteenWindow window);

    int deleteWindow(@Param("id") Integer id);

    List<Category> selectCategories();

    int insertCategory(Category category);

    int deleteCategory(@Param("id") Integer id);
}
