package com.apollo.userservice1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Security configuration for the application. Configures HTTP security,
 * disables CSRF, and secures endpoints using JWT (Keycloak).
 */
@Configuration
public class SecurityConfig {

	/**
	 * Bean for making REST calls, used for communicating with Keycloak.
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/*
	 * @Bean public BCryptPasswordEncoder passwordEncoder() { return new
	 * BCryptPasswordEncoder(); }
	 * 
	 * 
	 *//**
		 * Security filter chain configuration. - Allows unauthenticated access to
		 * register and login endpoints. - Secures all other endpoints. - Enables
		 * JWT-based authentication with OAuth2 resource server.
		 *//*
			 * @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http)
			 * throws Exception { http.csrf(csrf -> csrf.disable()) .authorizeHttpRequests(
			 * auth -> auth.requestMatchers("/users/register", "/users/login").permitAll()
			 * .anyRequest().authenticated() ).oauth2ResourceServer(oauth2 -> oauth2.jwt());
			 * // Enable JWT support
			 * 
			 * return http.build(); }
			 */
}
