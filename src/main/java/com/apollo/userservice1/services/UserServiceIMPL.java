package com.apollo.userservice1.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.apollo.userservice1.dto.CreateUserRequest;
import com.apollo.userservice1.dto.LoginRequest;
import com.apollo.userservice1.dto.LoginResponse;
import com.apollo.userservice1.dto.UserResponse;
import com.apollo.userservice1.model.User;
import com.apollo.userservice1.repository.UserRepository;

@Service
public class UserServiceIMPL implements UserService {

	private final UserRepository repo;
	private final RestTemplate restTemplate;

	@Value("${keycloak.auth-server-url}")
	private String keycloakServerUrl;

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${keycloak.client-id}")
	private String clientId;

	@Value("${keycloak.admin.username}")
	private String adminUsername;

	@Value("${keycloak.admin.password}")
	private String adminPassword;

	private static final String ADMIN_CLI = "admin-cli";

	public UserServiceIMPL(UserRepository repo, RestTemplate restTemplate) {
		this.repo = repo;
		this.restTemplate = restTemplate;
	}

	/**
	 * Registers a new user: - Creates the user in Keycloak for authentication -
	 * Saves non-sensitive user profile in MongoDB
	 */
	@Override
	public UserResponse register(CreateUserRequest req) {
		System.out.println("welcome to register");
		// Step 1: Register user in Keycloak
		createUserInKeycloak(req);
		System.out.println("sucessfully  register in keycloak");
		// Step 2: Store user profile (without password) in MongoDB
		User user = new User(req.getUsername(), req.getEmail(), req.getFirstName(), req.getLastName());
		User savedUser = repo.save(user);
		System.out.println("sucessfully  register");
		return new UserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(),
				savedUser.getFirstName(), savedUser.getLastName());
	}

	/**
	 * Authenticates user against Keycloak and returns JWT tokens.
	 */
	@Override
	public LoginResponse login(LoginRequest req) {
		System.out.println("login service");
		Map<String, Object> tokenResponse = authenticateWithKeycloak(req.getUsername(), req.getPassword(), clientId);
		System.out.println("sucessfully login"+ tokenResponse);
		return new LoginResponse(tokenResponse.get("access_token").toString(),
				tokenResponse.get("refresh_token").toString(), (int) tokenResponse.get("expires_in"));
	}

	/**
	 * Retrieves a user profile by ID from MongoDB.
	 */
	@Override
	public UserResponse getById(String id) {
		User user = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

		return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(),
				user.getLastName());
	}

	/**
	 * Creates a user in Keycloak using admin access token and user registration
	 * data.
	 */
	private void createUserInKeycloak(CreateUserRequest req) {
		System.out.println("welcome to createUserInKeycloak");
		String url = keycloakServerUrl + "/admin/realms/" + realm + "/users";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(getAdminAccessToken());

		// Prepare JSON payload for Keycloak API
		Map<String, Object> payload = new HashMap<>();
		payload.put("username", req.getUsername());
		payload.put("email", req.getEmail());
		payload.put("enabled", true);
		payload.put("firstName", req.getFirstName());
		payload.put("lastName", req.getLastName());
		
		System.out.println("paylode--"+ payload.toString());

		Map<String, Object> credentials = new HashMap<>();
		credentials.put("type", "password");
		credentials.put("value", req.getPassword());
		credentials.put("temporary", false);
		payload.put("credentials", new Object[] { credentials });
		System.out.println("credentials- "+ credentials);

		HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
		System.out.println("7--" +entity);

		ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
		

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new RuntimeException("Failed to register user in Keycloak: " + response.getBody());
		}
	}

	/**
	 * Authenticates a user with Keycloak and returns the token response.
	 */
	private Map<String, Object> authenticateWithKeycloak(String username, String password, String clientId) {
		String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "password");
		body.add("client_id", clientId);
		body.add("username", username);
		body.add("password", password);

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
		System.out.println(" 0 ------------->" + body);

		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
			Map<String, Object> tokenData = response.getBody();

			if (!response.getStatusCode().is2xxSuccessful() || tokenData == null
					|| !tokenData.containsKey("access_token")) {
				System.out.println("3 --- Invalid credentials");
				throw new RuntimeException("Invalid credentials");
				
			}
			System.out.println("4--" +tokenData);
			return tokenData;
			
		} catch (Exception e) {
			System.out.println("5--> failed");
			throw new RuntimeException("Failed to authenticate user", e);
		}
	}

	/**
	 * Gets an admin access token using admin credentials to perform administrative
	 * operations in Keycloak.
	 */
	private String getAdminAccessToken() {
		System.out.println("1---- inside etAdminAccessToken");
		Map<String, Object> tokenData = authenticateWithKeycloak(adminUsername, adminPassword, clientId);
		System.out.println("2 --- token data" + tokenData.toString());
		return tokenData.get("access_token").toString();
	}
}
