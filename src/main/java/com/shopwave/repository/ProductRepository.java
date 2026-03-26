package com.shopwave.repository;

import com.shopwave.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for Product entity operations.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find all products by category ID.
     * @param categoryId the category ID
     * @return list of products in the specified category
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Find all products with price less than or equal to the specified maximum price.
     * @param maxPrice the maximum price
     * @return list of products within the price range
     */
    List<Product> findByPriceLessThanEqual(BigDecimal maxPrice);

    /**
     * Find products where the name contains the specified keyword (case-insensitive).
     * @param keyword the search keyword
     * @return list of products matching the keyword
     */
    List<Product> findByNameContainingIgnoreCase(String keyword);

    /**
     * Find the top product ordered by price in descending order.
     * @return the most expensive product
     */
    Product findTopByOrderByPriceDesc();
}