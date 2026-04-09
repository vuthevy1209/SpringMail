package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
        User findByGoogleId(String googleId);
        Optional<User> findByEmail(String email);
}
