package com.example.demo;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.config.CustomUserDetails;
import com.example.demo.controller.AdminController;
import com.example.demo.service.AdminService;

@WebMvcTest(AdminController.class)
public class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AdminService adminService;

	private CustomUserDetails mockAdmin;

	@BeforeEach
	void setUp() {

		mockAdmin = Mockito.mock(CustomUserDetails.class);
		when(mockAdmin.getUsername()).thenReturn("admin");

	}

	
	@Test
	@DisplayName("Should load the admin dashboard status 200")
	void testDashboardStatus() throws Exception {
		mockMvc.perform(get("/admin/dashboard")
				.with(user(mockAdmin)))
				.andExpect(status().isOk());
	}


	@Test
	@DisplayName("Should return the 'admin_dashboard' view name")
	void testDashboardViewName() throws Exception {
		mockMvc.perform(get("/admin/dashboard")
				.with(user(mockAdmin)))
				.andExpect(view().name("admin_dashboard"));
	}

}