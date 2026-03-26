package com.shopwave.mapper;

import com.shopwave.dto.ProductDTO;
import com.shopwave.model.Product;
import org.springframework.stereotype.Component;

/**
 * Mapper component for converting between Product entity and ProductDTO.
 */
@Component
public class ProductMapper {

    /**
     * Convert a Product entity to a ProductDTO.
     * @param product the product entity
     * @return the product DTO
     */
    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .createdAt(product.getCreatedAt())
                .build();
    }
}