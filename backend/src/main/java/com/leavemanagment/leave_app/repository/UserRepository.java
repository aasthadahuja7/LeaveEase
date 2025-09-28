package com.leavemanagment.leave_app.repository;

import com.leavemanagment.leave_app.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByFullName(String fullName);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}