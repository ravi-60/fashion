package com.example.demo.config;

import com.example.demo.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired
	private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(
				auth -> auth.requestMatchers("/register", "/login", "/css/**", "/js/**", "/api/debug/**").permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/seller/**").hasRole("SELLER")
						.anyRequest().authenticated())

				.csrf(csrf -> csrf.disable())
				.formLogin(form -> form.loginPage("/login").successHandler((request, response, authentication) -> {
					com.example.demo.config.CustomUserDetails user = (com.example.demo.config.CustomUserDetails) authentication
							.getPrincipal();
					String role = user.getRole().name();
					String context = request.getContextPath();
					if (role.equals("ADMIN")) {
						response.sendRedirect(context + "/admin/dashboard");
					} else if (role.equals("SELLER")) {
						response.sendRedirect(context + "/seller/dashboard");
					} else {
						response.sendRedirect(context + "/products");
					}
				}).permitAll()).logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll());
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
}