package com.example.duan.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.duan.entity.Customer;
import com.example.duan.entity.FoodOrder;
import com.example.duan.entity.OrderItem;
import com.example.duan.entity.Product;
import com.example.duan.entity.RestaurantTable;
import com.example.duan.repository.FoodOrderRepository;
import com.example.duan.repository.OrderItemRepository;
import com.example.duan.repository.ProductRepository;

import jakarta.persistence.EntityManager;

@Service
public class AdminOrderService {

	private final FoodOrderRepository foodOrderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ProductRepository productRepository;
	private final EntityManager entityManager;

	// Constructor
	public AdminOrderService(FoodOrderRepository foodOrderRepository, OrderItemRepository orderItemRepository,
			ProductRepository productRepository, EntityManager entityManager) {
		this.foodOrderRepository = foodOrderRepository;
		this.orderItemRepository = orderItemRepository;
		this.productRepository = productRepository;
		this.entityManager = entityManager;
	}

	// ========================================
	// TAO DON HANG MOI
	// ========================================
	@Transactional
	public FoodOrder createOrder(Long customerId, String orderType, Long tableId, String deliveryAddress, String note,
			List<Long> productIds, List<Integer> quantities) {

		// 1. Kiem tra khach hang
		if (customerId == null) {
			throw new IllegalArgumentException("Vui lòng chọn khách hàng!");
		}

		Customer customer = entityManager.find(Customer.class, customerId);

		if (customer == null) {
			throw new IllegalArgumentException("Khách hàng không tồn tại!");
		}

		// 2. Kiem tra hinh thuc dat
		if (orderType == null) {
			throw new IllegalArgumentException("Vui lòng chọn hình thức đặt hàng!");
		}

		orderType = orderType.trim();

		if (!orderType.equals("DELIVERY") && !orderType.equals("DINE_IN")) {

			throw new IllegalArgumentException("Hình thức đặt hàng không hợp lệ!");
		}

		// 3. Xu ly ban an
		RestaurantTable table = null;

		if (orderType.equals("DINE_IN")) {

			if (tableId == null) {
				throw new IllegalArgumentException("Vui lòng chọn bàn ăn!");
			}

			table = entityManager.find(RestaurantTable.class, tableId);

			if (table == null) {
				throw new IllegalArgumentException("Bàn ăn không tồn tại!");
			}
		}

		// 4. Xu ly dia chi giao hang
		if (deliveryAddress == null) {
			deliveryAddress = "";
		}

		deliveryAddress = deliveryAddress.trim();

		if (orderType.equals("DELIVERY") && deliveryAddress.isEmpty()) {

			throw new IllegalArgumentException("Vui lòng nhập địa chỉ giao hàng!");
		}

		if (deliveryAddress.length() > 255) {
			throw new IllegalArgumentException("Địa chỉ giao hàng không quá 255 ký tự!");
		}

		// 5. Xu ly ghi chu
		if (note == null) {
			note = "";
		}

		note = note.trim();

		if (note.length() > 255) {
			throw new IllegalArgumentException("Ghi chú không quá 255 ký tự!");
		}

		// 6. Kiem tra danh sach mon an
		if (productIds == null || quantities == null || productIds.isEmpty()
				|| productIds.size() != quantities.size()) {

			throw new IllegalArgumentException("Vui lòng chọn ít nhất một món ăn!");
		}

		// Danh sach chi tiet don hang
		List<OrderItem> orderItems = new ArrayList<>();

		// Tong tien don hang
		double totalPrice = 0;

		// 7. Duyet tung mon an
		for (int i = 0; i < productIds.size(); i++) {

			Long productId = productIds.get(i);
			Integer quantity = quantities.get(i);

			// Kiem tra ID mon
			if (productId == null) {
				throw new IllegalArgumentException("Món ăn không hợp lệ!");
			}

			// Kiem tra so luong
			if (quantity == null || quantity <= 0 || quantity > 1000) {

				throw new IllegalArgumentException("Số lượng món ăn phải từ 1 đến 1000!");
			}

			// Tim mon an trong MySQL
			Product product = productRepository.findById(productId)
					.orElseThrow(() -> new IllegalArgumentException("Món ăn không tồn tại!"));

			// Lay don gia tu MySQL
			Double unitPrice = product.getPrice();

			if (unitPrice == null || !Double.isFinite(unitPrice) || unitPrice <= 0) {

				throw new IllegalArgumentException("Giá món ăn không hợp lệ!");
			}

			// Tinh thanh tien
			double subTotal = unitPrice * quantity;

			totalPrice += subTotal;

			// Tao chi tiet don hang
			OrderItem item = new OrderItem();

			item.setProduct(product);
			item.setQuantity(quantity);
			item.setPrice(unitPrice);

			orderItems.add(item);
		}

		if (!Double.isFinite(totalPrice)) {
			throw new IllegalArgumentException("Tổng tiền đơn hàng không hợp lệ!");
		}

		// 8. Tao don hang
		FoodOrder order = new FoodOrder();

		order.setCustomer(customer);
		order.setRestaurantTable(table);
		order.setOrderType(orderType);
		order.setOrderTime(LocalDateTime.now());
		order.setStatus("PENDING");
		order.setTotalPrice(totalPrice);
		order.setNote(note);

		// Chi luu dia chi neu giao hang
		if (orderType.equals("DELIVERY")) {
			order.setDeliveryAddress(deliveryAddress);
		} else {
			order.setDeliveryAddress("");
		}

		// 9. Luu don hang vao bang food_orders
		FoodOrder savedOrder = foodOrderRepository.save(order);

		// 10. Gan don hang cho tung chi tiet
		for (OrderItem item : orderItems) {

			item.setOrder(savedOrder);
		}

		// 11. Luu chi tiet vao bang order_items
		orderItemRepository.saveAll(orderItems);

		// 12. Tra ve don hang da tao
		return savedOrder;
	}

