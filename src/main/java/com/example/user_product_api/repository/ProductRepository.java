package com.example.user_product_api.repository;

import com.example.user_product_api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL " +
            "AND (p.name LIKE %:search% OR p.description LIKE %:search%)")
    Page<Product> findAllActiveProducts(@Param("search") String search, Pageable pageable);
}
