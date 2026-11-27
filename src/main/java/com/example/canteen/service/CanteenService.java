package com.example.canteen.service;

import com.example.canteen.entity.CanteenWindow;
import com.example.canteen.entity.Category;
import com.example.canteen.mapper.CanteenMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CanteenService {

    private final CanteenMapper canteenMapper;

    public CanteenService(CanteenMapper canteenMapper) {
        this.canteenMapper = canteenMapper;
    }

    public List<CanteenWindow> listWindows() {
        return canteenMapper.selectWindows();
    }

    public List<CanteenWindow> listWindowsByMerchant(Integer merchantId) {
        return canteenMapper.selectWindowsByMerchant(merchantId);
    }

    public CanteenWindow getWindow(Integer id) {
        return canteenMapper.selectWindowById(id);
    }

    public void createWindow(CanteenWindow window) {
        if (window.getStatus() == null) {
            window.setStatus(1);
        }
        canteenMapper.insertWindow(window);
    }

    public void updateWindow(CanteenWindow window) {
        canteenMapper.updateWindow(window);
    }

    public void deleteWindow(Integer id) {
        canteenMapper.deleteWindow(id);
    }

    public List<Category> listCategories() {
        return canteenMapper.selectCategories();
    }

    public void createCategory(Category category) {
        canteenMapper.insertCategory(category);
    }

    public void deleteCategory(Integer id) {
        canteenMapper.deleteCategory(id);
    }
}
