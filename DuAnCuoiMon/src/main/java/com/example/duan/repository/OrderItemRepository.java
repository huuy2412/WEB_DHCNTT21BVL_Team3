package com.example.duan.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.duan.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	// Lay 5 mon an ban chay nhat
	@Query(value = """
			SELECT
			    p.name AS productName,
			    SUM(oi.quantity) AS totalSold,
			    SUM(oi.quantity * oi.price) AS revenue
			FROM order_items oi
			JOIN products p ON oi.product_id = p.id
			JOIN food_orders o ON oi.order_id = o.id
			WHERE o.status = 'COMPLETED'
			GROUP BY p.id, p.name
			ORDER BY totalSold DESC, revenue DESC
			LIMIT 5
			""", nativeQuery = true)
	List<BestSeller> findTop5BestSellers();

	// Du lieu tra ve cho bang mon ban chay
	interface BestSeller {
		String getProductName();

		Long getTotalSold();

		Double getRevenue();
	}

	// ========================================
	// KIEM TRA MON AN DA CO TRONG DON HANG
	// ========================================
	// Dem so dong chi tiet don hang co chua mon nay
	long countByProduct_Id(Long productId);
	
	// ========================================
	// LAY DANH SACH MON AN THEO DON HANG
	// ========================================
	// Lay cac mon thuoc mot don hang, sap xep theo ID
	List<OrderItem> findByOrder_IdOrderByIdAsc(Long orderId);


}
