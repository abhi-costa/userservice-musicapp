package com.apollo.userservice1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.apollo.userservice1.dto.CreateUserRequest;
import com.apollo.userservice1.dto.LoginRequest;
import com.apollo.userservice1.dto.LoginResponse;
import com.apollo.userservice1.dto.UserResponse;
import com.apollo.userservice1.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	/**
	 * Registers a new user.
	 * 
	 * @param request contains user registration details.
	 * @return 201 Created with user response if successful, otherwise 400/500
	 *         error.
	 */
	@PostMapping("/register")
	public ResponseEntity<?> register(@Valid @RequestBody CreateUserRequest request) {
		try {
			if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getEmail())
					|| !StringUtils.hasText(request.getPassword())) {
				return ResponseEntity.badRequest().body("Username, email, and password must not be null or empty.");
			}
			UserResponse response = userService.register(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred during registration.");
		}
	}

	/**
	 * Authenticates a user and returns JWT access and refresh tokens.
	 * 
	 * @param request contains username and password.
	 * @return 200 OK with tokens if successful, otherwise 400/500 error.
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
		try {
			if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
				return ResponseEntity.badRequest().body("Username and password must not be null or empty.");
			}

			LoginResponse response = userService.login(request);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login.");
		}
	}

	/**
	 * Retrieves a user profile by their ID.
	 * 
	 * @param id MongoDB ID of the user.
	 * @return 200 OK with user details, 404 if not found, or 500 on server error.
	 */
	@GetMapping("/{id}")
	public ResponseEntity<?> getUserById(@PathVariable String id) {
		try {
			if (!StringUtils.hasText(id)) {
				return ResponseEntity.badRequest().body("User ID must not be null or empty.");
			}
			UserResponse response = userService.getById(id);
			return ResponseEntity.ok(response);
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An error occurred while fetching user.");
		}
	}
}
