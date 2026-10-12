package com.example.duan.controller;

import com.example.duan.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProductService productService;

	HomeController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping({"/", "/home"})
    public String home(Model model) {
		// lấy danh sách 12 món
    	model.addAttribute("products", productService.getHomeProducts());
        return "user/home";
    }
}
