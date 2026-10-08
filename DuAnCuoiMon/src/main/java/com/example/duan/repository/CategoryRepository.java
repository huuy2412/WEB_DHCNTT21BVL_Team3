package com.example.duan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.duan.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
