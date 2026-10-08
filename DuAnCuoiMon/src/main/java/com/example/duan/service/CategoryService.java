
package com.example.duan.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.duan.entity.Category;
import com.example.duan.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Constructor
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // 1. Lay tat ca danh muc
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 2. Tim danh muc theo ID
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    // 3. Them hoac cap nhat danh muc
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    // 4. Xoa danh muc theo ID
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
