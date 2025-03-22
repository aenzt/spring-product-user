package com.example.user_product_api.service;

import com.example.user_product_api.dto.PagedResponse;
import com.example.user_product_api.dto.product.ProductCreateDto;
import com.example.user_product_api.dto.product.ProductDto;
import com.example.user_product_api.dto.product.ProductUpdateDto;
import com.example.user_product_api.entity.Product;
import com.example.user_product_api.entity.User;
import com.example.user_product_api.exception.ResourceNotFoundException;
import com.example.user_product_api.repository.ProductRepository;
import com.example.user_product_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ProductDto> getAllProducts(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findAllActiveProducts(search != null ? search : "", pageable);

        List<ProductDto> productDtos = productPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                productDtos,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return mapToDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductCreateDto productCreateDto) {
        User currentUser = getCurrentUser();

        Product product = new Product();
        product.setName(productCreateDto.getName());
        product.setDescription(productCreateDto.getDescription());
        product.setPrice(productCreateDto.getPrice());
        product.setStock(productCreateDto.getStock());
        product.setCreatedBy(currentUser);

        Product savedProduct = productRepository.save(product);
        return mapToDto(savedProduct);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductUpdateDto productUpdateDto) {
        Product product = productRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(productUpdateDto.getName());
        product.setDescription(productUpdateDto.getDescription());
        product.setPrice(productUpdateDto.getPrice());
        product.setStock(productUpdateDto.getStock());

        Product updatedProduct = productRepository.save(product);
        return mapToDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Implement soft delete
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private ProductDto mapToDto(Product product) {
        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice());
        productDto.setStock(product.getStock());
        productDto.setCreatedById(product.getCreatedBy().getId());
        productDto.setCreatedByName(product.getCreatedBy().getName());
        productDto.setCreatedAt(product.getCreatedAt());
        productDto.setUpdatedAt(product.getUpdatedAt());
        return productDto;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
