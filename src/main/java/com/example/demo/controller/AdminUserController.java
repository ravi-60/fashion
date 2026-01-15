package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AdminService;
import com.example.demo.service.UserService;

@Controller
public class AdminUserController {
	@Autowired
	private AdminService adminService;
	
	@Value("${admin.users.page.size:5}")
	int size;

	@GetMapping("/admin/users")
	public String listUsers(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
			@RequestParam(name = "sort", required = false, defaultValue = "userId") String sort,
			@RequestParam(name = "dir", required = false, defaultValue = "asc") String dir,
			@RequestParam(name = "search", required = false) String search, Model model) {
		
		Page<User> userPage = adminService.listUsersService(page, sort, dir, search, size);
		model.addAttribute("userPage", userPage);
		model.addAttribute("users", userPage.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("pageSize", size);
		model.addAttribute("sort", sort);
		model.addAttribute("dir", dir);
		model.addAttribute("search", search);
		return "admin_users";
	}

	@PostMapping("/admin/users/add-admin")
	public String addAdmin(@RequestParam String username, @RequestParam String email, @RequestParam String password) {

		boolean isSaved = adminService.addAdminService(username, email, password);

	    if (isSaved) {
	        return "redirect:/admin/users?success";
	    } else {
	        return "redirect:/admin/users?error";
	    }
	}

}
