package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private UserDetailsService userDetailsService;
	
//	private DaoAuthenticationProvider dao;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/register", "/login", "/css/**", "/js/**", "/images/**")
				.permitAll().requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/seller/**")
				.hasRole("SELLER").anyRequest().authenticated())


//				.csrf(csrf -> csrf.requireCsrfProtectionMatcher(request -> {
//					String path = request.getRequestURI();
//					return "POST".equals(request.getMethod())
//							&& (path.equals("/login") || path.equals("/admin/users/add-admin"));
//				})).exceptionHandling(
//						exception -> exception.accessDeniedHandler((request, response, accessDeniedException) -> {
//							response.setStatus(403);
//							response.getWriter().write("CSRF Access Denied: Missing or Invalid Token");
//						}))
		
		.csrf(c -> c.disable())
				.formLogin(form -> form.loginPage("/login").successHandler((request, response, authentication) -> {
					CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
					String role = user.getRole().name();
					if (role.equals("ADMIN")) {
						response.sendRedirect("/admin/dashboard");
					} else if (role.equals("SELLER")) {
						response.sendRedirect("/seller/dashboard");
					} else {
						response.sendRedirect("/products");
					}
				}).permitAll())
				.logout(logout -> logout.logoutUrl("/logout").logoutRequestMatcher(
						request -> "GET".equals(request.getMethod()) && "/logout".equals(request.getRequestURI()))
						.logoutSuccessUrl("/login?logout").permitAll());
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}


}
