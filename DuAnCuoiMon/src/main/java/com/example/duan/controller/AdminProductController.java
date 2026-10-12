package com.example.duan.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.duan.entity.Category;
import com.example.duan.entity.Product;
import com.example.duan.repository.CategoryRepository;
import com.example.duan.repository.ProductRepository;
import com.example.duan.service.ProductService;

@Controller
public class AdminProductController {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;
	private final ProductService productService;

	// Thu muc luu hinh anh
	private static final Path UPLOAD_DIR = Path.of(System.getProperty("user.dir"), "uploads", "products")
			.toAbsolutePath().normalize();

	// Gioi han anh 5MB
	private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

	// Constructor
	public AdminProductController(ProductRepository productRepository, CategoryRepository categoryRepository,
			ProductService productService) {

		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
		this.productService = productService;
	}

	// ========================================
	// HIEN THI TRANG QUAN LY MON AN
	// ========================================
	@GetMapping("/admin/products")
	public String products(Model model) {

		List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

		List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));

		long totalProducts = productRepository.count();

		long availableProducts = productRepository.countByAvailableTrue();

		long unavailableProducts = productRepository.countByAvailableFalse();

		long totalCategories = categoryRepository.count();

		int year = Year.now().getValue();

		model.addAttribute("products", products);
		model.addAttribute("categories", categories);
		model.addAttribute("totalProducts", totalProducts);
		model.addAttribute("availableProducts", availableProducts);
		model.addAttribute("unavailableProducts", unavailableProducts);
		model.addAttribute("totalCategories", totalCategories);
		model.addAttribute("reportYear", year);

		return "admin/products";
	}

	// ========================================
	// THEM MON AN KEM HINH ANH
	// ========================================
	@PostMapping("/admin/products/add")
	public String addProduct(

			@RequestParam("name") String name,

			@RequestParam(value = "price", required = false) Double price,

			@RequestParam(value = "description", defaultValue = "") String description,

			@RequestParam(value = "photo", required = false) MultipartFile photo,

			@RequestParam(value = "available", defaultValue = "true") boolean available,

			@RequestParam(value = "categoryId", required = false) Long categoryId,

			RedirectAttributes redirectAttributes) {

		String imagePath = "";

		try {

			// Kiem tra du lieu truoc khi luu anh
			if (name == null || name.trim().isEmpty()) {
				throw new IllegalArgumentException("Vui lòng nhập tên món ăn!");
			}

			if (price == null || !Double.isFinite(price) || price <= 0) {
				throw new IllegalArgumentException("Giá bán phải lớn hơn 0!");
			}

			if (categoryId == null || !categoryRepository.existsById(categoryId)) {
				throw new IllegalArgumentException("Danh mục không hợp lệ!");
			}

			// Luu file anh vao server
			imagePath = savePhoto(photo);

			// Luu mon an va duong dan anh vao MySQL
			productService.createProduct(name, price, description, imagePath, available, categoryId);

			redirectAttributes.addFlashAttribute("success", "Thêm món ăn thành công!");

		} catch (IllegalArgumentException | IOException e) {

			deleteUploadedPhoto(imagePath);

			redirectAttributes.addFlashAttribute("error", e.getMessage());

		} catch (Exception e) {

			deleteUploadedPhoto(imagePath);

			redirectAttributes.addFlashAttribute("error", "Có lỗi khi lưu món ăn. Vui lòng kiểm tra lại!");
		}

		return "redirect:/admin/products";
	}

	// ========================================
	// SUA THONG TIN MON AN
	// ========================================
	@PostMapping("/admin/products/update")
	public String updateProduct(

			@RequestParam("id") Long id,

			@RequestParam("name") String name,

			@RequestParam(value = "price", required = false) Double price,

			@RequestParam(value = "description", defaultValue = "") String description,

			@RequestParam(value = "photo", required = false) MultipartFile photo,

			@RequestParam(value = "available", defaultValue = "true") boolean available,

			@RequestParam(value = "categoryId", required = false) Long categoryId,

			RedirectAttributes redirectAttributes) {

		// Duong dan anh moi, mac dinh la rong
		String imagePath = "";
		try {
			// 1. Kiem tra mon an can sua co ton tai khong
			if (id == null || !productRepository.existsById(id)) {

				throw new IllegalArgumentException("Không tìm thấy món ăn cần sửa!");
			}

			// 2. Kiem tra ten mon an
			if (name == null || name.trim().isEmpty()) {

				throw new IllegalArgumentException("Vui lòng nhập tên món ăn!");
			}

			// 3. Kiem tra gia ban
			if (price == null || !Double.isFinite(price) || price <= 0) {

				throw new IllegalArgumentException("Giá bán phải lớn hơn 0!");
			}

			// 4. Kiem tra danh muc
			if (categoryId == null || !categoryRepository.existsById(categoryId)) {

				throw new IllegalArgumentException("Danh mục không hợp lệ!");
			}

			// 5. Neu chon anh moi thi luu anh
			if (photo != null && !photo.isEmpty()) {

				imagePath = savePhoto(photo);
			}

			// 6. Cap nhat thong tin mon an trong MySQL
			productService.updateProduct(id, name, price, description, imagePath, available, categoryId);

			// 7. Thong bao thanh cong
			redirectAttributes.addFlashAttribute("success", "Cập nhật món ăn thành công!");

		} catch (IllegalArgumentException | IOException e) {
			// Xoa anh moi neu cap nhat that bai
			deleteUploadedPhoto(imagePath);
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		} catch (Exception e) {
			// Don dep anh moi neu MySQL gap loi
			deleteUploadedPhoto(imagePath);
			redirectAttributes.addFlashAttribute("error", "Có lỗi khi cập nhật món ăn!");
		}

		// Quay ve trang quan ly mon an
		return "redirect:/admin/products";
	}

	// ========================================
	// XOA MON AN
	// ========================================
	@PostMapping("/admin/products/delete")
	public String deleteProduct(
			
			@RequestParam("id") Long id,
			RedirectAttributes redirectAttributes) {

		try {
			// 1. Goi ProductService de xoa mon an
			productService.deleteProduct(id);

			// 2. Thong bao neu xoa thanh cong
			redirectAttributes.addFlashAttribute("success", "Xóa món ăn thành công!");

		} catch (IllegalArgumentException e) {

			// 3. Thong bao neu mon an khong duoc phep xoa
			redirectAttributes.addFlashAttribute("error", e.getMessage());

		} catch (org.springframework.dao.DataIntegrityViolationException e) {

			// 4. Xu ly neu mon an con lien ket voi du lieu khac
			redirectAttributes.addFlashAttribute("error", "Không thể xóa món ăn vì còn dữ liệu liên quan!");
		}

		// 5. Quay lai trang quan ly mon an
		return "redirect:/admin/products";
	}

	// ========================================
	// LUU FILE ANH VAO THU MUC UPLOAD
	// ========================================
	private String savePhoto(MultipartFile photo) throws IOException {

		// Khong chon anh thi luu duong dan rong
		if (photo == null || photo.isEmpty()) {
			return "";
		}

		// Kiem tra dung luong
		if (photo.getSize() > MAX_IMAGE_SIZE) {
			throw new IllegalArgumentException("Hình ảnh không được vượt quá 5MB!");
		}

		// Lay ten file goc
		String originalName = photo.getOriginalFilename();

		if (originalName == null) {
			throw new IllegalArgumentException("Tên file ảnh không hợp lệ!");
		}

		// Lay duoi file
		int dot = originalName.lastIndexOf(".");

		if (dot < 0) {
			throw new IllegalArgumentException("File ảnh không có định dạng hợp lệ!");
		}

		String extension = originalName.substring(dot).toLowerCase(Locale.ROOT);

		// Kiem tra dinh dang
		if (!extension.equals(".jpg") && !extension.equals(".jpeg") && !extension.equals(".png")
				&& !extension.equals(".webp")) {
			throw new IllegalArgumentException("Chỉ cho phép ảnh JPG, PNG hoặc WebP!");
		}
		// Doc noi dung anh
		byte[] bytes = photo.getBytes();
		// Kiem tra noi dung file co dung la anh
		if (!isValidImage(bytes, extension)) {
			throw new IllegalArgumentException("Nội dung file hình ảnh không hợp lệ!");
		}

		// Tu dong tao thu muc neu chua co
		Files.createDirectories(UPLOAD_DIR);

		// Tao ten file duy nhat
		String fileName = UUID.randomUUID() + extension;

		// Duong dan luu file
		Path filePath = UPLOAD_DIR.resolve(fileName);

		// Ghi anh xuong o dia
		Files.write(filePath, bytes, StandardOpenOption.CREATE_NEW);

		// Luu duong dan nay vao MySQL
		return "/uploads/products/" + fileName;
	}

	// ========================================
	// KIEM TRA DINH DANG THAT CUA ANH
	// ========================================
	private boolean isValidImage(byte[] b, String extension) {
		if (extension.equals(".jpg") || extension.equals(".jpeg")) {
			return b.length >= 3 && (b[0] & 255) == 255 && (b[1] & 255) == 216 && (b[2] & 255) == 255;
		}

		if (extension.equals(".png")) {
			return b.length >= 8 && (b[0] & 255) == 137 && b[1] == 80 && b[2] == 78 && b[3] == 71 && b[4] == 13
					&& b[5] == 10 && b[6] == 26 && b[7] == 10;
		}

		if (extension.equals(".webp")) {
			return b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W'
					&& b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
		}

		return false;
	}

	// ========================================
	// HIEN THI HINH ANH LEN TRINH DUYET
	// ========================================
	@GetMapping("/uploads/products/{fileName:.+}")
	public ResponseEntity<Resource> showProductImage(@PathVariable String fileName) {

		// Chi cho phep ten file do he thong tao
		if (!fileName.matches("[0-9a-f-]{36}\\.(jpg|jpeg|png|webp)")) {
			return ResponseEntity.notFound().build();
		}
		try {
			Path filePath = UPLOAD_DIR.resolve(fileName).normalize();
			if (!filePath.startsWith(UPLOAD_DIR) || !Files.isRegularFile(filePath)) {
				return ResponseEntity.notFound().build();
			}
			Resource resource = new UrlResource(filePath.toUri());
			MediaType mediaType;
			if (fileName.endsWith(".png")) {
				mediaType = MediaType.IMAGE_PNG;
			} else if (fileName.endsWith(".webp")) {
				mediaType = MediaType.parseMediaType("image/webp");
			} else {
				mediaType = MediaType.IMAGE_JPEG;
			}
			return ResponseEntity.ok().contentType(mediaType).body(resource);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	// ========================================
	// DON DEP ANH NEU LUU MON AN THAT BAI
	// ========================================

	private void deleteUploadedPhoto(String imagePath) {
		if (imagePath == null || !imagePath.startsWith("/uploads/products/")) {
			return;
		}
		try {
			String fileName = imagePath.substring("/uploads/products/".length());
			if (!fileName.matches("[0-9a-f-]{36}\\.(jpg|jpeg|png|webp)")) {
				return;
			}
			Files.deleteIfExists(UPLOAD_DIR.resolve(fileName));
		} catch (IOException e) {
			// Khong lam gian doan thong bao loi
		}
	}
}
