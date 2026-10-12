package com.example.duan.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.duan.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Tinh tong so tien da thanh toan
    @Query("SELECT COALESCE(SUM(p.amount), 0.0) " +
           "FROM Payment p " +
           "WHERE p.paymentStatus = 'PAID'")
    Double sumPaidAmount();
    
 // Tong doanh thu theo tung thang trong nam
    @Query(value = """
        SELECT
            MONTH(payment_time) AS month_number,
            SUM(amount) AS revenue
        FROM payments
        WHERE payment_status = 'PAID'
          AND YEAR(payment_time) = :year
        GROUP BY MONTH(payment_time)
        ORDER BY month_number
        """, nativeQuery = true)
    List<Object[]> findPaidRevenueByMonth(
            @Param("year") int year
    );

}
