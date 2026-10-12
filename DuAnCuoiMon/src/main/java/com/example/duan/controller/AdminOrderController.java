
package com.example.duan.controller;

import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.duan.entity.Customer;
import com.example.duan.entity.FoodOrder;
import com.example.duan.entity.OrderItem;
import com.example.duan.entity.Product;
import com.example.duan.entity.RestaurantTable;

import com.example.duan.repository.FoodOrderRepository;
import com.example.duan.repository.OrderItemRepository;
import com.example.duan.service.AdminOrderService;

import jakarta.persistence.EntityManager;

@Controller
public class AdminOrderController {

	private final FoodOrderRepository foodOrderRepository;
	private final AdminOrderService adminOrderService;
	private final EntityManager entityManager;
	private final OrderItemRepository orderItemRepository;

	// Constructor
	public AdminOrderController(FoodOrderRepository foodOrderRepository, AdminOrderService adminOrderService,
			EntityManager entityManager, OrderItemRepository orderItemRepository) {

		this.foodOrderRepository = foodOrderRepository;
		this.adminOrderService = adminOrderService;
		this.entityManager = entityManager;
		this.orderItemRepository = orderItemRepository;
	}

	// ========================================
	// HIEN THI TRANG QUAN LY DON HANG
	// ========================================

	@GetMapping("/admin/orders")
	public String orders(Model model) {

		// Lay danh sach don hang moi nhat
		List<FoodOrder> orders = foodOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderTime"));

		// Lay danh sach khach hang
		List<Customer> customers = entityManager.createQuery("SELECT c FROM Customer c ORDER BY c.name", Customer.class)
				.getResultList();

		// Lay danh sach ban an
		List<RestaurantTable> tables = entityManager
				.createQuery("SELECT t FROM RestaurantTable t", RestaurantTable.class).getResultList();

		// Lay cac mon an dang kinh doanh
		List<Product> products = entityManager
				.createQuery("SELECT p FROM Product p " + "WHERE p.available = true ORDER BY p.name", Product.class)
				.getResultList();

		// Thong ke don hang
		long totalOrders = foodOrderRepository.count();

		long pendingOrders = foodOrderRepository.countByStatus("PENDING");

		long preparingOrders = foodOrderRepository.countByStatus("PREPARING");

		long completedOrders = foodOrderRepository.countByStatus("COMPLETED");

		long cancelledOrders = foodOrderRepository.countByStatus("CANCELLED");

		// Lay nam hien tai
		int year = Year.now().getValue();

		// Truyen du lieu sang HTML
		model.addAttribute("orders", orders);
		model.addAttribute("customers", customers);
		model.addAttribute("tables", tables);
		model.addAttribute("products", products);

		model.addAttribute("totalOrders", totalOrders);
		model.addAttribute("pendingOrders", pendingOrders);
		model.addAttribute("preparingOrders", preparingOrders);
		model.addAttribute("completedOrders", completedOrders);
		model.addAttribute("cancelledOrders", cancelledOrders);
		model.addAttribute("reportYear", year);

		return "admin/orders";
	}

	// ========================================
	// TAO DON HANG MOI
	// ========================================

	@PostMapping("/admin/orders/add")
	public String addOrder(

			@RequestParam(value = "customerId", required = false) Long customerId,

			@RequestParam(value = "orderType", required = false) String orderType,

			@RequestParam(value = "tableId", required = false) Long tableId,

			@RequestParam(value = "deliveryAddress", defaultValue = "") String deliveryAddress,

			@RequestParam(value = "note", defaultValue = "") String note,

			@RequestParam(value = "productIds", required = false) List<Long> productIds,

			@RequestParam(value = "quantities", required = false) List<Integer> quantities,

			RedirectAttributes redirectAttributes) {

		try {

			// Goi Service de tao don hang
			FoodOrder newOrder = adminOrderService.createOrder(customerId, orderType, tableId, deliveryAddress, note,
					productIds, quantities);

			// Tao ma don hang
			String orderCode = String.format("DH%03d", newOrder.getId());

			redirectAttributes.addFlashAttribute("success", "Tạo đơn hàng " + orderCode + " thành công!");

		} catch (IllegalArgumentException e) {

			redirectAttributes.addFlashAttribute("error", e.getMessage());

		} catch (Exception e) {

			redirectAttributes.addFlashAttribute("error", "Có lỗi khi tạo đơn hàng. Vui lòng kiểm tra lại!");
		}

		return "redirect:/admin/orders";
	}

