package com.shopwave.controller;

import com.shopwave.dto.CreateProductRequest;
import com.shopwave.dto.ProductDTO;
import com.shopwave.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Product operations.
 * Provides endpoints for CRUD operations and search functionality.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Get all products with pagination.
     * @param page the page number (default 0)
     * @param size the page size (default 10)
     * @return page of products
     */
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ProductDTO> products = productService.getAllProducts(PageRequest.of(page, size));
        return ResponseEntity.ok(products);
    }

    /**
     * Get a product by its ID.
     * @param id the product ID
     * @return the product details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Create a new product.
     * @param request the product creation request
     * @return the created product
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductDTO createdProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * Search products by keyword and/or maximum price.
     * @param keyword the search keyword (optional)
     * @param maxPrice the maximum price (optional)
     * @return list of matching products
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal maxPrice) {
        List<ProductDTO> products = productService.searchProducts(keyword, maxPrice);
        return ResponseEntity.ok(products);
    }

    /**
     * Update the stock of a product by a delta value.
     * @param id the product ID
     *param request the request body containing "delta" value
     * @return the updated product
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductDTO> updateStock(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        Integer delta = request.get("delta");
        if (delta == null) {
            throw new IllegalArgumentException("Delta value is required");
        }
        ProductDTO updatedProduct = productService.updateStock(id, delta);
        return ResponseEntity.ok(updatedProduct);
    }
}