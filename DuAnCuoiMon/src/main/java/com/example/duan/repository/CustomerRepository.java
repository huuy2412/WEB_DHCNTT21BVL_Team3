package com.example.duan.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.duan.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