	// ========================================
	// LAY CHI TIET MON AN CUA MOT DON HANG
	// ========================================

	@GetMapping("/admin/orders/{id}/items")
	@ResponseBody
	public ResponseEntity<List<Map<String, Object>>> getOrderItems(@PathVariable("id") Long id) {

		// Kiem tra don hang co ton tai khong
		if (!foodOrderRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}

		// Lay danh sach mon an
		List<OrderItem> items = orderItemRepository.findByOrder_IdOrderByIdAsc(id);

		// Tao ket qua JSON
		List<Map<String, Object>> result = new ArrayList<>();

		for (OrderItem item : items) {

			Map<String, Object> row = new LinkedHashMap<>();

			String productName = "Món không xác định";

			if (item.getProduct() != null) {
				productName = item.getProduct().getName();
			}

			int quantity = item.getQuantity();
			double price = item.getPrice();
			double subtotal = quantity * price;

			row.put("name", productName);
			row.put("quantity", quantity);
			row.put("price", price);
			row.put("subtotal", subtotal);

			result.add(row);
		}

		return ResponseEntity.ok(result);
	}

	// ========================================
	// CAP NHAT TRANG THAI DON HANG
	// ========================================

	@PostMapping("/admin/orders/update-status")
	public String updateOrderStatus(

			@RequestParam("orderId") Long orderId,

			@RequestParam("status") String status,

			RedirectAttributes redirectAttributes) {

		try {

			// Goi Service xu ly cap nhat trang thai
			FoodOrder updatedOrder = adminOrderService.updateOrderStatus(orderId, status);

			// Tao ma don hang
			String orderCode = String.format("DH%03d", updatedOrder.getId());

			// Thong bao thanh cong
			redirectAttributes.addFlashAttribute("success",
					"Cập nhật trạng thái đơn hàng " + orderCode + " thành công!");

		} catch (IllegalArgumentException e) {

			// Loi du lieu hoac quy trinh trang thai
			redirectAttributes.addFlashAttribute("error", e.getMessage());

		} catch (Exception e) {

			// Loi he thong
			redirectAttributes.addFlashAttribute("error", "Có lỗi khi cập nhật trạng thái đơn hàng!");
		}

		return "redirect:/admin/orders";
	}

	// ========================================
	// XOA DON HANG
	// ========================================

	@PostMapping("/admin/orders/delete")
	public String deleteOrder(

			@RequestParam("orderId") Long orderId,

			RedirectAttributes redirectAttributes) {

		try {

			// 1. Goi Service de xoa don hang
			adminOrderService.deleteOrder(orderId);

			// 2. Tao ma don hang hien thi
			String orderCode = String.format("DH%03d", orderId);

			// 3. Thong bao xoa thanh cong
			redirectAttributes.addFlashAttribute("success", "Xóa đơn hàng " + orderCode + " thành công!");

		} catch (IllegalArgumentException e) {

			// 4. Thong bao neu don hang khong duoc phep xoa
			redirectAttributes.addFlashAttribute("error", e.getMessage());

		} catch (org.springframework.dao.DataIntegrityViolationException e) {

			// 5. Don hang con lien ket voi du lieu khac
			redirectAttributes.addFlashAttribute("error", "Không thể xóa đơn hàng vì còn dữ liệu liên quan!");

		} catch (Exception e) {

			// 6. Xu ly loi he thong
			redirectAttributes.addFlashAttribute("error", "Có lỗi khi xóa đơn hàng. Vui lòng kiểm tra lại!");
		}

		// 7. Quay ve trang quan ly don hang
		return "redirect:/admin/orders";
	}

}
