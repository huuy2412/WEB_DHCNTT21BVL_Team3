package com.example.duan.controller;

import com.example.duan.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.duan.repository.CategoryRepository;
import org.springframework.ui.Model;

@Controller
public class CategoryController {
	private final CategoryService categoryService;
	private final CategoryRepository categoryRepository;

	public CategoryController(CategoryRepository categoryRepository, CategoryService categoryService) {
		this.categoryRepository = categoryRepository;
		this.categoryService = categoryService;
	}
	
	// hiển thị danh sách
	@GetMapping("/categories")
	public String listCategories(Model model) {
		model.addAttribute("categpries", categoryService.getAllCategories());
		return "category/list";
	}
}
