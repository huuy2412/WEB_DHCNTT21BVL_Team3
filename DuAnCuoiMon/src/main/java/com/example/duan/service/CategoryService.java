package com.example.duan.service;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.duan.entity.Category;
import com.example.duan.repository.CategoryRepository;
import com.example.duan.repository.ProductRepository;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;

	// Constructor
	public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
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

	// ========================================
	// CAP NHAT DANH MUC
	// ========================================
	public Category updateCategory(Long id, String name, String description) {
		// Tim danh muc theo ID trong MySQL
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục!"));

		// Kiem tra ten danh muc
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Vui lòng nhập tên danh mục!");
		}

		name = name.trim();

		// Kiem tra do dai
		if (name.length() > 255) {
			throw new IllegalArgumentException("Tên danh mục quá dài!");
		}

		// Kiem tra ten trung voi danh muc KHAC
		for (Category c : categoryRepository.findAll()) {
			if (!c.getId().equals(id) && c.getName() != null && c.getName().trim().equalsIgnoreCase(name)) {
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

		// Cap nhat du lieu moi
		category.setName(name);
		category.setDescription(description);

		// Luu thay doi vao MySQL
		return categoryRepository.save(category);
	}

	// ========================================
	// XOA DANH MUC
	// ========================================
	public void deleteCategory(Long id) {

		// Kiem tra danh muc co ton tai khong
		Category category = categoryRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục cần xóa!"));

		// Dem so mon an trong danh muc
		long productCount = productRepository.countByCategory_Id(id);

		// Neu danh muc co mon an thi khong cho xoa
		if (productCount > 0) {
			throw new IllegalArgumentException("Không thể xóa! Danh mục đang có " + productCount + " món ăn.");
		}

		// Danh muc trong thi cho phep xoa
		categoryRepository.delete(category);
	}

}