	// ========================================
	// CAP NHAT TRANG THAI DON HANG
	// ========================================

	@Transactional
	public FoodOrder updateOrderStatus(Long orderId, String newStatus) {

		// 1. Kiem tra ID
		if (orderId == null) {
			throw new IllegalArgumentException("ID đơn hàng không hợp lệ!");
		}

		// 2. Kiem tra trang thai moi
		if (newStatus == null || newStatus.trim().isEmpty()) {
			throw new IllegalArgumentException("Vui lòng chọn trạng thái đơn hàng!");
		}

		newStatus = newStatus.trim();

		// 3. Chi cho phep 4 trang thai
		if (!newStatus.equals("PENDING") && !newStatus.equals("PREPARING") && !newStatus.equals("COMPLETED")
				&& !newStatus.equals("CANCELLED")) {

			throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ!");
		}

		// 4. Tim don hang
		FoodOrder order = foodOrderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng cần sửa!"));

		// 5. Lay trang thai cu
		String currentStatus = order.getStatus();

		// Neu khong thay doi thi tra ve
		if (newStatus.equals(currentStatus)) {
			return order;
		}

		// 6. Kiem tra quy trinh chuyen trang thai
		boolean validTransition = false;

		if ("PENDING".equals(currentStatus)) {

			// Cho xac nhan -> Dang che bien hoac Da huy
			validTransition = newStatus.equals("PREPARING") || newStatus.equals("CANCELLED");

		} else if ("PREPARING".equals(currentStatus)) {

			// Dang che bien -> Hoan thanh hoac Da huy
			validTransition = newStatus.equals("COMPLETED") || newStatus.equals("CANCELLED");
		}

		// 7. Chan chuyen trang thai khong hop le
		if (!validTransition) {
			throw new IllegalArgumentException(
					"Không thể chuyển trạng thái từ " + currentStatus + " sang " + newStatus + "!");
		}

		// 8. Cap nhat trang thai
		order.setStatus(newStatus);

		// 9. Luu vao MySQL
		return foodOrderRepository.save(order);
	}

	// ========================================
	// XOA DON HANG AN TOAN
	// ========================================

	@Transactional
	public void deleteOrder(Long orderId) {

		// 1. Kiem tra ID don hang
		if (orderId == null) {
			throw new IllegalArgumentException("ID đơn hàng không hợp lệ!");
		}

		// 2. Tim don hang trong MySQL
		FoodOrder order = foodOrderRepository.findById(orderId)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng cần xóa!"));

		// 3. Chi cho phep xoa don da huy
		if (!"CANCELLED".equals(order.getStatus())) {

			throw new IllegalArgumentException("Chỉ có thể xóa đơn hàng đã hủy!");
		}

		// 4. Kiem tra lich su thanh toan
		Long paymentCount = entityManager
				.createQuery("SELECT COUNT(p) FROM Payment p " + "WHERE p.order.id = :orderId", Long.class)
				.setParameter("orderId", orderId).getSingleResult();

		// 5. Neu co ban ghi thanh toan thi khong xoa
		if (paymentCount > 0) {

			throw new IllegalArgumentException("Không thể xóa đơn hàng vì đã có " + "dữ liệu thanh toán liên quan!");
		}

		// 6. Lay cac chi tiet mon an cua don hang
		List<OrderItem> items = orderItemRepository.findByOrder_IdOrderByIdAsc(orderId);

		// 7. Xoa chi tiet don hang truoc
		if (!items.isEmpty()) {

			orderItemRepository.deleteAll(items);

			// Dong bo thao tac xoa chi tiet
			orderItemRepository.flush();
		}

		// 8. Xoa don hang chinh
		foodOrderRepository.delete(order);

		// 9. Dong bo thao tac xoa
		foodOrderRepository.flush();
	}

}
