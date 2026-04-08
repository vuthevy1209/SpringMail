package com.vuthevy1209.springmail.repository;

import com.vuthevy1209.springmail.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
        User findByGoogleId(String googleId);
}
