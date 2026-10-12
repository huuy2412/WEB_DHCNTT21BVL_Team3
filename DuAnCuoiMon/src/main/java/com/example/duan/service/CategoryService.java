package com.example.duan.service;

import java.util.List;
import org.springframework.data.domain.Sort;
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

	// Lay tat ca danh muc, sap xep theo ID
	public List<Category> getAllCategories() {
		return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
	}

	// Dem tong so danh muc
	public long getTotalCategories() {
		return categoryRepository.count();
	}

	// Tim danh muc theo ID
	public Category getCategoryById(Long id) {
		return categoryRepository.findById(id).orElse(null);
	}

	// Them hoac cap nhat danh muc
	public Category saveCategory(Category category) {
		return categoryRepository.save(category);
	}

	// Them danh muc moi
	public Category createCategory(String name, String description) {

		// Kiem tra ten danh muc co rong khong
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Vui lòng nhập tên danh mục!");
		}

		// Xoa khoang trang dau va cuoi
		name = name.trim();

		// Kiem tra do dai ten
		if (name.length() > 255) {
			throw new IllegalArgumentException("Tên danh mục quá dài!");
		}

		// Kiem tra ten danh muc da ton tai
		for (Category c : categoryRepository.findAll()) {
			if (c.getName() != null && c.getName().trim().equalsIgnoreCase(name)) {
				throw new IllegalArgumentException("Tên danh mục đã tồn tại!");
			}
		}

		// Xu ly mo ta
		if (description == null) {
			description = "";
		}

		description = description.trim();
		if (description.length() > 255) {
			throw new IllegalArgumentException("Mô tả không được quá 255 ký tự!");
		}

		// Tao doi tuong Category moi
		Category category = new Category();

		// Gan du lieu nguoi dung nhap
		category.setName(name);
		category.setDescription(description);

		// Luu vao MySQL
		return categoryRepository.save(category);
	}

}
