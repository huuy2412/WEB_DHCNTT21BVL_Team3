package com.example.duan;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
	public class DuAnCuoiMonApplication {

	    public static void main(String[] args) {
	        SpringApplication.run(DuAnCuoiMonApplication.class, args);
	    }

	    @Bean
	    CommandLineRunner testConnection(DataSource dataSource) {
	        return args -> {
	            try (Connection connection = dataSource.getConnection()) {
	                System.out.println("==============================");
	                System.out.println("KẾT NỐI MYSQL THÀNH CÔNG");
	                System.out.println("Database: "
	                        + connection.getCatalog());
	                System.out.println("==============================");
	            } catch (Exception e) {
	                System.out.println("==============================");
	                System.out.println("KẾT NỐI MYSQL THẤT BẠI");
	                System.out.println(e.getMessage());
	                System.out.println("==============================");
	            }
	        };
	    }

}
