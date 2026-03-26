package com.shopwave.mapper;

import com.shopwave.dto.ProductDTO;
import com.shopwave.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDTO toDTO(Product p) {
        if (p == null) {
            return null;
        }

        return ProductDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .desc(p.getDesc())
                .price(p.getPrice())
                .stock(p.getStock())
                .categoryName(p.getCat() != null ? p.getCat().getName() : null)
                .createdAt(p.getCreatedAt())
                .build();
    }
}