
package com.example.duan.controller;

import java.util.List;
import java.time.Year;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.duan.repository.CategoryRepository;
import com.example.duan.repository.ProductRepository;
import com.example.duan.repository.CustomerRepository;
import com.example.duan.repository.FoodOrderRepository;
import com.example.duan.repository.PaymentRepository;
import com.example.duan.repository.RestaurantTableRepository;
import com.example.duan.repository.OrderItemRepository;

@Controller
public class AdminDashboardController {

	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;
	private final CustomerRepository customerRepository;
	private final FoodOrderRepository foodOrderRepository;
	private final PaymentRepository paymentRepository;
	private final RestaurantTableRepository restaurantTableRepository;
	private final OrderItemRepository orderItemRepository;

	public AdminDashboardController(CategoryRepository categoryRepository, ProductRepository productRepository,
			CustomerRepository customerRepository, FoodOrderRepository foodOrderRepository,
			PaymentRepository paymentRepository, RestaurantTableRepository restaurantTableRepository,
			OrderItemRepository orderItemRepository) {
		this.categoryRepository = categoryRepository;
		this.productRepository = productRepository;
		this.customerRepository = customerRepository;
		this.foodOrderRepository = foodOrderRepository;
		this.paymentRepository = paymentRepository;
		this.restaurantTableRepository = restaurantTableRepository;
		this.orderItemRepository = orderItemRepository;
	}

	@GetMapping({ "/admin", "/admin/dashboard" })
	public String dashboard(Model model) {

		// Tong danh muc
		long totalCategories = categoryRepository.count();
		// Tong mon an
		long totalProducts = productRepository.count();
		// Tong khach hang
		long totalCustomers = customerRepository.count();
		// Tong don hang
		long totalOrders = foodOrderRepository.count();
		// Don dang xu ly
		long processingOrders = foodOrderRepository.countByStatusIn(List.of("PENDING", "PREPARING"));
		// Don hoan thanh
		long completedOrders = foodOrderRepository.countByStatus("COMPLETED");
		// Tong doanh thu da thanh toan
		Double totalRevenue = paymentRepository.sumPaidAmount();
		// Tong so ban an
		long totalTables = restaurantTableRepository.count();
		// So ban dang co khach
		long servingTables = restaurantTableRepository.countByStatus("OCCUPIED");
		// Dem so ban trong
		long availableTables = restaurantTableRepository.countByStatus("AVAILABLE");
		// Dem so ban o trang thai khac
		long otherTables = totalTables - servingTables - availableTables;

		// Truyen du lieu sang dashboard.html
		model.addAttribute("totalCategories", totalCategories);
		model.addAttribute("totalProducts", totalProducts);
		model.addAttribute("totalCustomers", totalCustomers);
		model.addAttribute("totalOrders", totalOrders);
		model.addAttribute("processingOrders", processingOrders);
		model.addAttribute("completedOrders", completedOrders);
		model.addAttribute("totalRevenue", totalRevenue);
		model.addAttribute("totalTables", totalTables);
		model.addAttribute("servingTables", servingTables);
		model.addAttribute("availableTables", availableTables);
		model.addAttribute("otherTables", otherTables);

		// Lay 5 don hang dang cho hoac dang chuan bi
		List<com.example.duan.entity.FoodOrder> pendingOrders = foodOrderRepository
				.findTop5ByStatusInOrderByOrderTimeDesc(List.of("PENDING", "PREPARING"));
		// Truyen danh sach sang giao dien
		model.addAttribute("pendingOrders", pendingOrders);
		// Lay 5 mon an ban chay tu MySQL
		var bestSellers = orderItemRepository.findTop5BestSellers();
		// Chuyen danh sach sang giao dien
		model.addAttribute("bestSellers", bestSellers);
		// Lay nam hien tai
		int year = Year.now().getValue();
		// Tao mang doanh thu 12 thang
		double[] monthlyRevenue = new double[12];
		// Lay du lieu doanh thu tu MySQL
		List<Object[]> revenueRows = paymentRepository.findPaidRevenueByMonth(year);
		// Dua doanh thu vao dung thang
		for (Object[] row : revenueRows) {
			int month = ((Number) row[0]).intValue();
			double revenue = ((Number) row[1]).doubleValue();
			if (month >= 1 && month <= 12) {
				monthlyRevenue[month - 1] = revenue;
			}
		}
		
		// Truyen du lieu sang dashboard.html
		model.addAttribute("monthlyRevenue", monthlyRevenue);
		model.addAttribute("reportYear", year);
		
		// Tao mang doanh thu cho 4 quy
		double[] quarterlyRevenue = new double[4];
		// Cong doanh thu 3 thang thanh 1 quy
		for (int i = 0; i < 12; i++) {
			int quarterIndex = i / 3;
			quarterlyRevenue[quarterIndex] += monthlyRevenue[i];
		}
		
		// Truyen du lieu 4 quy sang HTML
		model.addAttribute("quarterlyRevenue", quarterlyRevenue);
		
		// ========================================
		// THONG KE DON HANG THEO 12 THANG
		// ========================================
		// Mang luu tong don hang cua 12 thang
		long[] monthlyOrderCounts = new long[12];
		// Mang luu don hang dang xu ly
		long[] monthlyProcessingOrders = new long[12];
		// Mang luu don hang da hoan thanh
		long[] monthlyCompletedOrders = new long[12];
		// Lay du lieu thong ke tu MySQL
		List<Object[]> monthlyOrderRows = foodOrderRepository.findMonthlyOrderStatistics(year);
		// Duyet qua ket qua truy van
		for (Object[] row : monthlyOrderRows) {
			// Lay so thang
			int month = ((Number) row[0]).intValue();
			// Lay so don hang
			long total = ((Number) row[1]).longValue();
			// Lay so don dang xu ly
			long processing = ((Number) row[2]).longValue();
			// Lay so don hoan thanh
			long completed = ((Number) row[3]).longValue();
			// Dua du lieu vao dung vi tri thang
			if (month >= 1 && month <= 12) {
				monthlyOrderCounts[month - 1] = total;
				monthlyProcessingOrders[month - 1] = processing;
				monthlyCompletedOrders[month - 1] = completed;
			}
		}
		
		// Truyen cac mang du lieu sang Dashboard
		model.addAttribute("monthlyOrderCounts", monthlyOrderCounts);
		model.addAttribute("monthlyProcessingOrders", monthlyProcessingOrders);
		model.addAttribute("monthlyCompletedOrders", monthlyCompletedOrders);
		
		return "admin/dashboard";
	}
}
