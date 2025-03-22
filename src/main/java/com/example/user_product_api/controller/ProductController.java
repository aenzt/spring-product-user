package com.example.user_product_api.controller;

import com.example.user_product_api.dto.ApiResponse;
import com.example.user_product_api.dto.PagedResponse;
import com.example.user_product_api.dto.product.ProductCreateDto;
import com.example.user_product_api.dto.product.ProductDto;
import com.example.user_product_api.dto.product.ProductUpdateDto;
import com.example.user_product_api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<ProductDto>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        PagedResponse<ProductDto> pagedResponse = productService.getAllProducts(page, size, search);
        return ResponseEntity.ok(ApiResponse.success(pagedResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable Long id) {
        ProductDto productDto = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(productDto));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@Valid @RequestBody ProductCreateDto productCreateDto) {
        ProductDto productDto = productService.createProduct(productCreateDto);
        return ResponseEntity.ok(ApiResponse.success("Product created successfully", productDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateDto productUpdateDto) {

        ProductDto productDto = productService.updateProduct(id, productUpdateDto);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", productDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }
}
