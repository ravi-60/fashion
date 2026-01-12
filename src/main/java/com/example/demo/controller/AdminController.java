package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.config.CustomUserDetails;
import com.example.demo.service.AdminService;

@Controller
public class AdminController {
	@Autowired
    private AdminService adminService;
	@GetMapping("/admin/dashboard")
    public String adminDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model){
			
		
        model.addAttribute("username", userDetails.getUsername());

        model.addAttribute("totalUsers",
                adminService.getTotalUsers());

        model.addAttribute("totalSellers",
                adminService.getTotalSellers());

        model.addAttribute("totalOrders",
                adminService.getTotalOrders());

        model.addAttribute("totalRevenue",
                adminService.getTotalRevenue());

        return "admin_dashboard";
    }
}
