package com.example.duan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.example.duan.entity.Product;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
	// lấy danh sách 12 món hiện lên file HOME
	List<Product> findTop12ByAvailableTrueOrderByIdDesc();

	// Dem so mon an trong tung danh muc
	@Query("""
			SELECT p.category.id, COUNT(p)
			FROM Product p
			WHERE p.category IS NOT NULL
			GROUP BY p.category.id
			""")
	List<Object[]> countProductsByCategory();
}
