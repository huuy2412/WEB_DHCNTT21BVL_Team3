package com.example.duan.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.duan.repository.OrderItemRepository;
import com.example.duan.entity.Product;
import com.example.duan.repository.ProductRepository;
import com.example.duan.entity.Category;
import com.example.duan.repository.CategoryRepository;

@Service
public class ProductService {
	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final OrderItemRepository orderItemRepository;

	public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
			OrderItemRepository orderItemRepository) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.orderItemRepository = orderItemRepository;
	}

	// lấy danh sách 12 món đưa về file HOME
	public List<Product> getHomeProducts() {
		return productRepository.findTop12ByAvailableTrueOrderByIdDesc();
	}

	// ========================================
	// THEM MON AN MOI
	// ========================================
	public Product createProduct(String name, Double price, String description, String image, boolean available,
			Long categoryId) {

		// 1. Kiem tra ten mon an
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Vui lòng nhập tên món ăn!");
		}

		name = name.trim();

		if (name.length() > 255) {
			throw new IllegalArgumentException("Tên món ăn không được quá 255 ký tự!");
		}

		// 2. Kiem tra gia ban
		if (price == null || !Double.isFinite(price) || price <= 0) {
			throw new IllegalArgumentException("Giá bán phải lớn hơn 0!");
		}

		// 3. Kiem tra danh muc
		if (categoryId == null) {
			throw new IllegalArgumentException("Vui lòng chọn danh mục!");
		}

		// 4. Tim danh muc trong MySQL
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại!"));

		// 5. Xu ly mo ta
		if (description == null) {
			description = "";
		}

		description = description.trim();

		if (description.length() > 255) {
			throw new IllegalArgumentException("Mô tả không được quá 255 ký tự!");
		}

		// 6. Xu ly ten file hinh anh
		if (image == null) {
			image = "";
		}

		image = image.trim();

		if (image.length() > 255) {
			throw new IllegalArgumentException("Đường dẫn hình ảnh quá dài!");
		}

		// 7. Tao doi tuong Product moi
		Product product = new Product();

		// 8. Gan thong tin mon an
		product.setName(name);
		product.setPrice(price);
		product.setDescription(description);
		product.setImage(image);
		product.setAvailable(available);
		product.setCategory(category);

		// 9. Luu vao MySQL
		return productRepository.save(product);
	}

	// ========================================
	// CAP NHAT THONG TIN MON AN
	// ========================================
	public Product updateProduct(Long id, String name, Double price, String description, String image,
			boolean available, Long categoryId) {

		// 1. Tim mon an can sua
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món ăn cần sửa!"));

		// 2. Kiem tra ten mon an
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Vui lòng nhập tên món ăn!");
		}

		name = name.trim();

		if (name.length() > 255) {
			throw new IllegalArgumentException("Tên món ăn không được quá 255 ký tự!");
		}

		// 3. Kiem tra gia ban
		if (price == null || !Double.isFinite(price) || price <= 0) {
			throw new IllegalArgumentException("Giá bán phải lớn hơn 0!");
		}

		// 4. Kiem tra danh muc
		if (categoryId == null) {
			throw new IllegalArgumentException("Vui lòng chọn danh mục!");
		}

		// Tim danh muc trong MySQL
		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại!"));

		// 5. Xu ly mo ta
		if (description == null) {
			description = "";
		}

		description = description.trim();

		if (description.length() > 255) {
			throw new IllegalArgumentException("Mô tả không được quá 255 ký tự!");
		}

		// 6. Cap nhat thong tin mon an
		product.setName(name);
		product.setPrice(price);
		product.setDescription(description);
		product.setCategory(category);
		product.setAvailable(available);

		// 7. Cap nhat hinh anh neu co anh moi
		if (image != null && !image.trim().isEmpty()) {
			image = image.trim();
			if (image.length() > 255) {
				throw new IllegalArgumentException("Đường dẫn hình ảnh quá dài!");
			}
			product.setImage(image);
		}

		// 8. Luu thay doi vao MySQL
		return productRepository.save(product);
	}

	// ========================================
	// XOA MON AN
	// ========================================
	public void deleteProduct(Long id) {

		// 1. Kiem tra ID mon an
		if (id == null) {
			throw new IllegalArgumentException("ID món ăn không hợp lệ!");
		}

		// 2. Tim mon an trong MySQL
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món ăn cần xóa!"));

		// 3. Kiem tra mon an da co trong don hang chua
		long orderItemCount = orderItemRepository.countByProduct_Id(id);

		// 4. Neu mon da co trong don hang thi khong xoa
		if (orderItemCount > 0) {

			throw new IllegalArgumentException(
					"Không thể xóa món ăn này vì đã có " + orderItemCount + " chi tiết đơn hàng liên quan!");
		}

		// 5. Mon chua co trong don hang thi cho phep xoa
		productRepository.delete(product);
	}

}
