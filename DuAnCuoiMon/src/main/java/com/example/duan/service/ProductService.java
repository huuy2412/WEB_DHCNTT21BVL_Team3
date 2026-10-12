package com.example.duan.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.duan.entity.Product;
import com.example.duan.repository.ProductRepository;

@Service
public class ProductService {
	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}
	
	// lấy danh sách 12 món đưa về file HOME
	public List<Product> getHomeProducts(){
		return productRepository.findTop12ByAvailableTrueOrderByIdDesc();
	}
}
