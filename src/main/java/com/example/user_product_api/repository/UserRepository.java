package com.example.user_product_api.repository;

import com.example.user_product_api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL " +
            "AND (u.name LIKE %:search% OR u.username LIKE %:search% OR u.email LIKE %:search%)")
    Page<User> findAllActiveUsers(@Param("search") String search, Pageable pageable);
}
