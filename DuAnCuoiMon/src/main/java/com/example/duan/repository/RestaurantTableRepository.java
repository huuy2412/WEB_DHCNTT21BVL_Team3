package com.example.duan.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.duan.entity.RestaurantTable;

public interface RestaurantTableRepository
        extends JpaRepository<RestaurantTable, Long> {

    // Dem so ban theo trang thai
    long countByStatus(String status);

}
