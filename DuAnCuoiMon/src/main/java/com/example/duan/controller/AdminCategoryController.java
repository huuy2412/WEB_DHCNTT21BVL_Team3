package com.example.duan.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.duan.entity.Category;
import com.example.duan.service.CategoryService;
import com.example.duan.repository.ProductRepository;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminCategoryController {

	private final CategoryService categoryService;
	private final ProductRepository productRepository;

	// Constructor
	public AdminCategoryController(CategoryService categoryService, ProductRepository productRepository) {
		this.categoryService = categoryService;
		this.productRepository = productRepository;
	}

	// Hien thi trang quan ly danh muc
	@GetMapping("/admin/categories")
	public String categories(Model model) {

		// Lay danh sach danh muc tu MySQL
		List<Category> categories = categoryService.getAllCategories();

		// Dem tong so danh muc
		long totalCategories = categoryService.getTotalCategories();

		// Dem tong so mon an
		long totalProducts = productRepository.count();

		// Tao Map luu so mon cua tung danh muc
		Map<Long, Long> productCounts = new HashMap<>();

		// Mac dinh moi danh muc co 0 mon
		for (Category category : categories) {
			productCounts.put(category.getId(), 0L);
		}

		// Lay so mon thuc te tu MySQL
		List<Object[]> results = productRepository.countProductsByCategory();

		// Cap nhat so mon cho tung danh muc
		for (Object[] row : results) {
			Long categoryId = ((Number) row[0]).longValue();
			Long count = ((Number) row[1]).longValue();
			productCounts.put(categoryId, count);
		}

		// Dem so danh muc co mon
		long categoriesWithProducts = productCounts.values().stream().filter(count -> count > 0).count();

		// Dem so danh muc trong
		long emptyCategories = totalCategories - categoriesWithProducts;

		// Truyen du lieu sang HTML
		model.addAttribute("categories", categories);
		model.addAttribute("totalCategories", totalCategories);
		model.addAttribute("totalProducts", totalProducts);
		// Truyen so mon cua tung danh muc
		model.addAttribute("productCounts", productCounts);
		// Truyen so danh muc co mon
		model.addAttribute("categoriesWithProducts", categoriesWithProducts);
		// Truyen so danh muc trong
		model.addAttribute("emptyCategories", emptyCategories);

		// Mo giao dien quan ly danh muc
		return "admin/categories";
	}

	// ========================================
	// THEM DANH MUC MOI
	// ========================================
	@PostMapping("/admin/categories/add")
	public String addCategory(@RequestParam("name") String name,
			@RequestParam(value = "description", defaultValue = "") String description,
			RedirectAttributes redirectAttributes) {
		try {
			// Goi Service de them danh muc vao MySQL
			categoryService.createCategory(name, description);
			// Thong bao them thanh cong
			redirectAttributes.addFlashAttribute("success", "Thêm danh mục thành công!");
		} catch (IllegalArgumentException e) {
			// Thong bao neu ten rong hoac bi trung
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		// Quay ve trang quan ly danh muc
		return "redirect:/admin/categories";
	}

}
