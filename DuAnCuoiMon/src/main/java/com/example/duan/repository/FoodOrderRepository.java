package com.example.duan.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.duan.entity.FoodOrder;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {

	// Dem don hang theo trang thai
	long countByStatus(String status);

	// Dem don hang theo nhieu trang thai
	long countByStatusIn(List<String> statuses);

	// Lay 5 don hang can xu ly moi nhat
	List<FoodOrder> findTop5ByStatusInOrderByOrderTimeDesc(List<String> statuses);

	// Thong ke don hang theo tung thang trong nam
	@Query(value = """
			SELECT
			    MONTH(order_time) AS month_no,
			    COUNT(*) AS total_orders,
			    SUM(
			        CASE
			            WHEN status IN ('PENDING', 'PREPARING')
			            THEN 1
			            ELSE 0
			        END
			    ) AS processing_orders,
			    SUM(
			        CASE
			            WHEN status = 'COMPLETED'
			            THEN 1
			            ELSE 0
			        END
			    ) AS completed_orders
			FROM food_orders
			WHERE YEAR(order_time) = :year
			GROUP BY MONTH(order_time)
			ORDER BY month_no
			""", nativeQuery = true)
	List<Object[]> findMonthlyOrderStatistics(@Param("year") int year);

}
