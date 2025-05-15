package com.apollo.userservice1.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.apollo.userservice1.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);

}
