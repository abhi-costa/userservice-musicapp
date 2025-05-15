package com.apollo.userservice1.services;

import com.apollo.userservice1.dto.CreateUserRequest;
import com.apollo.userservice1.dto.LoginRequest;
import com.apollo.userservice1.dto.LoginResponse;
import com.apollo.userservice1.dto.UserResponse;

public interface UserService {
	
	 UserResponse register(CreateUserRequest req);
     LoginResponse login(LoginRequest req);
     UserResponse getById(String id);

}
